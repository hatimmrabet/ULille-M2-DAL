package dal.api.banque.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.AccountEntry;
import dal.api.banque.services.AccountService;
import dal.api.banque.services.BanqueService;
import dal.api.banque.services.SecurityService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/banque")
public class BanqueController {

    @Autowired
    private BanqueService banqueService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SecurityService securityService;

    @GetMapping
    public ResponseEntity<Banque> getBanque() {
        return ResponseEntity.ok(banqueService.getMyBanque());
    }
    
    @PostMapping
    public Banque addBanque() {
        return banqueService.createBanque();
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        return ResponseEntity.ok(banqueService.getMyBanque().getAccounts());
    }


    @PostMapping("/accounts")
    public ResponseEntity<?> addAccounts(@RequestBody AccountEntry accountEntry) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if(accountService.checkIfAccountExistsByName(accountEntry.getName()))
            return ResponseEntity.badRequest().body("Account already exists");
        Account account = accountService.createAccount(accountEntry);
        banqueService.addAccountToBanque(account);
        // ne retourne pas id
        return ResponseEntity.status(201).body(account);
    }

    /** NOT USED for the moment */
    @PostMapping("/stocks/{id}")
    public ResponseEntity<?> addStockToAccount(@PathVariable String id, @RequestBody Stock stock){
        // System.out.println(securityService.getConnectedAccountId());
        if(!accountService.checkIfAccountExistsById(id))
            return ResponseEntity.badRequest().body("Account doesn't exist");
        Account account = accountService.getAccount(id);
        accountService.addStockToAccount(account, stock);
        return ResponseEntity.status(201).body(account);
    }

    @PostMapping("/transformation/{id}")
    public ResponseEntity<?> transform(@PathVariable String id, @RequestBody Stock stock){
        if(!accountService.checkIfAccountExistsById(id))
            return ResponseEntity.badRequest().body("Account doesn't exist");
        Account account = accountService.getAccount(id);
        account.setStocks(accountService.transform(account, stock));
        accountService.saveAccount(account);
        return ResponseEntity.ok().body(account);
    }

}
