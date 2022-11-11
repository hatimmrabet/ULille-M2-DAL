package dal.api.banque.controllers;

import dal.api.banque.models.Account;
import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.services.AccountService;
import dal.api.banque.services.QuotationService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bank/quotation")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getQuotation(@PathVariable String id) {
        Quotation quotation = quotationService.getQuotation(id);
        if (quotation != null) {
            return ResponseEntity.ok(quotation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addQuotation(
            @RequestHeader("password") String password,
            @RequestBody QuotationEntry quotationEntry, 
            @RequestParam String buyer,
            @RequestParam String seller) {
        Account account = accountService.getAccount(seller);
        if (account == null)
            return ResponseEntity.badRequest().body("Account not found");
        if (!accountService.checkPassword(seller, password))
            return ResponseEntity.status(401).body("Wrong password");
        Quotation quotation = quotationService.createQuotation(quotationEntry, buyer, seller);
        return ResponseEntity.status(201).body(quotationService.convertQuotationToQuotationDTO(quotation));
    }

    @PostMapping
    @RequestMapping("/{id}")
    public ResponseEntity<?> validateQuotation(@PathVariable String id, @RequestBody String status) {

        if (quotationService.checkIfQuotationExistsById(id)) {
            if (quotationService.getQuotation(id).getStatus() == Status.PENDING) {
                if (new JSONObject(status).getBoolean("status")) {
                    quotationService.validateQuotation(id);
                    return ResponseEntity.ok().body("Quotation validated");
                } else {
                    quotationService.refuseQuotation(id);
                    return ResponseEntity.ok().body("Quotation refused");
                }
            } else if (quotationService.getQuotation(id).getStatus() == Status.ACCEPTED) {
                return ResponseEntity.badRequest().body("Quotation already validated");
            } else if (quotationService.getQuotation(id).getStatus() == Status.REFUSED) {
                return ResponseEntity.badRequest().body("Quotation already refused");
            }
        }
        return ResponseEntity.badRequest().body("Quotation does not exist");
    }



}
