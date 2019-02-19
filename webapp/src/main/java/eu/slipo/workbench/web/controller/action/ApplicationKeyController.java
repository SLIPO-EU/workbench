package eu.slipo.workbench.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.admin.ApplicationKeyCreateRequest;
import eu.slipo.workbench.web.model.admin.ApplicationKeyQuery;
import eu.slipo.workbench.web.model.admin.ApplicationKeyQueryRequest;
import eu.slipo.workbench.web.repository.ApplicationKeyRepository;

@RestController
@Secured({ "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ApplicationKeyController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationKeyController.class);

    @Autowired
    ApplicationKeyRepository applicationKeyRepository;

    @PostMapping(value = "/action/admin/application-key/query")
    public RestResponse<?> query(@RequestBody ApplicationKeyQueryRequest request) {
        try {
            if (request == null || request.getQuery() == null) {
                return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
            }

            ApplicationKeyQuery query = request.getQuery();
            PageRequest pageRequest = request.getPageRequest();
            QueryResultPage<ApplicationKeyRecord> r = this.applicationKeyRepository.query(query, pageRequest);

            return RestResponse.result(QueryResult.create(r));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    @PostMapping(value = "/action/admin/application-key")
    public RestResponse<?> create(@RequestBody ApplicationKeyCreateRequest request) {
        try {
            ApplicationKeyRecord record = this.applicationKeyRepository.create(
                this.currentUserId(),
                request.getMappedAccount(),
                request.getMaxDailyRequestLimit(),
                request.getMaxConcurrentRequestLimit()
            );

            return RestResponse.result(record);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    @DeleteMapping(value = "/action/admin/application-key/{id}/revoke")
    public RestResponse<?> find(@PathVariable int id) {
        try {
            this.applicationKeyRepository.revoke(this.currentUserId(), id);

            return RestResponse.success();
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
