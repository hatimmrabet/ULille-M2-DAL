package dal.api.banque.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    public boolean checkIfAccountExists(String name) {
        return accountRepository.existsById(name);
    }

    public Account getAccount(String name) {
        return accountRepository.findById(name).get();
    }

    public Account convertAccountEntryToAccount(AccountEntry accountEntry) {
        Account account = new Account();
        account.setName(accountEntry.getName());
        account.setPassword(accountEntry.getPassword());
        account.setStocks(stockService.getStocks());
        return account;
    }

    public Account createAccount(AccountEntry accountEntry) {
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
        for(Stock accounStock : account.getStocks()) {
            if(accounStock.getName().equals(stock.getName())) {
                accounStock.setQuantity(accounStock.getQuantity() + stock.getQuantity());
            }
        }
        return account.getStocks();
    }

}
