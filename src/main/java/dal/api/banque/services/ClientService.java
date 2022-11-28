package dal.api.banque.services;

import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.BuyEntry;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BanqueService banqueService;

    @Autowired
    private StockService stockService;

    Logger logger = LoggerFactory.getLogger(ClientService.class);

    public Map<String,?> extraction() {
        logger.info("Debut extraction");
        //send account information as hsahmap to client
        List<Account> accounts = accountService.getAccounts();
        Map<String,Map<String,?>> accountMap= new HashMap<>();
        for(Account account : accounts) {
            JSONObject accountJson = extraction(account.getName());
            accountMap.put(account.getName(), accountJson.toMap());
        }
        logger.info("Available accounts: " + accountMap.size());
        logger.info("Fin extraction");
        return accountMap;
    }

    public JSONObject extraction(String name) {
        logger.info("Debut extraction pour le compte " + name);
        // send account information as hsahmap to client
        Account account = accountService.getAccount(name);
        JSONObject accountJson = new JSONObject();
        JSONObject stockJson = new JSONObject();
        for (Stock stock : account.getStock()) {
            stockJson.put(stock.getType(), stock.getQuantity());
        }
        accountJson.put("stock", stockJson);
        accountJson.put("money", account.getMoney());
    
        logger.info("Fin extraction");
        return accountJson;
    }

    public Boolean paiement(String fournisseur, String produit, int quantite,double prix) {
        logger.info("Debut paiement");
        Account account = accountService.getAccount(fournisseur);
        //check if the account exists
        if(account==null) {
            logger.info("Account not found");
            return false;
        }
        // //check if the stock exists
        // if(account.getStocks().stream().filter(stock -> stock.getName().equals(produit)).count()==0) {
        //     return false;
        // }
        // //check if the quantity is available
        // if(account.getStocks().stream().filter(stock -> stock.getName().equals(produit)).findFirst().get().getQuantity()<quantite) {
        //     return false;
        // }
        Banque banque = banqueService.getMyBanque();
        //update the account
        double frais = (quantite*prix)*account.getFee()/100;
        account.setMoney(account.getMoney()+(quantite*prix)-frais);
        banque.setCapital(banque.getCapital()+ frais);
        //update the stock
        Stock stock = account.getStock().stream().filter(stock1 -> stock1.getType().equals(produit)).findFirst().get();
        stock.setQuantity(stock.getQuantity()-quantite);
        stock.setQuantity((int) stock.getQuantity()-quantite);
        logger.info("Details of the transaction: fournisseur: " + fournisseur +" produit: " + produit + " qty: " + quantite + " prix: " + prix);
        accountService.saveAccount(account);
        banqueService.saveBanque(banque);

        return true;
    }

    public Boolean buy(String name, BuyEntry buyEntry) {
        Account account = accountService.getAccount(name);
        //check if the account exists
        if(account==null) {
            return false;
        }
        Banque banque = banqueService.getMyBanque();
        //update the account
        double priceOfStock =stockService.getStocks().stream().filter(stock -> stock.getType().equals(buyEntry.getType())).findFirst().get().getPrice();
        double feeOfProduct = (buyEntry.getQuantity()*priceOfStock*account.getFee()/100);
        System.out.println(feeOfProduct);
        double total = (buyEntry.getQuantity()*priceOfStock)+feeOfProduct;

        account.setMoney(account.getMoney()-total);
        banque.setCapital(banque.getCapital()+ feeOfProduct);
        //update the stock
        Stock stock = account.getStock().stream().filter(stock1 -> stock1.getType().equals(buyEntry.getType())).findFirst().get();
        stock.setQuantity((int) stock.getQuantity()+buyEntry.getQuantity());
        accountService.saveAccount(account);
        banqueService.saveBanque(banque);
        logger.info("Fin paiement");
        return true;
    }
}
