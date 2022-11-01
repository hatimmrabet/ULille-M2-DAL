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
        if (banqueRepository.findById(BANQUE_ID).isPresent())
            return banqueRepository.findById(BANQUE_ID).get();
        return null;
    }

    public Banque createBanque() {
        if (getMyBanque() != null)
        {
            return getMyBanque();
        }
        else
        {
            Banque notreBanque = new Banque();
            notreBanque.setId(BANQUE_ID);
            notreBanque.setName("Rothschild & Co");
            notreBanque.setAdress("47 Rue du Faubourg Saint-Honoré, 75008 Paris");
            notreBanque.setAccounts(new ArrayList<>());
            return banqueRepository.save(notreBanque);
        }
    }

    public void addAccountToBanque(Account account) {
        Banque mybanque = getMyBanque();
        mybanque.getAccounts().add(account);
        banqueRepository.save(mybanque);
    }
    
}
