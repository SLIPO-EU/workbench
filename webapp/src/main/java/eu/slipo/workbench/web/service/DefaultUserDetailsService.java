package eu.slipo.workbench.web.service;

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

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.common.repository.AccountRepository;

@Service
public class DefaultUserDetailsService implements UserDetailsService
{
    public static class Details implements UserDetails
    {
        private static final long serialVersionUID = 1L;

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
            for (EnumRole role: account.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
            }
            return authorities;
        }

        public Integer getId() {
            return account.getId();
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

        public String getLang()
        {
            return account.getLang();
        }

        public boolean hasRole(EnumRole role)
        {
            return account.hasRole(role);
        }

        public void setRoles(Set<EnumRole> roles)
        {
            this.account.setRoles(roles);
        }

    }

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        AccountEntity accountEntity = accountRepository.findOneByUsername(username);
        if (accountEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new Details(accountEntity.toDto(), accountEntity.getPassword());
    }
}
