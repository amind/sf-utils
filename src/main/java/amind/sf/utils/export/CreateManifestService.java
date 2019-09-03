package amind.sf.utils.export;

import com.sforce.soap.metadata.*;
import com.sforce.ws.ConnectionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import jdk.nashorn.internal.ir.ContinueNode;

public class CreateManifestService {

    Calendar periodStart;
    Calendar periodEnd;
    MetadataConnection metadataConnection;
    
    public static int SOAP_VERSION = 42;

    public CreateManifestService(MetadataConnection metadataConnection, Calendar periodStart, Calendar periodEnd) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;

        this.metadataConnection = metadataConnection;
    }

    public void getlastModifiedComponents(List<String> componentTypesList, boolean generateManifestCsv) throws FileNotFoundException {

        if (componentTypesList == null) {
            componentTypesList = Arrays.asList("CustomField", "ApexClass", "ApexTrigger", "ApexPage", "AuraDefinitionBundle",
                    "CustomLabel", "StaticResource", "Layout", "WorkflowRule", "WorkflowOutboundMessage", "WorkflowFieldUpdate",
                    "CustomObject", "Report", "ReportType", "StandardValueSet", "Profile", "PermissionSet", "StandardValueSetTranslation", "CustomObjectTranslation", "SamlSsoConfig",
                    "RemoteSiteSetting", "SamlSsoConfig", "ConnectedApp", "Document", "Folder", "GlobalValueSet");
        }

        Map<String, List<FileProperties>> lastModifiedFields = getLastModifiedData(componentTypesList);

        // fix incorrect layout names
        lastModifiedFields.entrySet().stream().filter((entry) -> (entry.getKey().equals("Layout"))).forEachOrdered((entry) -> {
            entry.getValue().forEach(c -> {
                String apiName = c.getFullName().split("-")[0];
                String label = c.getFullName().split("-")[1];
                if (c.getManageableState() == ManageableState.installed) {//apiName.endsWith("__c")){
                    //c.setFullName(apiName+"-"+apiName.substring(0, apiName.length() - 3)+" Layout");
                    c.setFullName(apiName + "-" + (c.getNamespacePrefix() == null ? "" : c.getNamespacePrefix() + "__") + label);
                }
            });
        });

        XMLFileBuilder xmlFileBuilder = new XMLFileBuilder();
        xmlFileBuilder.createPackageXml(lastModifiedFields);

        if (generateManifestCsv) {
            
            Map<String, List<Component>> mapOfComponent = new HashMap<>();

            for (Map.Entry<String, List<FileProperties>> entry : lastModifiedFields.entrySet()) {
                List<Component> components = entry.getValue().stream()
                        .map(p -> new Component(p.getType(), p.getFullName(), p.getLastModifiedByName(), isCreated(p), p.getLastModifiedDate().getTime()))
                        .collect(Collectors.toList());

                if (entry.getKey().equals("CustomObject")) {
                    components.forEach(c -> readCustomData(c));
                }

                if (entry.getKey().equals("Profile")) {
                    components.forEach(c -> {
                        try {
                            readProfiles(c);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                }

                if (entry.getKey().equals("CustomField")) {
                    components.forEach(c -> modifyCustomField(c));
                    mapCustomFieldComponents(components);
                }

                if (entry.getKey().equals("WorkflowOutboundMessage") || entry.getKey().equals("WorkflowFieldUpdate")
                        || entry.getKey().equals("WorkflowRule")) {
                    components.forEach(c -> readCustomData(c));
                }

                mapOfComponent.put(entry.getKey(), components);
            }

            String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            File f = new File(path);
            String directory = f.getParent();

            try (PrintWriter pw = new PrintWriter(new File(directory + "/deploymentmanifest.csv"))) {
                StringBuilder sb = new StringBuilder();

                createCSVData(mapOfComponent, sb);
                pw.write(sb.toString());
            }

        }
    }

    private Map<String, List<FileProperties>> getLastModifiedData(List<String> componentTypes) {

        List<FileProperties> properties = new ArrayList<>();
        
        componentTypes.forEach(component -> properties.addAll(getLastModifiedMetadata(component)));

        filterProperites(properties);

        System.out.println("Number of component modified : " + properties.size());

        Map<String, List<FileProperties>> lastModifiedFields = properties.stream()
                .collect(Collectors.groupingBy(i -> i.getType()));

        if (lastModifiedFields.get("CustomObject") != null && lastModifiedFields.get("CustomField") != null) {
            List<String> objects = lastModifiedFields.get("CustomObject").stream()
                    .map(prop -> prop.getFullName())
                    .collect(Collectors.toList());

            objects.forEach(o -> lastModifiedFields.get("CustomField").removeIf(field -> field.getFullName().split("\\.")[0].equals(o)));
        }
        return lastModifiedFields;
    }

    private void createCSVData(Map<String, List<Component>> mapOfComponent, StringBuilder sb) {
        sb.append("Parent Object");
        sb.append(',');
        sb.append("Operation");
        sb.append(',');
        sb.append("Component Type");
        sb.append(',');
        sb.append("Component Name");
        sb.append(',');
        sb.append("API Name");
        sb.append(',');
        sb.append("Comments");
        sb.append(',');
        sb.append("Owner: Full Name");
        sb.append(',');
        sb.append("Last Modified Date");
        sb.append('\n');

        for (Map.Entry<String, List<Component>> entry : mapOfComponent.entrySet()) {

            if (entry.getKey().equals("CustomObject")) {
                entry.getValue().forEach(c -> {
                    sb.append(c.getLabel());
                    sb.append(',');
                    sb.append(c.getOperation());
                    sb.append(',');
                    sb.append(c.isCustomSetting ? "Custom Setting" : c.getType());
                    sb.append(',');
                    sb.append(c.getApiName());
                    sb.append(',');
                    sb.append(" ");
                    sb.append(',');
                    sb.append(" ");
                    sb.append(',');
                    sb.append(c.getCreatedBy());
                    sb.append(',');
                    sb.append(c.getLastModifDate());
                    sb.append('\n');
                });
            } else if (entry.getKey().equals("ApexPage") || entry.getKey().equals("ApexClass")
                    || entry.getKey().equals("StaticResource") || entry.getKey().equals("ApexTrigger")) {
                entry.getValue().forEach(c -> {
                    sb.append(c.getParentObject());
                    sb.append(',');
                    sb.append(c.getOperation());
                    sb.append(',');
                    sb.append(c.getType());
                    sb.append(',');
                    sb.append(c.getApiName());
                    sb.append(',');
                    sb.append(" ");
                    sb.append(',');
                    sb.append(" ");
                    sb.append(',');
                    sb.append(c.getCreatedBy());
                    sb.append(',');
                    sb.append(c.getLastModifDate());
                    sb.append('\n');
                });
            } else if (entry.getKey().equals("Layout")) {
                entry.getValue().forEach(c -> {
                    sb.append(c.getParentObject());
                    sb.append(',');
                    sb.append(c.getOperation());
                    sb.append(',');
                    sb.append(c.getType());
                    sb.append(',');
                    sb.append(c.getApiName());
                    sb.append(',');
                    sb.append(" ");
                    sb.append(',');
                    sb.append(" ");
                    sb.append(',');
                    sb.append(c.getCreatedBy());
                    sb.append(',');
                    sb.append(c.getLastModifDate());
                    sb.append('\n');
                });
            } else {
                entry.getValue().forEach(c -> {
                    sb.append(c.getParentObject());
                    sb.append(',');
                    sb.append(c.getOperation());
                    sb.append(',');
                    sb.append(c.getType());
                    sb.append(',');
                    sb.append(c.getLabel());
                    sb.append(',');
                    sb.append(c.getApiName());
                    sb.append(',');
                    sb.append(c.getDescription() == null ? " " : c.getDescription());
                    sb.append(',');
                    sb.append(c.getCreatedBy());
                    sb.append(',');
                    sb.append(c.getLastModifDate());
                    sb.append('\n');
                });
            }
        }
    }

    private void filterProperites(List<FileProperties> properties) {
        properties.sort(Comparator.comparing(a -> a.getFullName()));
        
        properties.removeIf(p-> p.getLastModifiedDate().equals(p.getCreatedDate()) && p.getManageableState()!=null && p.getManageableState().equals(ManageableState.installed));

        //properties.removeIf(p->p.getManageableState().equals(ManageableState.installed));
        //properties.removeIf(p -> p.getLastModifiedByName().equals("Susheel Bist"));
        //properties.removeIf(p-> p.getType().equals("CustomObject") && p.getManageableState()!=null && p.getManageableState().equals(ManageableState.installed));
        
        
        
        
        /*properties.removeIf(p-> p.getType().equals("CustomObject") && p.getCreatedByName().equals("Susheel Bist"));
        
        
>>>>>>> origin/master
        // remove stuff for exporting HBTRR stuff and merging into DEVPRO org
        //properties.removeIf(p-> p.getType().equals("CustomObject") && p.getCreatedByName().equals("Susheel Bist"));
        //properties.removeIf(p -> !p.getLastModifiedByName().equals("yury iagutyan"));
        /*properties.removeIf(p -> p.getLastModifiedByName().equals("Shivani Sbrol"));
        properties.removeIf(p -> p.getLastModifiedByName().equals("conga conga"));
        properties.removeIf(p -> p.getLastModifiedByName().equals("Dale Pepin"));
        //properties.removeIf(p -> p.getLastModifiedByName().equals("Martin Renaud"));
        properties.removeIf(p -> p.getLastModifiedByName().equals("Shiyam Sundar"));
        properties.removeIf(p -> p.getLastModifiedByName().equals("muthu kumaran"));
        properties.removeIf(p -> p.getLastModifiedByName().equals("Shiyam Sundar"));
        properties.removeIf(p -> p.getLastModifiedByName().equals("Vinay Karajagi"));
        
        properties.removeIf(p-> p.getType().equals("ApexPage") && p.getFullName().equals("APTS_Main"));
        
        properties.removeIf(p-> p.getType().equals("Profile") && (!p.getFullName().equals("Admin") && !p.getFullName().equals("HBT RR Partner Community Login User") && !p.getFullName().equals("HBTRR Mulesoft Api")));
         */
 /*properties.removeIf(
                p -> !p.getLastModifiedByName().equals("Susheel Bist")
                && !p.getLastModifiedByName().equals("Shivani Sbrol")
                && !p.getLastModifiedByName().equals("conga conga")
                && !p.getLastModifiedByName().equals("Dale Pepin")
                && !p.getLastModifiedByName().equals("Shiyam Sundar")
                && !p.getLastModifiedByName().equals("muthu kumaran")
                && !p.getLastModifiedByName().equals("Shiyam Sundar")
                && !p.getLastModifiedByName().equals("Vinay Karajagi")
        );*/
        // remove stuff by name pattern
        /*properties.removeIf(p -> p.getFullName().contains("agf__"));
         properties.removeIf(p -> p.getFullName().contains("CoveoV2__"));
         properties.removeIf(p -> p.getFullName().contains("CRMC_PP__"));
         properties.removeIf(p -> p.getFullName().contains("MyHBT"));
         properties.removeIf(p -> p.getFullName().contains("myHBT"));
         properties.removeIf(p -> p.getFullName().contains("KCS_"));*/
 /*properties.removeIf(p-> p.getType().equals("ApexPage") && p.getFullName().equals("APTS_Main"));
         properties.removeIf(p-> p.getType().equals("ApexPage") && p.getFullName().equals("GT_PAYMENT_FRAME"));
         properties.removeIf(p-> p.getType().equals("CustomObject") && p.getFullName().equals("APTS_B2C_Community_Settings__c"));
         properties.removeIf(p-> p.getType().equals("CustomObject") && p.getFullName().equals("FOL__c"));
         properties.removeIf(p-> p.getType().equals("Workflow") && p.getFullName().equals("FOL__c"));*/

 /*properties.removeIf(p->p.getType().equals("CustomField") && p.getLastModifiedByName().equals("Susheel Bist"));
         properties.removeIf(p->p.getType().equals("CustomField") && p.getLastModifiedByName().equals("Shivani Sbrol"));
         properties.removeIf(p->p.getType().equals("ApexClass") && p.getLastModifiedByName().equals("Susheel Bist"));
         properties.removeIf(p->p.getType().equals("CustomObject") && p.getLastModifiedByName().equals("Susheel Bist"));
         properties.removeIf(p->p.getType().equals("ApexPage") && p.getLastModifiedByName().equals("Susheel Bist"));
         properties.removeIf(p->p.getType().equals("Layout") && p.getLastModifiedByName().equals("Susheel Bist"));
         properties.removeIf(p->p.getType().equals("Layout") && p.getLastModifiedByName().equals("Automated Process"));*/

 /*properties.removeIf(p->p.getType().equals("CustomField") && p.getLastModifiedByName().equals("conga conga"));
         properties.removeIf(p->p.getType().equals("CustomField") && p.getLastModifiedByName().equals("Dale Pepin"));
         properties.removeIf(p->p.getType().equals("CustomObject") && p.getLastModifiedByName().equals("Dale Pepin"));
         properties.removeIf(p->p.getType().equals("CustomObject") && p.getLastModifiedByName().equals("conga conga"));
         properties.removeIf(p->p.getType().equals("ApexPage") && p.getLastModifiedByName().equals("conga conga"));
         properties.removeIf(p->p.getType().equals("Layout") && p.getLastModifiedByName().equals("conga conga"));
         properties.removeIf(p->p.getType().equals("ApexClass") && p.getLastModifiedByName().equals("conga conga"));
         properties.removeIf(p->p.getType().equals("StaticResource") && p.getLastModifiedByName().equals("conga conga"));
         properties.removeIf(p->p.getType().equals("ApexTrigger") && p.getLastModifiedByName().equals("conga conga"));*/
        //properties.removeIf(p->p.getType().equals("StaticResource") && p.getLastModifiedByName().equals("Susheel Bist"));
        //properties.removeIf(p->p.getType().equals("ApexTrigger") && p.getLastModifiedByName().equals("Susheel Bist"));
        //properties.removeIf(p->p.getType().equals("Profile") && !p.getFullName().equals("HBT RR Partner Community Login User"));
    }

    private void modifyCustomField(Component component) {
        setParentObj(component);
        setFieldFullName(component);
        //setCustomFieldsLabel(component);
    }

    private void mapCustomFieldComponents(List<Component> components) {
        Map<String, List<Component>> mapOfFields = components.stream()
                .collect(Collectors.groupingBy(i -> i.getParentObject()));

        for (Map.Entry<String, List<Component>> entry : mapOfFields.entrySet()) {
            String parentObjLabel = getParenObjectLabel(entry.getKey());
            setCustomFieldsLabel(entry.getKey(), entry.getValue());
            entry.getValue().forEach(e -> e.setParentObject(parentObjLabel));
        }
    }

    private void setFieldFullName(Component component) {
        component.setFieldFullName(component.getApiName());
        component.setApiName(component.getApiName().split("\\.")[1]);
    }

    private void setParentObj(Component c) {
        if (c.getType().equals("CustomField")) {
            c.setParentObject(c.getApiName().split("\\.")[0]);
        }
    }

    private String isCreated(FileProperties prop) {
        return prop.getCreatedDate().before(periodStart) ? "Update" : "Create";
    }

    public void readCustomData(Component component) {
        try {
            ReadResult readResult = metadataConnection
                    .readMetadata(component.getType(), new String[]{component.getApiName()});
            Metadata[] mdInfo = readResult.getRecords();

            for (Metadata md : mdInfo) {
                if (md != null) {
                    if (component.getType().equals("CustomField")) {
                        CustomObject obj = (CustomObject) md;
                        component.setParentObject(obj.getLabel());
                    } else if (component.getType().equals("WorkflowOutboundMessage")) {
                        WorkflowOutboundMessage obj = (WorkflowOutboundMessage) md;
                        component.setLabel(obj.getName());
                        component.setDescription(obj.getDescription());
                    } else if (component.getType().equals("WorkflowRule")) {
                        WorkflowRule obj = (WorkflowRule) md;
                        component.setLabel(obj.getFullName());
                        component.setDescription(obj.getDescription());
                    } else if (component.getType().equals("WorkflowFieldUpdate")) {
                        WorkflowFieldUpdate obj = (WorkflowFieldUpdate) md;
                        component.setLabel(obj.getName());
                        component.setDescription(obj.getDescription());
                    } else {
                        CustomObject obj = (CustomObject) md;
                        component.setLabel(obj.getLabel());
                        component.setParentObject(component.getApiName());
                        component.setIsCustomSetting(obj.getCustomSettingsType() != null);
                    }

                } else {
                    System.out.println("Empty metadata.");
                }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    public void readProfiles(Component component) throws FileNotFoundException {
        List<ProfileFieldLevelSecurity> fieldSecurity = new ArrayList<>();
        try {
            ReadResult readResult = metadataConnection
                    .readMetadata(component.getType(), new String[]{component.getApiName()});
            Metadata[] mdInfo = readResult.getRecords();

            for (Metadata md : mdInfo) {
                if (md != null) {
                    Profile obj = (Profile) md;
                    fieldSecurity = Arrays.asList(obj.getFieldPermissions());
                    fieldSecurity.forEach(f -> f.getField());

                } else {
                    System.out.println("Empty metadata.");
                }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        XMLFileBuilder xmlFileBuilder = new XMLFileBuilder();
        xmlFileBuilder.createProfilePackageXml(fieldSecurity);

        PrintWriter pw = new PrintWriter(new File("profile.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("Editable");
        sb.append(',');
        sb.append("Is Readable");
        sb.append(',');
        sb.append("Field");
        sb.append(',');
        sb.append("Custom Object");
        sb.append(',');
        sb.append("Custom Field");
        sb.append('\n');

        fieldSecurity.forEach(c -> {
            sb.append(c.isEditable());
            sb.append(',');
            sb.append(c.isReadable());
            sb.append(',');
            sb.append(c.getField());
            sb.append(',');
            sb.append(c.getField().split("\\.")[0]);
            sb.append(',');
            sb.append(c.getField().split("\\.")[1]);
            sb.append('\n');
        });

        System.out.println(sb.toString());
        pw.write(sb.toString());
        pw.close();
    }

    private String getParenObjectLabel(String objName) {
        String parentObject = "";
        try {
            ReadResult readResult = metadataConnection
                    .readMetadata("CustomObject", new String[]{objName});
            Metadata[] mdInfo = readResult.getRecords();

            for (Metadata md : mdInfo) {
                if (md != null) {
                    CustomObject obj = (CustomObject) md;
                    parentObject = obj.getLabel();
                } else {
                    System.out.println("Empty metadata.");
                }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        return parentObject;
    }

    public void setCustomFieldsLabel(String objectName, List<Component> components) {
        List<CustomField> customFields = new ArrayList<>();
        try {
            ReadResult readResult = metadataConnection
                    .readMetadata("CustomObject", new String[]{objectName});
            Metadata[] mdInfo = readResult.getRecords();

            for (Metadata md : mdInfo) {
                if (md != null) {
                    CustomObject obj = (CustomObject) md;
                    customFields.addAll(Arrays.asList(obj.getFields()));
                } else {
                    System.out.println("Empty metadata.");
                }
            }

            components.forEach(c -> {

                CustomField customField = null;
                try {
                    customField = customFields.stream()
                            .filter(cf -> cf.getFullName().equals(c.getApiName()))
                            .findFirst()
                            .get();
                } catch (Exception exp) {
                }

                if (customField != null) {
                    setCustomFieldComponentData(c, customField);
                }

            });

        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

    }

    private void setCustomFieldComponentData(Component component, CustomField customField) {
        component.setLabel(customField.getLabel() == null ? "" : customField.getLabel());
        component.setDescription(customField.getDescription() == null ? "" : customField.getDescription());
    }

    public List<FileProperties> getLastModifiedMetadata(String type) {
        List<FileProperties> properties = new ArrayList<>();
        try {
            
            FileProperties[] lmr = null;
            switch(type){
                case "EmailTemplate": {
                    lmr = CreateSFPackageXMLUtils.getAllTemplatesList(metadataConnection);
                    break;
                }
                default: {
                    lmr = CreateSFPackageXMLUtils.getSFTypeMetadataList(type, metadataConnection);
                    break;
                }
            }
            
            if (lmr != null) {
                
                for(int i=0; i<lmr.length; i++){
                    System.out.println(lmr[i].getFullName());
                }
                
                if (type.equals("CustomObject")) {
                    properties = Arrays.stream(lmr)
                            .filter(field -> field.getLastModifiedDate().after(periodStart))
                            .collect(Collectors.toList());
                } else {
                    properties = Arrays.stream(lmr)
                            .filter(field -> field.getLastModifiedDate().after(periodStart))
                            .collect(Collectors.toList());
                }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        return properties;
    }
}
