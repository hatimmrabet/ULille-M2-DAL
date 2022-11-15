package dal.api.banque.services;

import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
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
            Map<String,Double> content = new HashMap<>();
            for(Stock stock : account.getStocks()) {
                content.put(stock.getName(), (double) stock.getQuantity());
            }
            content.put("compte",account.getBalance());
            accountMap.put(account.getName(),content);
        }
        return accountMap;

    }

    public Boolean paiement(String fournisseur, String produit, int quantite,double prix) {
        Account account = accountService.getAccount(fournisseur);
        //check if the account exists
        if(account==null) {
            return false;
        }
/*        //check if the stock exists
        if(account.getStocks().stream().filter(stock -> stock.getName().equals(produit)).count()==0) {
            return false;
        }
        //check if the quantity is available
        if(account.getStocks().stream().filter(stock -> stock.getName().equals(produit)).findFirst().get().getQuantity()<quantite) {
            return false;
        }*/
        Banque banque = banqueService.getMyBanque();
        //update the account
        double frais = (quantite*prix)*account.getFee()/100;
        account.setBalance(account.getBalance()+(quantite*prix)-frais);
        banque.setCapital(banque.getCapital()+ frais);
        //update the stock
        Stock stock = account.getStocks().stream().filter(stock1 -> stock1.getName().equals(produit)).findFirst().get();
        stock.setQuantity((int) stock.getQuantity()-quantite);
        accountService.saveAccount(account);
        banqueService.saveBanque(banque);

        return true;
    }
}
