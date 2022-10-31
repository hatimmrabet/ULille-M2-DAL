package dal.api.banque.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dal.api.banque.repositories.BanqueRepository;

@Service
public class BanqueService {

    @Autowired
    private BanqueRepository banqueRepository;
    
}
