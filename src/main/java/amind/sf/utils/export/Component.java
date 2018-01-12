package amind.sf.utils.export;

import java.util.Date;

public class Component {
    String type;
    String apiName;
    String label;
    String lastModifiedBy;
    String createdBy;
    String parentObject;
    String operation;
    String fieldFullName;
    String description;
    Date lastModifDate;

    public String getFieldFullName() {
        return fieldFullName;
    }

    public void setFieldFullName(String fieldFullName) {
        this.fieldFullName = fieldFullName;
    }

    boolean isCustomSetting;

    public Component(String type, String apiName, String createdBy, String operation, Date lastModifDate) {
        this.type = type;
        this.apiName = apiName;
        this.createdBy = createdBy;
        this.operation = operation;
        this.lastModifDate = lastModifDate;
    }

    public Component() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastModifDate() {
        return lastModifDate;
    }

    public void setLastModifDate(Date lastModifDate) {
        this.lastModifDate = lastModifDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean getIsCustomSetting() {
        return isCustomSetting;
    }

    public void setIsCustomSetting(Boolean isCustomSetting) {
        this.isCustomSetting = isCustomSetting;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getParentObject() {
        return parentObject;
    }

    public void setParentObject(String parentObject) {
        this.parentObject = parentObject;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
