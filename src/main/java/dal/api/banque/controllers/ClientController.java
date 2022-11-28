package dal.api.banque.controllers;

import java.util.Map;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
