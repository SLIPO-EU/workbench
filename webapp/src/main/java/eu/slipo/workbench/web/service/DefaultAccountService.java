package eu.slipo.workbench.web.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.web.model.admin.AccountErrorCode;
import eu.slipo.workbench.web.model.admin.AccountQuery;
import eu.slipo.workbench.web.repository.AccountRepository;

@Service
public class DefaultAccountService implements AccountService {


    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public QueryResultPage<Account> query(AccountQuery query, PageRequest pageReq) {
        return this.accountRepository.query(query, pageReq);
    }

    @Override
    public List<Error> update(Account account) {
        List<Error> errors = new ArrayList<Error>();

        if (account == null) {
            errors.add(new Error(AccountErrorCode.ACCOUNT_NOT_SET, "Account is not set"));
            return errors;
        }

        if (StringUtils.isBlank(account.getFamilyName())) {
            errors.add(new Error(AccountErrorCode.INVALID_FAMILY_NAME, "Family name is not set"));
        }
        if (StringUtils.isBlank(account.getGivenName())) {
            errors.add(new Error(AccountErrorCode.INVALID_GIVEN_NAME, "Given name is not set"));
        }
        if ((account.getRoles() == null) || (account.getRoles().isEmpty())) {
            errors.add(new Error(AccountErrorCode.NO_ROLE_SET, "At least one role must be selected"));
        }

        Account existingAccount = accountRepository.findOne(account.getId());
        if (existingAccount == null) {
            errors.add(new Error(AccountErrorCode.ACCOUNT_NOT_FOUND, "Account was not found"));
        }

        accountRepository.update(this.authenticationFacade.getCurrentUserId(), account);

        return errors;
    }

}
