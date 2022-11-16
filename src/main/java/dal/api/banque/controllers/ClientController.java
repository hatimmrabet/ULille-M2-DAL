package dal.api.banque.controllers;

import java.util.Map;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dal.api.banque.models.entry.BuyEntry;
import dal.api.banque.services.ClientService;

@RestController
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/extraction")
    public ResponseEntity<Map<String,?>> extraction() {

        return ResponseEntity.ok().body(clientService.extraction());
    }

    @GetMapping("/paiement")
    public ResponseEntity<Boolean> paiement(@PathParam("fournisseur") String fournisseur, @PathParam("prix") Double prix, @PathParam("produit") String produit, @PathParam("qte") int qte) {
        return ResponseEntity.ok().body(clientService.paiement(fournisseur,produit,qte,prix));
    }

    @PostMapping("/buy")
        public ResponseEntity<Boolean> buy(@PathParam("name") String name, @RequestBody BuyEntry buyEntry) {
            System.out.println(buyEntry.getQuantity());
            return ResponseEntity.ok().body(clientService.buy(name, buyEntry));

        }
}
