package dal.api.banque.controllers;

import dal.api.banque.models.Quotation;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.services.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/quotation")
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
    public ResponseEntity<?> addQuotation(@RequestBody QuotationEntry quotationEntry) {
        Quotation quotation = quotationService.createQuotation(quotationEntry);
        return ResponseEntity.status(201).body(quotation);
    }

    @PutMapping
    @RequestMapping("/validate/{id}")
    public ResponseEntity<?> validateQuotation(@PathVariable String id) {
        System.out.println(quotationService.checkIfQuotationExistsById(id));
        System.out.println(quotationService.getAllQuotations());
        if (quotationService.checkIfQuotationExistsById(id)) {
            quotationService.validateQuotation(id);
            quotationService.createTransaction(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Quotation does not exist");
    }

    @PutMapping
    @RequestMapping("/refuse/{id}")
    public ResponseEntity<?> refuseQuotation(@PathVariable String id) {
        if (quotationService.checkIfQuotationExistsById(id)) {
            quotationService.refuseQuotation(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Quotation does not exist");
    }


}
