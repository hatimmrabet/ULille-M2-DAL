package dal.api.banque.services;

import dal.api.banque.models.Account;
import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.repositories.AccountRepository;
import dal.api.banque.repositories.QuotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
@Service
public class QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StockService stockService;

    public Quotation getQuotation(String id) {
        return quotationRepository.findById(id).isPresent() ? quotationRepository.findById(id).get() : null;
    }

    public Quotation createQuotation(QuotationEntry quotationEntry) {
        Quotation quotation=convertQuotationEntryToQuotation(quotationEntry);
        quotation.setStatus(Status.PENDING);
        AtomicReference<Double> totalHT= new AtomicReference<>((double) 0);
        Integer fee= accountRepository.findByName(quotationEntry.getBuyer()).getFee();
        stockService.getStocks().forEach(stock -> {
            if(quotation.getCart().containsKey(stock.getName())){
                totalHT.updateAndGet(v -> v + stock.getPrice() * quotation.getCart().get(stock.getName()));
            }
        });
        quotation.setTotalHT(totalHT.get());
        quotation.setTotalTTC(totalHT.get()*fee/100+totalHT.get());
  /*      for(){
            //totalHT+=article.getKey()
            // TODO
        }*/
        quotationRepository.save(quotation);
        return quotation;
    }

    public boolean checkIfQuotationExistsById(String id) {
        return quotationRepository.existsById(id);
    }

    private Quotation convertQuotationEntryToQuotation(QuotationEntry quotationEntry) {
        Quotation quotation = new Quotation();
        quotation.setBuyer(accountRepository.findByName(quotationEntry.getBuyer()).getName());
        quotation.setSeller(accountRepository.findByName(quotationEntry.getSeller()).getName());
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

    public boolean createTransaction(String id) {
        Quotation quotation = quotationRepository.findById(id).get();
        Account buyer = accountRepository.findByName(quotation.getBuyer());
        Account seller = accountRepository.findByName(quotation.getSeller());
        buyer.setBalance(buyer.getBalance()-quotation.getTotalTTC());
        seller.setBalance(seller.getBalance()+quotation.getTotalHT());
        buyer.addStocks(quotation.getCart());
        seller.removeStocks(quotation.getCart());
        accountRepository.save(buyer);
        accountRepository.save(seller);
        return true;

    }

    public String getAllQuotations() {
        List<Quotation> quotations= quotationRepository.findAll();
        String result="";
        for(Quotation quotation:quotations){
            result+=quotation.toString()+"\n";
        }
        return result;

    }


}
