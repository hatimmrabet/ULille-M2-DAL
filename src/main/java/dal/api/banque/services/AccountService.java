package dal.api.banque.services;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            account.setStocks(stockService.getFornisseurStocks(account.getName()));
        } else {
            account.setStocks(stockService.getStocks());
        }
        // initialiser le compte avec un solde aleatoire
        account.setBalance(new Random().nextInt(1000000)/100.0);
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
            for(Stock accountStock : account.getStocks()) {
                if(rulesStock.getType().equals(accountStock.getType())) {
                    accountStock.setQuantity(accountStock.getQuantity() - (rulesStock.getQuantity() * stock.getQuantity()));
                }
            }
        }
        // ajouter le stock produit
        for(Stock accountStock : account.getStocks()) {
            if(accountStock.getType().equals(stock.getType())) {
                accountStock.setQuantity(accountStock.getQuantity() + stock.getQuantity());
            }
        }
        return account.getStocks();
    }


}
