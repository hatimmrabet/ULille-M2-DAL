package dal.api.banque.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Account;
import dal.api.banque.models.Banque;
import dal.api.banque.repositories.BanqueRepository;

@Service
public class BanqueService {

    /**
     * l'ID unique de notre banque
     */
    public static final String BANQUE_ID = "14";

    @Autowired
    private BanqueRepository banqueRepository;

    @Autowired
    private AccountService accountService;

    Logger logger = LoggerFactory.getLogger(BanqueService.class);

    /**
     * Recuperer les information de notre banque en utilisant notre ID
     * return null si la banque n'existe pas 
     */
    public Banque getMyBanque() {
        logger.info("Recuperation des données de la banque");
        if (banqueRepository.findById(BANQUE_ID).isPresent())
            return banqueRepository.findById(BANQUE_ID).get();
        return null;
    }

    /**
     * Verifie si la banque existe, sinon, cree la banque avec des données static
     * @return notre banque
     */
    public Banque createBanque() {
        logger.info("Creation de la banque");
        if (getMyBanque() != null)
        {
            return getMyBanque();
        }
        else
        {
            Banque notreBanque = new Banque();
            notreBanque.setId(BANQUE_ID);
            notreBanque.setName("Rothschild & Co");
            notreBanque.setAddress("47 Rue du Faubourg Saint-Honoré, 75008 Paris");
            notreBanque.setAccounts(accountService.getAccounts());
            notreBanque.setCapital(1000);
            return banqueRepository.save(notreBanque);
        }
    }

    /**
     * Ajouter un compte a notre banque
     * @param account le compte a ajouter
     */
    public void addAccountToBanque(Account account) {
        logger.info("Ajout du compte "+account.getName()+" a la banque");
        Banque mybanque = getMyBanque();
        mybanque.getAccounts().add(account);
        banqueRepository.save(mybanque);
    }

    public void saveBanque(Banque banque) {
        banqueRepository.save(banque);
    }
}
