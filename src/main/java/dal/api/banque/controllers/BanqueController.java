package dal.api.banque.controllers;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/banque")
public class BanqueController {

    @Autowired
    private BanqueService banqueService;

    @Autowired
    private AccountService accountService;

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
    public ResponseEntity<?> addAccounts(@RequestBody AccountEntry accountEntry){
        if(accountService.checkIfAccountExists(accountEntry.getName()))
            return ResponseEntity.badRequest().body("Account already exists");   
        Account account = accountService.createAccount(accountEntry);
        banqueService.addAccountToBanque(account);           
        return ResponseEntity.status(201).body(account);
    }


    @PostMapping("/stocks")
    public ResponseEntity<?> addStockToAccount(@PathParam("name") String name, @RequestBody Stock stock){
        if(!accountService.checkIfAccountExists(name))
            return ResponseEntity.badRequest().body("Account doesn't exist");
        Account account = accountService.getAccount(name);
        accountService.addStockToAccount(account, stock);     
        return ResponseEntity.status(201).body(account);
    }



}
