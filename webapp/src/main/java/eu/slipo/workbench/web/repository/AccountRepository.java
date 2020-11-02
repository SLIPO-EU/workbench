package eu.slipo.workbench.web.repository;

import java.util.Set;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.web.model.admin.AccountQuery;

public interface AccountRepository {

    /**
     * Count accounts
     *
     * @return The number of accounts
     */
    int count();

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
     * Find the account with the given user name
     *
     * @param userName the account user name to search for
     * @return An {@link Account} object if the account exists; Otherwise <tt>null</tt> is
     * returned.
     */
    Account findOne(String userName);

    /**
     * Create a new account
     *
     * @param createdBy The id of the authenticated user
     * @param userName Unique user name
     * @param password User password
     * @param givenName User given name
     * @param familyName User family name
     * @param roles User assigned roles
     *
     * @return The new account
     */
    Account create(Integer createdBy, String userName, String password, String givenName, String familyName, Set<EnumRole> roles);

    /**
     * Update the account with the given id
     *
     * @param updatedBy the id of the authenticated user
     * @param account the account to update
     */
    void update(int updatedBy, Account account);

}
