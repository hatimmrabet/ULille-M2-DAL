package dal.api.banque.services;

import dal.api.banque.models.*;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.models.response.QuotationDTO;
import dal.api.banque.repositories.AccountRepository;
import dal.api.banque.repositories.BanqueRepository;
import dal.api.banque.repositories.QuotationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    private BanqueRepository banqueRepository;

    Logger logger = LoggerFactory.getLogger(QuotationService.class);

    public Quotation getQuotation(String id) {
        return quotationRepository.findById(id).isPresent() ? quotationRepository.findById(id).get() : null;
    }

    public Quotation createQuotation(QuotationEntry quotationEntry, String seller, String buyer) {
        logger.info("Creating quotation for seller: " + seller + " and buyer: " + buyer);
        Quotation quotation = convertQuotationEntryToQuotation(quotationEntry, seller, buyer);
        int fee = accountRepository.findByName(buyer).getFee();
        List<Stock> stocks = quotationEntry.getCart();
        double totalHT = stocks.get(0).getPrice()*stocks.get(0).getQuantity();
        double productionCost = getProductionCost(stocks.get(0).getType(), accountRepository.findByName(buyer).getId(),
                stocks.get(0).getQuantity());
        quotation.setFee(fee);
        quotation.setTotalHT(totalHT);
        double totalTTC = totalHT + totalHT * fee / 100;
        quotation.setTotalTTC(totalTTC);
        if (productionCost >= totalTTC) {
            quotation.setStatus(Status.REFUSED);
        } else {
            quotation.setStatus(Status.PENDING);
        }
        quotationRepository.save(quotation);
        logger.info("Quotation created with id: " + quotation.getId());
        return quotation;
    }

    public boolean checkIfQuotationExistsById(String id) {
        return quotationRepository.existsById(id);
    }

    private Quotation convertQuotationEntryToQuotation(QuotationEntry quotationEntry, String seller, String buyer) {
        Quotation quotation = new Quotation();
        quotation.setBuyer(accountRepository.findByName(seller));
        quotation.setSeller(accountRepository.findByName(buyer));
        quotation.setCart(quotationEntry.getCart());
        return quotation;
    }

    public QuotationDTO convertQuotationToQuotationDTO(Quotation quotation) {
        QuotationDTO quotationDTO = new QuotationDTO();
        quotationDTO.setId(quotation.getId());
        quotationDTO.setBuyer(quotation.getBuyer().getName());
        quotationDTO.setSeller(quotation.getSeller().getName());
        quotationDTO.setFee(quotation.getFee());
        quotationDTO.setTotalHT(quotation.getTotalHT());
        quotationDTO.setTotalTTC(quotation.getTotalTTC());
        quotationDTO.setStatus(quotation.getStatus());
        return quotationDTO;
    }

    public boolean validateQuotation(String id) {
        Quotation quotation = quotationRepository.findById(id).get();
        if (quotation.getStatus().equals(Status.PENDING)) {
            quotation.setStatus(Status.ACCEPTED);
            createTransaction(quotation.getId());
            quotationRepository.save(quotation);
            logger.info("Quotation validated");
            return true;
        }
        return false;
    }

    public boolean refuseQuotation(String id) {
        Quotation quotation = quotationRepository.findById(id).get();
        if (quotation.getStatus().equals(Status.PENDING)) {
            quotation.setStatus(Status.REFUSED);
            quotationRepository.save(quotation);
            logger.info("Quotation refused");
            return true;
        }
        return false;
    }

    public boolean createTransaction(String id) {
        logger.info("Creating transaction for quotation with id: " + id);
        Quotation quotation = quotationRepository.findById(id).get();
        Account buyer = accountRepository.findByName(quotation.getBuyer().getName());
        Account seller = accountRepository.findByName(quotation.getSeller().getName());
        logger.info("Buyer: " + buyer.getName() + " and seller: " + seller.getName());
        buyer.setBalance(buyer.getBalance() - quotation.getTotalTTC());
        seller.setBalance(seller.getBalance() + quotation.getTotalHT());
        logger.info("Buyer balance: " + buyer.getBalance() + " and seller balance: " + seller.getBalance());
        Banque banque = banqueRepository.findById(BanqueService.BANQUE_ID).get();
        buyer.addStock(quotation.getCart().get(0));
        seller.removeStocks(quotation.getCart().get(0));
        banque.setCapital(banque.getCapital() + quotation.getTotalTTC() - quotation.getTotalHT());
        accountRepository.save(buyer);
        accountRepository.save(seller);
        banqueRepository.save(banque);
        logger.info("Transaction completed");
        return true;
    }

    public String getAllQuotations() {
        List<Quotation> quotations = quotationRepository.findAll();
        String result = "";
        for (Quotation quotation : quotations) {
            result += quotation.toString() + "\n";
        }
        return result;

    }

    private double getProductionCost(String nameProduct, String idSeller, int quantity) {
        double productionCost = 0;
        Account seller = accountService.getAccountById(idSeller);
        List<Stock> ressources = stockService.getRulesForProduct(nameProduct);
        if (ressources != null) {
            for (Stock ressource : ressources) {
                for (Stock stock : seller.getStocks()) {
                    if (stock.getType().equals(ressource.getType())) {
                        productionCost += stock.getPrice() * ressource.getQuantity();
                        break;
                    }
                }
            }
        } else {
            for (Stock stock : seller.getStocks()) {
                if (stock.getType().equals(nameProduct)) {
                    productionCost += stock.getPrice();
                    break;
                }
            }
        }
        return productionCost * quantity;
    }

}
