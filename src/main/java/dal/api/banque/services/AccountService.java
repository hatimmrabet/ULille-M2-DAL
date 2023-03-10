package dal.api.banque.services;

import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dal.api.banque.exceptions.StockException;
import dal.api.banque.models.Account;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.AccountEntry;
import dal.api.banque.repositories.AccountRepository;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    Logger logger = org.slf4j.LoggerFactory.getLogger(AccountService.class);

    /**
     * Verifie si le compte existe par son id
     */
    public boolean checkIfAccountExistsById(String Id) {
        return accountRepository.existsById(Id);
    }

    /**
     * Verifie si le compte existe par son nom
     */
    public boolean checkIfAccountExistsByName(String name) {
        return accountRepository.existsByName(name);
    }

    public boolean checkPassword(String name, String password) {
        Account account = accountRepository.findByName(name);
        return passwordEncoder.matches(password, account.getPassword());
    }

    /**
     * Recupere un compte par son nom
     */
    public Account getAccount(String name) {
        return accountRepository.findByName(name);
    }

    /**
     * Recupere un compte par son id
     */
    public Account getAccountById(String id) {
        return accountRepository.findById(id).isPresent() ? accountRepository.findById(id).get() : null;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Creer un compte ?? partir d'un compteEntry, et remplir les champs manquants
     * avec des valeurs aleatoires
     */
    public Account convertAccountEntryToAccount(AccountEntry accountEntry) {
        Account account = new Account();
        account.setName(accountEntry.getName());
        // On crypte le mot de passe
        account.setPassword(passwordEncoder.encode(accountEntry.getPassword()));
        // initialis?? le stock du compte
        if (stockService.getFornisseurStocks(account.getName()) != null) {
            account.setStock(stockService.getFornisseurStocks(account.getName()));
        } else {
            account.setStock(stockService.getStocks());
        }
        // initialiser le compte avec un solde aleatoire
        account.setMoney(new Random().nextInt(1000000) / 100.0);
        // donner des frais de transaction aleatoire
        if (stockService.getFournisseurFrais(account.getName()) != -1) {
            account.setFee(stockService.getFournisseurFrais(account.getName()));
        } else {
            account.setFee(new Random().nextInt(15) + 5);
        }
        return account;
    }


    /**
     * Ajouter un compte a la base de donnees
     */
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Enregistrer un compte dans la base de donnees, ?? partir d'un compteEntry
     */
    public Account saveAccount(AccountEntry accountEntry) {
        return accountRepository.save(convertAccountEntryToAccount(accountEntry));
    }

    /**
     * Ajouter les produits demand?? et diminu?? le nombre de ressources necessaires
     * 
     * @return le nouveau stock
     * @throws StockException
     */
    public void transform(Account account, Stock produitFini) throws StockException {
        // equilibrer les stocks
        // boucler sur les ressources necessaires pour le produit fini
        for (Stock rulesStock : stockService.getRulesForProduct(produitFini.getType())) {
            Stock accountStock = account.getStock(rulesStock.getType());
            int qtyNecessaire = rulesStock.getQuantity() * produitFini.getQuantity();
            if (accountStock != null) {
                // si on a pas assez de ressources
                if (accountStock.getQuantity() < qtyNecessaire) {
                    int diffStock = qtyNecessaire - accountStock.getQuantity();
                    // trouver le stock dans une autre banque
                    String ip = findCorrectStockInBank(account, rulesStock.getType(), diffStock);
                    if (ip != null) {
                        // stock to update
                        Stock updateStock = new Stock(rulesStock.getType(), diffStock, 0);
                        String url = "http://" + ip + "/bank/exchange" + "?name=" + account.getName();
                        HttpEntity<Stock> request = new HttpEntity<>(updateStock);
                        RestTemplate restTemplate = new RestTemplate();
                        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request,
                                String.class);
                        if (response.getStatusCode().is2xxSuccessful()) {
                            logger.info("Stock modifie dans une autre banque");
                            // on ajoute le stock dans notre banque
                            account.addStock(updateStock);
                            logger.info("Stock ajoute dans notre banque");
                        } else {
                            logger.error("Erreur lors de la modification du stock dans une autre banque");
                        }
                    } else {
                        saveAccount(account);
                        // si on a pas trouv?? de stock dans une autre banque
                        logger.info("Pas assez de stock pour " + rulesStock.getType() + " dans toutes les banques");
                        throw new StockException("Pas assez de stock pour " + rulesStock.getType() + " dans toutes les banques");
                    }
                }
            }
        }
        // enlever les ressources necessaires
        for (Stock rulesStock : stockService.getRulesForProduct(produitFini.getType())) {
            logger.info("Enlever " + rulesStock.getQuantity() * produitFini.getQuantity() + " " + rulesStock.getType());
            Stock accountStock = account.getStock(rulesStock.getType());
            int qtyNecessaire = rulesStock.getQuantity() * produitFini.getQuantity();
            if (accountStock != null) {
                // si on a assez de ressources
                if (accountStock.getQuantity() >= qtyNecessaire) {
                    accountStock.setQuantity(accountStock.getQuantity() - qtyNecessaire);
                } else {
                    logger.error("Transformation impossible, pas assez de ressources pour " + rulesStock.getType());
                    throw new StockException("Pas assez de ressources pour " + rulesStock.getType());
                }
            }
        }
        // ajouter le produit fini
        account.addStock(produitFini);
        // sauvegarder le compte
        saveAccount(account);
    }

    public String findCorrectStockInBank(Account account, String type, int quantity) {
        logger.info("Cherche " + quantity + " " + type + " dans d'autres banques");
        RestTemplate restTemplate = new RestTemplate();
        // request each bank
        for (String ip : BanqueService.banques_ip.values()) {
            logger.info("Requesting " + ip);
            String url = "http://" + ip + "/bank/stock?name=" + account.getName();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Succesful response from " + ip);
                    JSONObject json = new JSONObject(response.getBody());
                    if (json != null) {
                        if (json.has("stock") && json.getJSONObject("stock").has(type)) {
                            int quantityInBank = json.getJSONObject("stock").getInt(type);
                            if (quantityInBank >= quantity) {
                                logger.info("Found " + type + " with quantity " + quantity + " in bank " + ip);
                                return ip;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error while requesting bank " + ip + " : " + e.getMessage());
            }
        }
        logger.info("No bank found for " + type + " with quantity " + quantity);
        return null;
    }

    public boolean exchange(Account account, Stock stock) {
        int qtyAEnlever = Math.abs(stock.getQuantity());
        if (account != null) {
            Stock AccountStock = account.getStock().stream().filter(s -> s.getType().equals(stock.getType()))
                    .findFirst().orElse(null);
            if (AccountStock != null) {
                if (AccountStock.getQuantity() >= qtyAEnlever) {
                    AccountStock.setQuantity(AccountStock.getQuantity() - qtyAEnlever);
                    accountRepository.save(account);
                    return true;
                }
            }
        }
        return false;
    }

}
