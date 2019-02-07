package eu.slipo.workbench.web.model.provenance;

public class PropertyAction {

    public String operation;
    public String property;
    public String value;

    private PropertyAction() {

    }

    public static PropertyAction of(String property, String operation, String value) {
        PropertyAction pa = new PropertyAction();
        pa.property = property;
        pa.operation = operation;
        pa.value = value;
        return pa;
    }

}
