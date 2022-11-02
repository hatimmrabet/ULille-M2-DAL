package dal.api.banque.services;

import java.util.List;

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

    public boolean checkIfAccountExistsById(String Id) {
        return accountRepository.existsById(Id);
    }

    public boolean checkIfAccountExistsByName(String name) {
        return accountRepository.existsByName(name);
    }

    public Account getAccount(String name) {
        return accountRepository.findById(name).get();
    }

    public Account convertAccountEntryToAccount(AccountEntry accountEntry) {
        Account account = new Account();
        account.setName(accountEntry.getName());
        String hashedPassword = passwordEncoder.encode(accountEntry.getPassword());
        account.setPassword(hashedPassword);
        account.setStocks(stockService.getStocks());
        return account;
    }

    public Account createAccount(AccountEntry accountEntry) {
        return accountRepository.save(convertAccountEntryToAccount(accountEntry));
    }

    public void addStockToAccount(Account account, Stock stock) {
        account.addStock(stock);
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
                    accountStock.setQuantity(accountStock.getQuantity() - (rulesStock.getQuantity() * stock.getQuantity()));
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


}
