package dal.api.banque.controllers;

import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.services.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bank/quotation")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

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
    public ResponseEntity<?> addQuotation(@RequestBody QuotationEntry quotationEntry, @RequestParam String buyer,
            @RequestParam String seller) {
        Quotation quotation = quotationService.createQuotation(quotationEntry, buyer, seller);
        return ResponseEntity.status(201).body(quotationService.convertQuotationToQuotationDTO(quotation));
    }

    @PostMapping
    @RequestMapping("/{id}")
    public ResponseEntity<?> validateQuotation(@PathVariable String id, @RequestBody boolean status) {

        if (quotationService.checkIfQuotationExistsById(id)) {
            if (quotationService.getQuotation(id).getStatus() == Status.PENDING) {
                if (status) {
                    quotationService.validateQuotation(id);
                } else {
                    quotationService.refuseQuotation(id);
                }
                return ResponseEntity.ok().build();
            } else if (quotationService.getQuotation(id).getStatus() == Status.ACCEPTED) {
                return ResponseEntity.badRequest().body("Quotation already validated");
            } else if (quotationService.getQuotation(id).getStatus() == Status.REFUSED) {
                return ResponseEntity.badRequest().body("Quotation already refused");
            }
            return ResponseEntity.badRequest().body("Quotation does not exist");
        }
        return ResponseEntity.badRequest().body("Quotation does not exist");
    }

}
