package io.mosip.certify.core.dto;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MDocTemplateData holds the structured data for mDoc credential template processing.
 * This class organizes data by namespaces and manages digestID assignment for mDoc elements.
 */
public class MDocTemplateData {

    @JsonProperty("docType")
    private String docType;

    @JsonProperty("namespaces")
    private Map<String, NamespaceData> namespaces;

    @JsonProperty("validityInfo")
    private ValidityInfo validityInfo;

    public MDocTemplateData() {
        this.namespaces = new HashMap<>();
        this.validityInfo = new ValidityInfo();
    }

    public MDocTemplateData(String docType) {
        this.docType = docType;
        this.namespaces = new HashMap<>();
        this.validityInfo = new ValidityInfo();
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Map<String, NamespaceData> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, NamespaceData> namespaces) {
        this.namespaces = namespaces;
    }

    public ValidityInfo getValidityInfo() {
        return validityInfo;
    }

    public void setValidityInfo(ValidityInfo validityInfo) {
        this.validityInfo = validityInfo;
    }

    /**
     * Add data element to a specific namespace
     */
    public void addDataElement(String namespace, String elementIdentifier, Object value) {
        if (!namespaces.containsKey(namespace)) {
            namespaces.put(namespace, new NamespaceData());
        }
        namespaces.get(namespace).addElement(elementIdentifier, value);
    }

    /**
     * Get data element from a specific namespace
     */
    public Object getDataElement(String namespace, String elementIdentifier) {
        if (namespaces.containsKey(namespace)) {
            return namespaces.get(namespace).getElement(elementIdentifier);
        }
        return null;
    }

    /**
     * Get all element identifiers for a namespace
     */
    public List<String> getElementIdentifiers(String namespace) {
        if (namespaces.containsKey(namespace)) {
            return namespaces.get(namespace).getElementIdentifiers();
        }
        return new ArrayList<>();
    }

    /**
     * NamespaceData holds the data elements for a specific namespace
     */
    public static class NamespaceData {
        @JsonProperty("elements")
        private Map<String, DataElement> elements;

        public NamespaceData() {
            this.elements = new HashMap<>();
        }

        public Map<String, DataElement> getElements() {
            return elements;
        }

        public void setElements(Map<String, DataElement> elements) {
            this.elements = elements;
        }

        public void addElement(String elementIdentifier, Object value) {
            elements.put(elementIdentifier, new DataElement(elementIdentifier, value));
        }

        public Object getElement(String elementIdentifier) {
            DataElement element = elements.get(elementIdentifier);
            return element != null ? element.getValue() : null;
        }

        public List<String> getElementIdentifiers() {
            return new ArrayList<>(elements.keySet());
        }
    }

    /**
     * DataElement represents a single data element with its identifier and value
     */
    public static class DataElement {
        @JsonProperty("elementIdentifier")
        private String elementIdentifier;

        @JsonProperty("value")
        private Object value;

        @JsonProperty("digestID")
        private Integer digestID;

        public DataElement() {}

        public DataElement(String elementIdentifier, Object value) {
            this.elementIdentifier = elementIdentifier;
            this.value = value;
        }

        public String getElementIdentifier() {
            return elementIdentifier;
        }

        public void setElementIdentifier(String elementIdentifier) {
            this.elementIdentifier = elementIdentifier;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Integer getDigestID() {
            return digestID;
        }

        public void setDigestID(Integer digestID) {
            this.digestID = digestID;
        }
    }

    /**
     * ValidityInfo holds the validity information for the mDoc
     */
    public static class ValidityInfo {
        @JsonProperty("signed")
        private String signed;

        @JsonProperty("validFrom")
        private String validFrom;

        @JsonProperty("validUntil")
        private String validUntil;

        public ValidityInfo() {}

        public String getSigned() {
            return signed;
        }

        public void setSigned(String signed) {
            this.signed = signed;
        }

        public String getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(String validFrom) {
            this.validFrom = validFrom;
        }

        public String getValidUntil() {
            return validUntil;
        }

        public void setValidUntil(String validUntil) {
            this.validUntil = validUntil;
        }
    }
}