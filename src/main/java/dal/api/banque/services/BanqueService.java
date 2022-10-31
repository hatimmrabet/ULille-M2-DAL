package dal.api.banque.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
import dal.api.banque.repositories.BanqueRepository;

@Service
public class BanqueService {

    private final String BANQUE_ID = "635f91f3917ad14fec82238d";

    @Autowired
    private BanqueRepository banqueRepository;

    public Banque getMyBanque() {
        return banqueRepository.findById(BANQUE_ID).get();
    }

    public Banque createBanque() {
        Banque notreBanque = new Banque();
        notreBanque.setId(BANQUE_ID);
        notreBanque.setName("Banque de France");
        notreBanque.setAdress("Cité scientifique, Lille");
        notreBanque.setAccounts(new ArrayList<>());
        return banqueRepository.save(notreBanque);
    }

    public void addAccountToBanque(Account account) {
        Banque mybanque = getMyBanque();
        mybanque.getAccounts().add(account);
        banqueRepository.save(mybanque);
    }
    
}
