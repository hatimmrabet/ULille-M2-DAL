package dal.api.banque.controllers;

import javax.websocket.server.PathParam;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import dal.api.banque.exceptions.StockException;
import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.AccountEntry;
import dal.api.banque.models.entry.BuyEntry;
import dal.api.banque.services.AccountService;
import dal.api.banque.services.BanqueService;
import dal.api.banque.services.ClientService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bank")
public class BanqueController {

    @Autowired
    private BanqueService banqueService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    Logger logger = LoggerFactory.getLogger(BanqueController.class);

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
        logger.info("Recuperation de compte " + name);
        Account account = accountService.getAccount(name);
        if (account == null) {
            logger.info("Compte " + name + " n'existe pas");
            return ResponseEntity.badRequest().body("Account not found");
        }
        if (!accountService.checkPassword(name, password)) {
            logger.info("Mot de passe incorrect");
            return ResponseEntity.badRequest().body("Wrong password");
        }
        return ResponseEntity.ok().body(clientService.extraction(name).toMap());
    }

    /**
     * Ajouter un compte a notre banque si le nom du compte n'existe pas deja
     */
    @PostMapping("/account")
    public ResponseEntity<?> addAccounts(@RequestBody AccountEntry accountEntry) {
        logger.info("Creation de compte " + accountEntry.getName());
        if (accountService.checkIfAccountExistsByName(accountEntry.getName())) {
            logger.info("Compte " + accountEntry.getName() + " existe deja");
            return ResponseEntity.badRequest().body("Account already exists");
        }
        Account account = accountService.saveAccount(accountEntry);
        banqueService.addAccountToBanque(account);
        logger.info("Compte " + accountEntry.getName() + " cree");
        return ResponseEntity.status(201).body(account);
    }

    /**
     * Transformer un produit vers un autre
     */
    @PutMapping("/transform")
    public ResponseEntity<?> transform(@RequestHeader("password") String password,
            @PathParam("name") String name,
            @RequestBody Stock produitFini) {
        logger.info("Transformation du produit " + produitFini.getType() + ", qty: " + produitFini.getQuantity() + " de " + name);
        Account account = accountService.getAccount(name);
        if (account == null) {
            logger.info("Compte " + name + " n'existe pas");
            return ResponseEntity.badRequest().body("Account not found");
        }
        if (!accountService.checkPassword(name, password)) {
            logger.info("Mot de passe incorrect");
            return ResponseEntity.badRequest().body("Wrong password");
        }
        try {
            accountService.transform(account, produitFini);
        } catch (StockException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        logger.info("Transformation du produit effectuee");
        return ResponseEntity.ok().body(account);
    }

    @GetMapping("/stock")
    public ResponseEntity<?> stocksDuFournisseur(@PathParam("name") String name) {
        logger.info("Recuperation du stock de " + name);
        Account account = accountService.getAccount(name);
        if (account == null) {
            logger.info("Compte " + name + " n'existe pas");
            return ResponseEntity.badRequest().body("Account not found");
        }
        // preparer le json
        JSONObject json = clientService.extraction(account.getName());
        json.remove("money");
        logger.info("Stock recupere, size stock : " + account.getStock().size());
        return ResponseEntity.ok().body(json.toMap());
    }

    @PutMapping("/exchange")
    public ResponseEntity<?> echangerStock(@PathParam("name") String name, @RequestBody Stock stock) {
        logger.info("Echange du stock de " + name);
        logger.info("Stock : " + stock.getType() + ", qty: " + stock.getQuantity());
        Account account = accountService.getAccount(name);
        if (account == null) {
            logger.info("Compte " + name + " n'existe pas");
            return ResponseEntity.badRequest().body("Account not found");
        }
        if(accountService.exchange(account, stock)) {
            logger.info("Echange du stock effectue");
            return ResponseEntity.ok().body(clientService.extraction(name).toMap());
        } else {
            logger.info("Echange du stock echoue");
            return ResponseEntity.badRequest().body("Echange echoue, quantite insuffisante");
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<Boolean> buy(@PathParam("name") String name, @RequestBody BuyEntry buyEntry) {
        return ResponseEntity.ok().body(clientService.buy(name, buyEntry));
    }

}
