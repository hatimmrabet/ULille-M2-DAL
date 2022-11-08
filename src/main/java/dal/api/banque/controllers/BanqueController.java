package dal.api.banque.controllers;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
@RequestMapping("/bank")
public class BanqueController {

    @Autowired
    private BanqueService banqueService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SecurityService securityService;

    /**
     * Avoir les informations de notre banque
     */
    @GetMapping
    public ResponseEntity<Banque> getBanque() {
        return ResponseEntity.ok(banqueService.getMyBanque());
    }

    /*
     * Creer notre banque si elle n'existe pas
     */
    @PostMapping
    public Banque addBanque() {
        return banqueService.createBanque();
    }

    /**
     * Le detail d'un compte
     */
    @GetMapping("/account")
    public ResponseEntity<?> getUserAccount(
            @RequestHeader("password") String password,
            @PathParam("name") String name) {
        Account account = accountService.getAccount(name);
        if (account == null)
            return ResponseEntity.badRequest().body("Account not found");
        if (!accountService.checkPassword(name, password))
            return ResponseEntity.status(401).body("Wrong password");
        return ResponseEntity.ok().body(account);
    }

    /**
     * Ajouter un compte a notre banque si le nom du compte n'existe pas deja
     */
    @PostMapping("/account")
    public ResponseEntity<?> addAccounts(@RequestBody AccountEntry accountEntry) {
        if (accountService.checkIfAccountExistsByName(accountEntry.getName()))
            return ResponseEntity.badRequest().body("Account already exists");
        Account account = accountService.saveAccount(accountEntry);
        banqueService.addAccountToBanque(account);
        return ResponseEntity.status(201).body(account);
    }

    /**
     * Transformer un produit vers un autre
     */
    @PutMapping("/transform")
    public ResponseEntity<?> transform(@RequestHeader("password") String password,
            @PathParam("name") String name,
            @RequestBody Stock stock) {
        Account account = accountService.getAccount(name);
        if (account == null)
            return ResponseEntity.badRequest().body("Account not found");
        if (!accountService.checkPassword(name, password))
            return ResponseEntity.status(401).body("Wrong password");
        account.setStocks(accountService.transform(account, stock));
        accountService.saveAccount(account);
        return ResponseEntity.ok().body(account);
    }

}
