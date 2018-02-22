package eu.slipo.workbench.web.model.configuration;

public class GeoServerConfiguration {

    public static class Services {

        private String wfs;

        public String getWfs() {
            return wfs;
        }

        public void setWfs(String wfs) {
            this.wfs = wfs;
        }

    }

    private Services services;

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

}