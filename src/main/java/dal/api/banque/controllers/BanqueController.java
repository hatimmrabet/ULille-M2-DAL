package dal.api.banque.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dal.api.banque.models.Banque;
import dal.api.banque.services.BanqueService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/banque")
public class BanqueController {

    @Autowired
    private BanqueService banqueService;
    
    @PostMapping("/add")
    public Banque addBanque() {
        return banqueService.createBanque();
    }

}
