package dal.api.banque.services;

import dal.api.banque.models.Account;
import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.repositories.AccountRepository;
import dal.api.banque.repositories.QuotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private AccountService accountService;

    public Quotation getQuotation(String id) {
        return quotationRepository.findById(id).isPresent() ? quotationRepository.findById(id).get() : null;
    }

    public Quotation createQuotation(QuotationEntry quotationEntry,String seller,String buyer) {
        Quotation quotation=convertQuotationEntryToQuotation(quotationEntry,seller,buyer);
        int fee= accountRepository.findByName(buyer).getFee();
        List<Stock> stocks= quotationEntry.getCart();
        double totalHT= stocks.get(0).getPrice();
        double productionCost =getProductionCost(stocks.get(0).getName(),accountRepository.findByName(buyer).getId(),stocks.get(0).getQuantity());

        quotation.setFee(fee);
        quotation.setTotalHT(totalHT);
        double totalTTC=totalHT+totalHT*fee/100;
        quotation.setTotalTTC(totalTTC);
        if (productionCost>=totalTTC){
            quotation.setStatus(Status.REFUSED);
        }
        else {
            quotation.setStatus(Status.PENDING);
        }
        quotationRepository.save(quotation);
        return quotation;
    }

    public boolean checkIfQuotationExistsById(String id) {
        return quotationRepository.existsById(id);
    }

    private Quotation convertQuotationEntryToQuotation(QuotationEntry quotationEntry,String seller,String buyer) {
        Quotation quotation = new Quotation();
        quotation.setBuyer(accountRepository.findByName(seller));
        quotation.setSeller(accountRepository.findByName(buyer));
        quotation.setCart(quotationEntry.getCart());
        return quotation;
    }

    public boolean validateQuotation(String id){
        Quotation quotation = quotationRepository.findById(id).get();
        if(quotation.getStatus().equals(Status.PENDING)){
            quotation.setStatus(Status.ACCEPTED);
            quotationRepository.save(quotation);
            return true;
        }
        return false;
    }

    public boolean refuseQuotation(String id){
        Quotation quotation = quotationRepository.findById(id).get();
        if(quotation.getStatus().equals(Status.PENDING)){
            quotation.setStatus(Status.REFUSED);
            quotationRepository.save(quotation);
            return true;
        }
        return false;
    }

/*    public boolean createTransaction(String id) {
        Quotation quotation = quotationRepository.findById(id).get();
        Account buyer = accountRepository.findByName(quotation.getBuyer().getName());
        Account seller = accountRepository.findByName(quotation.getSeller().getName());
        buyer.setBalance(buyer.getBalance()-quotation.getTotalTTC());
        seller.setBalance(seller.getBalance()+quotation.getTotalHT());
        buyer.addStocks(quotation.getCart());
        seller.removeStocks(quotation.getCart());
        accountRepository.save(buyer);
        accountRepository.save(seller);
        return true;

    }*/

    public String getAllQuotations() {
        List<Quotation> quotations= quotationRepository.findAll();
        String result="";
        for(Quotation quotation:quotations){
            result+=quotation.toString()+"\n";
        }
        return result;

    }

    private double getProductionCost(String nameProduct, String idSeller,int quantity){
        double productionCost=0;
        Account seller = accountService.getAccount(idSeller);
        List<Stock> ressources=stockService.getRulesForProduct(nameProduct);
        if (ressources!=null){

        for (Stock ressource:ressources){
            for (Stock stock:seller.getStocks()){
                if(stock.getName().equals(ressource.getName())){
                    productionCost+=stock.getPrice()*ressource.getQuantity();
                    break;
                }
            }
        }
        }
        else {
            for (Stock stock:seller.getStocks()){
                if(stock.getName().equals(nameProduct)){
                    productionCost+=stock.getPrice();
                    break;
                }
            }
        }
        return productionCost*quantity;
    }

}
