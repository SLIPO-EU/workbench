package eu.slipo.workbench.web.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import eu.slipo.workbench.web.logging.filter.MappedDiagnosticContextFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    @Qualifier("defaultUserDetailsService")
    UserDetailsService userService;

    @Override
    public void configure(WebSecurity security) throws Exception
    {
        security.ignoring()
            .antMatchers("/css/**", "/js/**", "/images/**", "vendor/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception
    {
        // Which authentication providers are to be used?

        builder.userDetailsService(userService)
            .passwordEncoder(new BCryptPasswordEncoder());

        builder.eraseCredentials(true);
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {
        // Authorize requests:
        // Which granted authorities must be present for each request?

        security.authorizeRequests()
            .antMatchers(
                    "/", "/index",
                    "/login", "/logged-out")
                .permitAll()
            .antMatchers(
                    "/logged-in", "/logout",
                    "/action/**")
                .authenticated()
            .antMatchers(
                    "/admin/**")
                .hasAuthority("ROLE_ADMIN");

        // Support form-based login

        security.formLogin()
            .loginPage("/login")
            .failureUrl("/login?error")
            .defaultSuccessUrl("/logged-in", true)
            .usernameParameter("username")
            .passwordParameter("password");

        security.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/logged-out")
            .invalidateHttpSession(true);

        // Configure CSRF

        security.csrf()
            .requireCsrfProtectionMatcher((HttpServletRequest req) -> {
                String method = req.getMethod();
                String path = req.getServletPath();
                if (path.startsWith("/api/")) {
                    return false; // exclude Rest API
                }
                if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
                    return true; // include all state-changing methods
                }
                return false;
             });

        // Do not redirect unauthenticated requests (just respond with a status code)

        security.exceptionHandling()
            .authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        // Add servlet filters

        security.addFilterAfter(
            new MappedDiagnosticContextFilter(), SwitchUserFilter.class);
    }
}
