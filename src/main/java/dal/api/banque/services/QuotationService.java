package dal.api.banque.services;

import java.util.List;

import dal.api.banque.exceptions.StockException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.repositories.AccountRepository;
import dal.api.banque.repositories.BanqueRepository;
import dal.api.banque.repositories.QuotationRepository;
import org.springframework.web.client.RestTemplate;

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
        quotation.setHT(totalHT);
        double totalTTC = totalHT + totalHT * fee / 100;
        quotation.setTTC(totalTTC);
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

    public JSONObject buildQuotationResponse(Quotation quotation) {
        JSONObject response = new JSONObject();
        response.put("id", quotation.getId());
        response.put("fee", quotation.getFee());
        response.put("HT", quotation.getHT());
        response.put("TTC", quotation.getTTC());
        return response;
    }

    public boolean validateQuotation(String id) throws StockException {
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

    public boolean createTransaction(String id) throws StockException {
        logger.info("Creating transaction for quotation with id: " + id);
        Quotation quotation = quotationRepository.findById(id).get();
        Account buyer = accountRepository.findByName(quotation.getBuyer().getName());
        Account seller = accountRepository.findByName(quotation.getSeller().getName());
        logger.info("Buyer: " + buyer.getName() + " and seller: " + seller.getName());
        Stock sellerStock = seller.getStock().stream().filter(stock -> stock.getType().equals(quotation.getCart().get(0).getType())).findFirst().get();
        int qtyNecessaire = quotation.getCart().get(0).getQuantity();
    // si on a pas assez de ressources

        if (sellerStock.getQuantity() < qtyNecessaire) {
            int diffStock = qtyNecessaire - sellerStock.getQuantity();
            // trouver le stock dans une autre banque
            String ip = accountService.findCorrectStockInBank(seller, quotation.getCart().get(0).toString(), diffStock);
            if (ip != null) {
                // stock to update in the other bank
                Stock updateStock = new Stock(sellerStock.getType(), diffStock, 0);
                String url = "http://" + ip + "/bank/exchange" + "?name=" + seller.getName();
                HttpEntity<Stock> request = new HttpEntity<>(updateStock);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request,
                        String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Stock modifie dans une autre banque");
                    // on ajoute le stock dans notre banque
                    seller.addStock(updateStock);
                    logger.info("Stock ajoute dans notre banque");
                } else {
                    logger.error("Erreur lors de la modification du stock dans une autre banque");
                }
            } else {
                // si on a pas trouvé de stock dans une autre banque
                logger.info("Pas assez de stock pour " + sellerStock.getType() + " dans toutes les banques");
                throw new StockException("Pas assez de stock pour " + sellerStock.getType() + " dans toutes les banques");
            }
        }

        buyer.setMoney(buyer.getMoney() - quotation.getTTC());
        seller.setMoney(seller.getMoney() + quotation.getHT());
        logger.info("Buyer balance: " + buyer.getMoney() + " and seller balance: " + seller.getMoney());
        Banque banque = banqueRepository.findById(BanqueService.BANQUE_ID).get();
        buyer.addStock(quotation.getCart().get(0));
        seller.removeStocks(quotation.getCart().get(0));
        banque.setCapital(banque.getCapital() + quotation.getTTC() - quotation.getHT());
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
                for (Stock stock : seller.getStock()) {
                    if (stock.getType().equals(ressource.getType())) {
                        productionCost += stock.getPrice() * ressource.getQuantity();
                        break;
                    }
                }
            }
        } else {
            for (Stock stock : seller.getStock()) {
                if (stock.getType().equals(nameProduct)) {
                    productionCost += stock.getPrice();
                    break;
                }
            }
        }
        return productionCost * quantity;
    }

    public boolean checkCart(List<Stock> cart) {
        for(Stock stock : cart) {
            if (!stockService.getStocks().stream().filter(s -> s.getType().equalsIgnoreCase(stock.getType())).findFirst().isPresent()) {
                logger.info("Stock with name "+stock.getType()+" not found");
                return false;
            }
        }
        return true;
    }

}
