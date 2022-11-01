package dal.api.banque.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Account;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.AccountEntry;
import dal.api.banque.repositories.AccountRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StockService stockService;

    public boolean checkIfAccountExistsById(String Id) {
        return accountRepository.existsById(Id);
    }

    public boolean checkIfAccountExistsByName(String name) {
        return accountRepository.existsByName(name);
    }

    public Account getAccount(String name) {
        return accountRepository.findById(name).get();
    }

    public Account convertAccountEntryToAccount(AccountEntry accountEntry) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Account account = new Account();
        account.setName(accountEntry.getName());
        String hashedPassword = hashPassword(accountEntry.getPassword());
        account.setPassword(hashedPassword);
        account.setStocks(stockService.getStocks());
        return account;
    }

    public Account createAccount(AccountEntry accountEntry) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return accountRepository.save(convertAccountEntryToAccount(accountEntry));
    }

    public void addStockToAccount(Account account, Stock stock) {
        account.getStocks().add(stock);
        accountRepository.save(account);
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }


    public List<Stock> transform(Account account, Stock stock) {
        // diminuer le stock en se bsant sur la quantité demandé et les regles de transformation
        for(Stock rulesStock : stockService.getRulesForProduct(stock.getName())) {
            for(Stock accountStock : account.getStocks()) {
                if(rulesStock.getName().equals(accountStock.getName())) {
                    accountStock.setQuantity( accountStock.getQuantity() - (rulesStock.getQuantity() * stock.getQuantity()));
                }
            }
        }
        // ajouter le stock produit
        for(Stock accountStock : account.getStocks()) {
            if(accountStock.getName().equals(stock.getName())) {
                accountStock.setQuantity(accountStock.getQuantity() + stock.getQuantity());
            }
        }
        return account.getStocks();
    }

    public String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //hash password with bcrypt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return new String(hash);
    }


}
