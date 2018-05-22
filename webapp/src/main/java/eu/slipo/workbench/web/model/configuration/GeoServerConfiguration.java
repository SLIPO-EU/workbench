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

    private boolean enabled;

    private String host;

    private int port;

    private String username;

    private String password;

    private String workspace;

    private String store;

    private Services services;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

}