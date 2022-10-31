package dal.api.banque.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Banque;
import dal.api.banque.repositories.BanqueRepository;

@Service
public class BanqueService {

    @Autowired
    private BanqueRepository banqueRepository;

    private final String BANQUE_ID = "635f91f3917ad14fec82238d";

    public Banque createBanque() {
        Banque notreBanque = new Banque();
        notreBanque.setId(BANQUE_ID);
        notreBanque.setNom("Banque de France");
        notreBanque.setAdresse("Cité scientifique, Lille");
        return banqueRepository.save(notreBanque);
    }
    
}
