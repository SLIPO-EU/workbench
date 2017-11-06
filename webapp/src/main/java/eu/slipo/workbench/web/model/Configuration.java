package eu.slipo.workbench.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.web.model.resource.EnumDataSource;

/**
 * Application configuration settings
 */
public class Configuration {

    ValueListCollection values = new ValueListCollection();

    public ValueListCollection getValues() {
        return values;
    }

    public static class ValueListCollection {

        private List<ValueListItem<EnumTool>> tools = new ArrayList<ValueListItem<EnumTool>>();

        private List<ValueListItem<EnumOperation>> operations = new ArrayList<ValueListItem<EnumOperation>>();

        private List<ValueListItem<EnumDataFormat>> dataFormats = new ArrayList<ValueListItem<EnumDataFormat>>();

        private List<ValueListItem<EnumDataSource>> dataSources = new ArrayList<ValueListItem<EnumDataSource>>();

        private List<ValueListItem<EnumResourceType>> resourceTypes = new ArrayList<ValueListItem<EnumResourceType>>();

        public List<ValueListItem<EnumTool>> getTools() {
            return Collections.unmodifiableList(tools);
        }

        public List<ValueListItem<EnumOperation>> getOperations() {
            return Collections.unmodifiableList(operations);
        }

        public List<ValueListItem<EnumDataFormat>> getDataFormats() {
            return Collections.unmodifiableList(dataFormats);
        }

        public List<ValueListItem<EnumDataSource>> getDataSources() {
            return Collections.unmodifiableList(dataSources);
        }

        public List<ValueListItem<EnumResourceType>> getResourceTypes() {
            return Collections.unmodifiableList(resourceTypes);
        }

        public void addTool(ValueListItem<EnumTool> t) {
            this.tools.add(t);
        }

        public void addOperation(ValueListItem<EnumOperation> o) {
            this.operations.add(o);
        }

        public void addDataFormat(ValueListItem<EnumDataFormat> f) {
            this.dataFormats.add(f);
        }

        public void addDataSource(ValueListItem<EnumDataSource> s) {
            this.dataSources.add(s);
        }

        public void addResourceType(ValueListItem<EnumResourceType> t) {
            this.resourceTypes.add(t);
        }

    }
}
