package dal.api.banque.controllers;

import dal.api.banque.models.Account;
import dal.api.banque.services.AccountService;
import dal.api.banque.services.BanqueService;
import dal.api.banque.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ClientController {


    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    @GetMapping("/extraction")
    public ResponseEntity<Map<String,Map<String,Double>>> extraction() {

        return ResponseEntity.ok().body(clientService.extraction());
    }

    @GetMapping("/paiement")
    public ResponseEntity<Boolean> paiement(@PathParam("fournisseur") String fournisseur, @PathParam("prix") Double prix, @PathParam("produit") String produit, @PathParam("qte") int qte) {
        return ResponseEntity.ok().body(clientService.paiement(fournisseur,produit,qte,prix));
    }


}
