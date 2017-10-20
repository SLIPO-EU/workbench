package eu.slipo.workbench.web.model;

public abstract class Tool {

    /**
     * Get all supported operations
     *
     * @return a list of {@link EnumOperation}
     */
    public abstract EnumOperation[] Implements();

    /**
     * Returns true if the {@code operation} is supported
     *
     * @param operation the operation
     * @return true if the {@code operation} is supported
     */
    public abstract boolean Supports(EnumOperation operation);

}
