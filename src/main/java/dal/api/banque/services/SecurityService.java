package dal.api.banque.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Account;
import dal.api.banque.repositories.AccountRepository;

@Service
public class SecurityService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByName(username);
        if (account == null) {
            throw new UsernameNotFoundException("User not found with name:"+username);
        }
        List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("user"));
        return new User(account.getName(), account.getPassword(), authorities);
    }

    public Account getConnectedAccount()
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByName(username);
    }

    public String getConnectedAccountId()
    {
        return getConnectedAccount().getId();
    }
    
}
