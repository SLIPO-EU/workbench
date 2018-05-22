package eu.slipo.workbench.web.service.etl;

public class DatabaseImportResult {

    private Exception exception;

    private String schema;

    private String tableName;

    private String geometryColumn;

    public DatabaseImportResult(Exception exception) {
        this.exception = exception;
    }

    public DatabaseImportResult(String schema, String tableName, String geometryColumn) {
        this.schema = schema;
        this.tableName = tableName;
        this.geometryColumn = geometryColumn;
    }

    public boolean getSuccess() {
        return (exception == null);
    }

    public Exception getException() {
        return exception;
    }

    public String getSchema() {
        return schema;
    }

    public String getTableName() {
        return tableName;
    }

    public String getGeometryColumn() {
        return geometryColumn;
    }

}
