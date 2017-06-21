package eu.slipo.workbench.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.domain.AccountEntity;
import eu.slipo.workbench.model.Account;
import eu.slipo.workbench.model.EnumRole;
import eu.slipo.workbench.repository.AccountRepository;

@Service
public class DefaultUserService implements UserService
{
    @Autowired
    private AccountRepository userRepository;
    
    private static class Details implements UserDetails
    {
        private final Account account;
        
        private final String password;
        
        public Details(Account account, String password)
        {
            this.account = account;
            this.password = password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities()
        {
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (EnumRole role: account.getRoles())
                authorities.add(new SimpleGrantedAuthority(role.name()));
            return authorities;    
        }

        @Override
        public String getPassword()
        {
            return password;
        }

        @Override
        public String getUsername()
        {
            return account.getUsername();
        }

        @Override
        public boolean isAccountNonExpired()
        {
            return account.isActive();
        }

        @Override
        public boolean isAccountNonLocked()
        {
            return !account.isBlocked();
        }

        @Override
        public boolean isCredentialsNonExpired()
        {
            return account.isActive();
        }

        @Override
        public boolean isEnabled()
        {
            return account.isActive();
        }
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException
    {
        AccountEntity accountEntity = userRepository.findOneByUsername(username);
        if (accountEntity == null)
            throw new UsernameNotFoundException(username);
        return new Details(accountEntity.toDto(), accountEntity.getPassword());
    }

    @Override
    public Account findOneByUsername(String username)
    {
        AccountEntity accountEntity = userRepository.findOneByUsername(username);
        return accountEntity == null? null : accountEntity.toDto();
    }

    @Override
    public Account findOneByEmail(String email)
    {
        AccountEntity eaccountEntity = userRepository.findOneByEmail(email);
        return eaccountEntity == null? null : eaccountEntity.toDto();
    }
}
