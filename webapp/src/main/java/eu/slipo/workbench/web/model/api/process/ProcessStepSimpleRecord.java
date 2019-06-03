package eu.slipo.workbench.web.model.api.process;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.Step;

public class ProcessStepSimpleRecord {

    private int key;
    private int group;
    private String name;
    private EnumOperation operation;
    private EnumTool tool;
    private List<String> inputKeys = new ArrayList<>();
    private String outputKey;

    public ProcessStepSimpleRecord() {

    }

    public ProcessStepSimpleRecord(Step step) {
        this.key = step.key();
        this.group = step.group();
        this.name = step.name();
        this.operation = step.operation();
        this.tool = step.tool();
        this.inputKeys.addAll(step.inputKeys());
        this.outputKey = step.outputKey();
    }

    public int getKey() {
        return key;
    }

    public int getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public EnumTool getTool() {
        return tool;
    }

    public List<String> getInputKeys() {
        return inputKeys;
    }

    public String getOutputKey() {
        return outputKey;
    }

}
