package eu.slipo.workbench.web.controller.action;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceMetadataView;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.web.model.Dashboard;
import eu.slipo.workbench.web.model.Event;

/**
 * Actions for querying generic application data
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class DashboardController {

    /**
     * Returns data for several KPIs (key performance indicators) relevant to the
     * workbench application
     *
     * @param Authentication the authenticated principal
     * @return an instance of {@link Dashboard}}
     */
    @RequestMapping(value = "/action/dashboard", method = RequestMethod.GET)
    public RestResponse<Dashboard> getDashboard(Authentication Authentication) {
        return RestResponse.result(this.createDashboard());
    }

    private Dashboard createDashboard() {
        Dashboard dashboard = new Dashboard();

        for (int i = 0; i < 10; i++) {
            dashboard.addResource(createResource(i + 1));
        }

        for (int i = 0; i < 10; i++) {
            dashboard.addProcessExecution(createProcessExecution(i));
        }

        for (int i = 0; i < 10; i++) {
            dashboard.addEvent(this.createEvent(i));
        }

        dashboard.setStatistics(createStatistics());

        return dashboard;
    }

    private Event createEvent(int id) {
        String level = "INFO";

        switch (id % 3) {
            case 0:
                level = "INFO";
                break;
            case 1:
                level = "WARN";
                break;
            case 2:
                level = "ERROR";
                break;
        }

        return new Event(
            "webapp",
            ZonedDateTime.now(),
            level,
            "Authentication has failed for user 'admin'",
            null,
            "192.168.0.2",
            "admin",
            "Authentication",
            "Login");
    }

    private ProcessExecutionRecord createProcessExecution(int id) {
        ProcessExecutionRecord process = new ProcessExecutionRecord(id, id, 1);

        process.setName(String.format("Process %d", id));
        process.setStartedOn(ZonedDateTime.now().minusHours(id + 1));
        switch(id % 4) {
            case 0:
                process.setStatus(EnumProcessExecutionStatus.COMPLETED);
                process.setCompletedOn(ZonedDateTime.now());
                break;
            case 1:
                process.setStatus(EnumProcessExecutionStatus.RUNNING);
                break;
            case 2:
                process.setStatus(EnumProcessExecutionStatus.STOPPED);
                process.setCompletedOn(ZonedDateTime.now());
                break;
            case 3:
                process.setStatus(EnumProcessExecutionStatus.FAILED);
                process.setErrorMessage("Error description");
                process.setCompletedOn(ZonedDateTime.now());
                break;
        }
        process.setSubmittedBy(1, "Admin");
        process.setSubmittedOn(process.getStartedOn().minusMinutes(5));

        return process;
    }

    private ResourceRecord createResource(long id) {
        ResourceRecord resource = this.createResourceRevision(id, 1);

        resource.addRevision(this.createResourceRevision(id, 2));
        resource.addRevision(this.createResourceRevision(id, 3));
        resource.addRevision(this.createResourceRevision(id, 4));

        return resource;

    }

    private ResourceRecord createResourceRevision(long id, int version) {
        ResourceRecord resource = new ResourceRecord(id, version);

        resource.setType(EnumResourceType.POI_DATA);
        resource.setSourceType(EnumDataSourceType.UPLOAD);
        resource.setInputFormat(EnumDataFormat.GPX);
        resource.setFormat(EnumDataFormat.N_TRIPLES);
        resource.setProcessExecutionId(1L);
        resource.setCreatedOn(ZonedDateTime.now());
        resource.setUpdatedOn(resource.getCreatedOn());
        resource.setTableName(UUID.randomUUID());
        resource.setFilePath("file.xml");
        resource.setFileSize((long) (Math.random() * 1024 * 1024) + 100);


        resource.setMetadata(
            new ResourceMetadataView(
                String.format("Resource %d", id),
                "Uploaded sample POI data",
                (int) (Math.random() * 1024*1204 + 100)
            )
        );

        return resource;
    }

    private Dashboard.StatisticsCollection createStatistics() {
        Dashboard.StatisticsCollection result = new Dashboard.StatisticsCollection();

        result.resources = new Dashboard.ResourceStatistics(120, 10, 4);
        result.processes = new Dashboard.ProcessStatistics(20, 2, 3);
        result.events = new Dashboard.EventStatistics(0, 5, 75);
        result.system = new Dashboard.SystemStatistics(12, 100, 24, 128, 75, 2048);

        return result;
    }

}
