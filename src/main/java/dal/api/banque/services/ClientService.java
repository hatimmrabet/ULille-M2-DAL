package dal.api.banque.services;

import dal.api.banque.models.Account;
import dal.api.banque.models.Stock;
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

    public Map<String,Map<String,Double>> extraction() {
        //send account information as hsahmap to client
        List<Account> accounts =accountService.getAccounts();
        Map<String,Map<String,Double>> accountMap= new HashMap<>();
        //retour → {«Fournisseur B&M» : {« compte » : montant, « bois » : montant, ...},
        //« Fournisseur M&Pe » : ...}
        for(Account account : accounts) {
            accountMap.put(account.getName(),null);
            Map<String,Double> content = new HashMap<>();
            for(Stock stock : account.getStocks()) {
                content.put(stock.getName(), (double) stock.getQuantity());
            }
            content.put("compte",account.getBalance());
            accountMap.put(account.getName(),content);
        }
        return accountMap;

    }
}
