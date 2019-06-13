package eu.slipo.workbench.web.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.web.model.admin.AccountCreateRequest;
import eu.slipo.workbench.web.model.admin.AccountQuery;

public interface AccountService {

    /**
     * Find accounts filtered by an {@link AccountQuery}
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @param pageReq A page request
     */
    QueryResultPage<Account> query(AccountQuery query, PageRequest pageReq);

    /**
     * Create a new account
     *
     * @param request Account data
     * @return a list of {@link Error} objects if any errors occur, or an empty list
     * if the operation is successful
     */
    List<Error> create(AccountCreateRequest request);

    /**
     * Update an existing account
     *
     * @param account the account to update
     * @return a list of {@link Error} objects if any errors occur, or an empty list
     * if the operation is successful
     */
    List<Error> update(Account account);
}
