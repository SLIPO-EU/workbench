package eu.slipo.workbench.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.Dashboard;
import eu.slipo.workbench.web.repository.DashboardRepository;

/**
 * Actions for querying generic application data
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class DashboardController {

    @Autowired
    DashboardRepository dashboardRepository;

    /**
     * Returns data for several KPIs (key performance indicators) relevant to the
     * workbench application
     *
     * @param Authentication the authenticated principal
     * @return an instance of {@link Dashboard}}
     * @throws Exception if a data access operation fails
     */
    @RequestMapping(value = "/action/dashboard", method = RequestMethod.GET)
    public RestResponse<Dashboard> getDashboard(Authentication Authentication) throws Exception {
        final Dashboard dashboard = dashboardRepository.load();

        return RestResponse.result(dashboard);
    }

}
