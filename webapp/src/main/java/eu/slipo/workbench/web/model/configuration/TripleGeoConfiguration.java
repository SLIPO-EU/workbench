package eu.slipo.workbench.web.model.configuration;

public class TripleGeoConfiguration extends AbstractToolConfiguration {

    private String[] mappingFileTypes;

    private String[] classificationFileTypes;

    public String[] getMappingFileTypes() {
        return mappingFileTypes;
    }

    public void setMappingFileTypes(String[] mappingFileTypes) {
        this.mappingFileTypes = mappingFileTypes;
    }

    public String[] getClassificationFileTypes() {
        return classificationFileTypes;
    }

    public void setClassificationFileTypes(String[] classificationFileTypes) {
        this.classificationFileTypes = classificationFileTypes;
    }

}
