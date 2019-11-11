package eu.slipo.workbench.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.ProcessDraftRecord;
import eu.slipo.workbench.common.repository.ProcessDraftRepository;

/**
 * Actions for managing process drafts
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProcessDraftController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ProcessDraftRepository processDraftRepository;

    @GetMapping(value = "/action/process/draft")
    public RestResponse<?> getDraft() {
        try {
            ProcessDraftRecord record = processDraftRepository.findOne( this.currentUserId());

            return RestResponse.result(record);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    @PostMapping(value = "/action/process/draft")
    public RestResponse<?> save(@RequestBody String definition) {
        try {
            ProcessDraftRecord record = this.processDraftRepository.save(this.currentUserId(), definition, false);

            return RestResponse.result(record);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    @PostMapping(value = "/action/process/draft/{id}/{version}")
    public RestResponse<?> save(@PathVariable long id, @PathVariable long version, @RequestBody String definition) {
        try {

            ProcessDraftRecord record = this.processDraftRepository.save(
                this.currentUserId(), definition, id, false
            );

            return RestResponse.result(record);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
