package slipo.eu.workbench.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    public void configure(WebSecurity security) throws Exception
    {
        security.ignoring().antMatchers("/css/**", "/js/**", "/images/**");
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception
    {
        // Which authentication providers are to be used?
        
        // Todo configure UserDetailsService
        
        builder.inMemoryAuthentication()
            .withUser("admin")
                .password("adm1n")
                .authorities("ADMIN", "MAINTAINER")
            .and()
            .withUser("malex")
                .password("m@lex")
                .authorities("USER");
        
        builder.eraseCredentials(true);
    }
      
    @Override
    protected void configure(HttpSecurity security) throws Exception
    {        
        // Authorize requests: 
        // Which granted authorities must be present for each request?
        
        security.authorizeRequests()
            .antMatchers(
                "/index", "/", 
                "/login", "/logout", "/logged-out")
            .permitAll()
            .antMatchers(
                "/user/me")
            .hasAuthority("USER")
            .antMatchers(
                "/users",
                "/api/user/new")
            .hasAuthority("ADMIN");

        // Support normal form-based login/logout

        security.formLogin()
            .loginPage("/login")
            .failureUrl("/login?error")
            .defaultSuccessUrl("/user/me")
            .usernameParameter("username")
            .passwordParameter("password");

        security.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/logged-out")
            .invalidateHttpSession(true);       
        
        // Todo Configure CSRF
    }
}
