package eu.slipo.workbench.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;

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

        private List<ValueListItem<EnumDataSourceType>> dataSources = new ArrayList<ValueListItem<EnumDataSourceType>>();

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

        public List<ValueListItem<EnumDataSourceType>> getDataSources() {
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

        public void addDataSource(ValueListItem<EnumDataSourceType> s) {
            this.dataSources.add(s);
        }

        public void addResourceType(ValueListItem<EnumResourceType> t) {
            this.resourceTypes.add(t);
        }

    }
}
