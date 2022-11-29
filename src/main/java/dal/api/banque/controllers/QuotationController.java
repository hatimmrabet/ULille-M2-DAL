package dal.api.banque.controllers;

import dal.api.banque.exceptions.StockException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dal.api.banque.models.Account;
import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.services.AccountService;
import dal.api.banque.services.QuotationService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bank/quotation")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private AccountService accountService;

    Logger logger = LoggerFactory.getLogger(QuotationController.class);

    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getQuotation(@PathVariable String id) {
        logger.info("Recuperation de quotation " + id);
        Quotation quotation = quotationService.getQuotation(id);
        if (quotation != null) {
            return ResponseEntity.ok(quotation);
        } else {
            logger.info("Quotation " + id + " n'existe pas");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addQuotation(
            @RequestHeader("password") String password,
            @RequestBody QuotationEntry quotationEntry, 
            @RequestParam String buyer,
            @RequestParam String seller) {
        if (buyer.trim().equals(seller.trim())) {
            logger.info("Le vendeur et l'acheteur ne peuvent pas etre les memes");
            return ResponseEntity.badRequest().body("Le vendeur et l'acheteur ne peuvent pas etre les memes");
        }
        Account account = accountService.getAccount(seller);
        if (account == null )
        {
            logger.info("Compte " + seller + " n'existe pas");            
            return ResponseEntity.badRequest().body("Seller account not found");
        }
        if (!accountService.checkPassword(seller, password))
        {
            logger.info("Mot de passe incorrect");            
            return ResponseEntity.badRequest().body("Wrong password");
        }
        if (accountService.getAccount(buyer) == null )
        {
            logger.info("Compte " + buyer + " n'existe pas");            
            return ResponseEntity.badRequest().body("Buyer account not found");
        }
        if(!quotationService.checkCart(quotationEntry.getCart()))
        {
            logger.info("Panier invalide");
            return ResponseEntity.badRequest().body("Invalid cart");
        }
        Quotation quotation = quotationService.createQuotation(quotationEntry, buyer, seller);
        logger.info("Quotation " + quotation.getId() + " added");
        return ResponseEntity.status(201).body(quotationService.buildQuotationResponse(quotation).toMap());
    }

    @PostMapping
    @RequestMapping("/{id}")
    public ResponseEntity<?> validateQuotation(@PathVariable String id, @RequestBody String status) {
        logger.info("Validation de quotation " + id);
        if (quotationService.checkIfQuotationExistsById(id)) {
            Quotation quotation = quotationService.getQuotation(id);
            if (quotation.getStatus() == Status.PENDING) {
                if (new JSONObject(status).getBoolean("status")) {
                    try {
                        quotationService.validateQuotation(id);
                    }
                    catch (StockException e) {
                        logger.info("Stock insuffisant");
                        return ResponseEntity.badRequest().body("Stock insuffisant dans toutes les banques");
                    }
                    logger.info("Quotation " + id + " validated");
                    return ResponseEntity.ok().body("Quotation validated");
                } else {
                    quotationService.refuseQuotation(id);
                    return ResponseEntity.ok().body("Quotation refused");
                }
            } else if (quotation.getStatus() == Status.ACCEPTED) {
                logger.info("Quotation " + id + " already accepted");
                return ResponseEntity.badRequest().body("Quotation already validated");
            } else if (quotation.getStatus() == Status.REFUSED) {
                logger.info("Quotation " + id + " already refused");
                return ResponseEntity.badRequest().body("Quotation already refused");
            }
        }
        logger.info("Quotation " + id + " n'existe pas");
        return ResponseEntity.badRequest().body("Quotation does not exist");
    }



}
