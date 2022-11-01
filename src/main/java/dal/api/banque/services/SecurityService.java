package dal.api.banque.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Account;
import dal.api.banque.models.SecurityUser;
import dal.api.banque.repositories.AccountRepository;

@Service
public class SecurityService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByName(username);
        if (account == null) {
            throw new UsernameNotFoundException("User not found: "+username);
        }
        System.out.println("User found: "+account.getName());
        System.out.println("password: "+account.getPassword());
        return new SecurityUser(account);
    }
    
}
