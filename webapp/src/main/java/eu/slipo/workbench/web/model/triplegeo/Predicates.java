package eu.slipo.workbench.web.model.triplegeo;

public class Predicates {

    public static final String ATTRIBUTE_GEOMETRY = "__WKT_GEOMETRY__";

    public static final String ACCURACY = "slipo:accuracy";
    public static final String ADDRESS_COUNTRY = "slipo:country";
    public static final String ADDRESS_LOCALITY = "slipo:locality";
    public static final String ADDRESS_NUMBER = "slipo:number";
    public static final String ADDRESS_POSTAL_CODE = "slipo:postcode";
    public static final String ADDRESS_REGION = "slipo:region";
    public static final String ADDRESS_STREET = "slipo:street";
    public static final String DESCRIPTION = "slipo:description";
    public static final String EMAIL = "slipo:email";
    public static final String FAX = "slipo:fax";
    public static final String HOMEPAGE = "slipo:homepage";
    public static final String ID = "slipo:poiRef";
    public static final String LAST_UPDATED = "slipo:lastUpdated";
    public static final String LATITUDE = "wgs84_pos:lat";
    public static final String LONGITUDE = "wgs84_pos:long";
    public static final String NAME = "slipo:name";
    public static final String OPENING_HOURS = "slipo:concat";
    public static final String OTHER_LINK = "slipo:otherLink";
    public static final String PHONE = "slipo:phone";
    public static final String URL = "slipo:url";

    public static class Types {

        public static final String ACCURACY_POSITIONAL = "Positional Accuracy Level";
        public static final String ACCURACY_GEOCODING = "Geocoding Accuracy Level";
        public static final String NAME_BRAND = "brandname";
        public static final String NAME_COMPANY = "companyname";
        public static final String NAME_TRANSLIT = "transliterated";
    }

}
