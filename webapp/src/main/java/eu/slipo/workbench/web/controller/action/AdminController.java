package eu.slipo.workbench.web.controller.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.web.model.EventRecord;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.admin.AccountQuery;
import eu.slipo.workbench.web.model.admin.AccountQueryRequest;
import eu.slipo.workbench.web.model.admin.EventQuery;
import eu.slipo.workbench.web.model.admin.EventQueryRequest;
import eu.slipo.workbench.web.repository.EventRepository;
import eu.slipo.workbench.web.service.AccountService;

/**
 * Actions for administration tasks
 */
@RestController
@RequestMapping(produces = "application/json")
public class AdminController extends BaseController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AccountService accountService;

    /**
     * Search for system events
     *
     * @param data the query to execute
     * @return a list of events
     */
    @Secured({ "ROLE_ADMIN", "ROLE_DEVELOPER" })
    @RequestMapping(value = "/action/admin/events", method = RequestMethod.POST)
    public RestResponse<QueryResult<EventRecord>> find(@RequestBody EventQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        EventQuery query = request.getQuery();
        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<EventRecord> r = this.eventRepository.query(query, pageRequest);

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Search for accounts
     *
     * @param data the query to execute
     * @return a list of accounts
     */
    @Secured({ "ROLE_ADMIN" })
    @RequestMapping(value = "/action/admin/accounts", method = RequestMethod.POST)
    public RestResponse<QueryResult<Account>> find(@RequestBody AccountQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        AccountQuery query = request.getQuery();
        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<Account> r = this.accountService.query(query, pageRequest);

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Update an account
     *
     * @param account the account to update
     * @return an empty response if the update is successful or a list of {@link Error}
     * objects if any errors occur
     */
    @Secured({ "ROLE_ADMIN" })
    @RequestMapping(value = "/action/admin/account", method = RequestMethod.POST)
    public RestResponse<?> update(@RequestBody Account account) {

        List<Error> errors = this.accountService.update(account);

        if (errors.isEmpty()) {
            return RestResponse.success();
        }
        return RestResponse.error(errors);
    }

}
