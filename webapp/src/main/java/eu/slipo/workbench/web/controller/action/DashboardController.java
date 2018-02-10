package eu.slipo.workbench.web.controller.action;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceMetadataView;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.web.model.Dashboard;
import eu.slipo.workbench.web.model.Event;

/**
 * Actions for querying generic application data
 */
@RestController
public class DashboardController {

    /**
     * Returns data for several KPIs (key performance indicators) relevant to the
     * workbench application
     *
     * @param Authentication the authenticated principal
     * @return an instance of {@link Dashboard}}
     */
    @RequestMapping(value = "/action/dashboard", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Dashboard> getDashboard(Authentication Authentication) {
        return RestResponse.result(this.createDashboard());
    }

    private Dashboard createDashboard() {
        Dashboard dashboard = new Dashboard();

        for (int i = 0; i < 10; i++) {
            long id = i + 1;
            ResourceRecord resource = this.createResource(id, 1);

            resource.addVersion(this.createResource(id, 2));
            resource.addVersion(this.createResource(id, 3));
            resource.addVersion(this.createResource(id, 4));

            dashboard.addResouce(resource);
        }

        for (int i = 0; i < 10; i++) {
            dashboard.addEvent(this.createEvent());
        }

        dashboard.setStatistics(createStatistics());

        return dashboard;
    }

    private Event createEvent() {
        return new Event(
            "webapp",
            ZonedDateTime.now(),
            "ERROR",
            "Authentication has failed for user 'admin'",
            null,
            "192.168.0.2",
            "admin",
            "Authentication",
            "Login");
    }

    private ResourceRecord createResource(long id, int version) {
        ResourceRecord resource = new ResourceRecord(id, version);

        resource.setType(EnumResourceType.POI_DATA);
        resource.setDataSource(EnumDataSourceType.UPLOAD);
        resource.setInputFormat(EnumDataFormat.GPX);
        resource.setOutputFormat(EnumDataFormat.N_TRIPLES);
        resource.setProcessExecutionId(1L);
        resource.setCreatedOn(ZonedDateTime.now());
        resource.setUpdatedOn(resource.getCreatedOn());
        resource.setTable(UUID.randomUUID());
        resource.setFilePath("file.xml");
        resource.setFileSize((int) (Math.random() * 1024 * 1024) + 100);


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

        result.resources = new Dashboard.ResourceStitistics(120, 10, 4);
        result.events = new Dashboard.EventStitistics(0, 5, 75);

        return result;
    }

}
