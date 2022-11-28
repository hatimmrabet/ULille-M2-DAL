package dal.api.banque.services;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
     * Creer un compte à partir d'un compteEntry, et remplir les champs manquants
     * avec des valeurs aleatoires
     */
    public Account convertAccountEntryToAccount(AccountEntry accountEntry) {
        Account account = new Account();
        account.setName(accountEntry.getName());
        // On crypte le mot de passe
        account.setPassword(passwordEncoder.encode(accountEntry.getPassword()));
        // initialisé le stock du compte
        if(stockService.getFornisseurStocks(account.getName()) != null) {
            account.setStock(stockService.getFornisseurStocks(account.getName()));
        } else {
            account.setStock(stockService.getStocks());
        }
        // initialiser le compte avec un solde aleatoire
        account.setMoney(new Random().nextInt(1000000)/100.0);
        // donner des frais de transaction aleatoire
        if(stockService.getFournisseurFrais(account.getName()) != -1) {
            account.setFee(stockService.getFournisseurFrais(account.getName()));
        } else {
            account.setFee(new Random().nextInt(15) + 5);
        }
        return account;
    }


    /**
     * Ajouter un stock a un compte, on ajoute que la quantité si le stock existe deja
     */
    public void addStockToAccount(Account account, Stock stock) {
        // utiliser notre fonction pour l'ajout d'un stock
        account.addStock(stock);
        accountRepository.save(account);
    }


    /**
     * Ajouter un compte a la base de donnees
     */ 
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Enregistrer un compte dans la base de donnees, à partir d'un compteEntry
     */
    public Account saveAccount(AccountEntry accountEntry) {
        return accountRepository.save(convertAccountEntryToAccount(accountEntry));
    }


    /**
     * Ajouter les produits demandé et diminué le nombre de ressources necessaires 
     * @return le nouveau stock
     */
    public List<Stock> transform(Account account, Stock stock) {
        // diminuer le stock en se basant sur la quantité demandé et les regles de transformation
        for(Stock rulesStock : stockService.getRulesForProduct(stock.getType())) {
            for(Stock accountStock : account.getStock()) {
                if(rulesStock.getType().equals(accountStock.getType())) {
                    if (accountStock.getQuantity() < rulesStock.getQuantity() * stock.getQuantity()) {// si on a pas assez de ressources
                        int diffStock = rulesStock.getQuantity() * stock.getQuantity() - accountStock.getQuantity();
                        String ip = findCorrectStockInBank(account, rulesStock.getType(),diffStock);// trouver le stock dans une autre banque
                        if(ip != null) {
                                // exchange request on other bank
                            Stock updateStock = new Stock(rulesStock.getType(), diffStock, rulesStock.getPrice());// stock to update
                            String url = "http://" + ip + "/bank/exchange"+ "?name=" + account.getName();
                            HttpEntity<Stock> request = new HttpEntity<>(updateStock);
                            RestTemplate restTemplate = new RestTemplate();
                            restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, request, JSONObject.class);
                            accountStock.setQuantity(accountStock.getQuantity() + stock.getQuantity());
                            Stock stocktoreduce = account.getStock().stream().filter(s -> s.getType().equals(rulesStock.getType())).findFirst().get();
                            stocktoreduce.setQuantity(0);

                        } else {
                            return null;
                        }

                    }
                }
            }
        }
        // ajouter le stock produit

        return account.getStock();
    }

    public String findCorrectStockInBank(Account account, String type, int quantity) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> ipBank = BanqueService.banques_ip;
        //request each bank
        for(String ip : ipBank.keySet()){
            String url = "http://" + ip + "/bank/stock?name=" + account.getName();
            try{
                ResponseEntity<JSONObject> response = restTemplate.getForEntity(url, JSONObject.class);
                JSONObject json = response.getBody();
                if(json != null){
                    if(json.has(type)){
                        int quantityInBank = json.getInt("quantity");
                        if(quantityInBank >= quantity){
                            return ip;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error");
            }
        }
        return null;
    }

    public boolean exchange(Account account, Stock stock) {
        int qtyAEnlever = Math.abs(stock.getQuantity());
        if (account != null) {
            Stock AccountStock = account.getStock().stream().filter(s -> s.getType().equals(stock.getType())).findFirst().orElse(null);
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
