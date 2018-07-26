package eu.slipo.workbench.web.repository;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.web.model.admin.AccountQuery;

public interface AccountRepository {

    /**
     * Find accounts filtered by an {@link AccountQuery}
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @param pageReq A page request
     */
    QueryResultPage<Account> query(AccountQuery query, PageRequest pageReq);

    /**
     * Find the account with the given id
     *
     * @param id the account id to search for
     * @return An {@link Account} object if the account exists; Otherwise <tt>null</tt> is
     * returned.
     */
    Account findOne(int id);

    /**
     * Update the account with the given id
     *
     * @param updatedBy the id of the authenticated user
     * @param account the account to update
     */
    void update(int updatedBy, Account account);

}
