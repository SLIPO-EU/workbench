package eu.slipo.workbench.web.listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.web.repository.AccountRepository;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StartupApplicationListener.class);

    @Value("${security.default.admin.name:}")
    private String username;

    @Value("${security.default.admin.password:}")
    private String password;

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            final int count = accountRepository.count();

            if (count == 0) {
                initializeDefaultAccount();
            }
        } catch (Exception ex) {
            logger.error("Failed to initialize application default account", ex);
        }
    }

    @Transactional
    private void initializeDefaultAccount() {
        try {
            boolean logPassword = false;

            if (StringUtils.isBlank(username)) {
                logger.warn("Default admin username is not set. Admin account was not created.");
                return;
            }
            if (StringUtils.isBlank(password)) {
                password = UUID.randomUUID().toString();
                logPassword = true;
            }

            final Set<EnumRole> roles = new HashSet<EnumRole>(Arrays.asList(EnumRole.USER, EnumRole.ADMIN));

            this.accountRepository.create(null,username  , password, "admin", "", roles);

            if(logPassword) {
                logger.info("Default admin user [{}] has been created. Password is [{}].", username, password);
            } else {
                logger.info("Default admin user [{}] has been created.", username);
            }
        } catch (Exception ex) {
            logger.error("Failed to initialize application default account", ex);
        }
    }

}
