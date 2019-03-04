package eu.slipo.workbench.web.model.triplegeo;

import java.util.List;

import eu.slipo.workbench.common.model.tool.TriplegeoFieldMapping;

public class MappingFileRequest {

    private List<TriplegeoFieldMapping> mappings;

    public List<TriplegeoFieldMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<TriplegeoFieldMapping> mappings) {
        this.mappings = mappings;
    }

}
