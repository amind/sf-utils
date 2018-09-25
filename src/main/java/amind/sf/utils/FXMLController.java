package amind.sf.utils;

import com.sforce.soap.metadata.MetadataConnection;
import amind.sf.utils.export.CreateManifestService;
import amind.sf.utils.export.RetrieveSample;
import com.sforce.soap.enterprise.fault.LoginFault;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.SaveResult;
import com.sforce.soap.metadata.StaticResource;
import com.sforce.soap.metadata.StaticResourceCacheControl;
import com.sforce.soap.metadata.UpsertResult;
import com.sforce.ws.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.zeroturnaround.zip.ZipUtil;

public class FXMLController implements Initializable {

    @FXML
    private DatePicker fromDatePickerControl;

    @FXML
    private DatePicker tillDatePickerControl;

    @FXML
    private TextArea statusTextAreaControl;

    @FXML
    private TextArea objectTypesTextAreaControl;

    @FXML
    private TextField sfUsernameTextFieldControl;

    @FXML
    private PasswordField sfPasswordPasswordFieldControl;

    @FXML
    private TextField sfSecurityTokenTextFieldControl;
    
    @FXML
    private ComboBox sfLoginUrlControl;

    @FXML
    private TextField packageFilePathTextFieldControl;

    @FXML
    private CheckBox generateManifestCsv;

    private final String redStyle = "-fx-highlight-fill: #ff0000; -fx-highlight-text-fill: #000000; -fx-text-fill: #ff0000; ";
    private final String greenStyle = "-fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        objectTypesTextAreaControl.setText("ApexClass, ApexTrigger, ApexPage, AuraDefinitionBundle, \n CustomObject, CustomField, CustomLabel, WebLink, \n StaticResource, Layout, WorkflowRule, WorkflowOutboundMessage, WorkflowFieldUpdate, Report, ReportType, StandardValueSet, Profile, PermissionSet, \n Translations, StandardValueSetTranslation, CustomObjectTranslation, GlobalValueSetTranslation, \n RemoteSiteSetting, SamlSsoConfig, ConnectedApp, Document, GlobalValueSet, SamlSsoConfig");

        //objectTypesTextAreaControl.setText("Layout");
        sfUsernameTextFieldControl.setText("artem.nakhapetiani@amindsolutions.com");
        sfPasswordPasswordFieldControl.setText("");
        //sfSecurityTokenTextFieldControl.setText("lbtBEK2IQLVbec6sgxkCkA2Cg");
        fromDatePickerControl.setValue(LocalDate.now().minusYears(1));
        tillDatePickerControl.setValue(LocalDate.now());
        
        sfLoginUrlControl.getItems().addAll(
            "test.salesforce.com",
            "login.salesforce.com"  
        );
        sfLoginUrlControl.setValue("login.salesforce.com");
    }

    private void checkLoginFields() {
        if (sfUsernameTextFieldControl.getText().trim().equalsIgnoreCase("")) {
            throw new RuntimeException("Please enter SF username.");
        }

        if (sfPasswordPasswordFieldControl.getText().trim().equalsIgnoreCase("")) {
            throw new RuntimeException("Please enter SF password.");
        }

        /*if(sfSecurityTokenTextFieldControl.getText().trim().equalsIgnoreCase("")){
            throw new RuntimeException("Please enter SF security token.");
        }*/
    }

    @FXML
    private void exportManifestAction(ActionEvent event) {

        try {

            this.checkLoginFields();

            if (fromDatePickerControl.getValue() == null || tillDatePickerControl.getValue() == null) {
                throw new RuntimeException("Please enter export dates.");
            }

            Calendar periodStart = Calendar.getInstance();
            periodStart.set(fromDatePickerControl.getValue().getYear(), fromDatePickerControl.getValue().getMonthValue() - 1, fromDatePickerControl.getValue().getDayOfMonth());

            Calendar periodEnd = Calendar.getInstance();
            periodEnd.set(tillDatePickerControl.getValue().getYear(), tillDatePickerControl.getValue().getMonthValue() - 1, tillDatePickerControl.getValue().getDayOfMonth());

            MetadataConnection metadataConnection = MetadataLoginUtil.login(
                    sfUsernameTextFieldControl.getText(),
                    sfPasswordPasswordFieldControl.getText() + sfSecurityTokenTextFieldControl.getText(),
                    sfLoginUrlControl.getValue().toString()
            );
            CreateManifestService createManifestService = new CreateManifestService(metadataConnection, periodStart, periodEnd);

            List<String> typesList = Arrays.asList(objectTypesTextAreaControl.getText().replace(" ", "").replace("\n", "").split(","));

            createManifestService.getlastModifiedComponents(typesList, generateManifestCsv.isSelected());

            statusTextAreaControl.setText("Success!");
            statusTextAreaControl.setStyle(greenStyle);

        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: " + (ex == null ? " undefined error." : ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }

    }

    @FXML
    private void chooseExportPackageXML(ActionEvent event) {

        try {

            Node source = (Node) event.getSource();
            Window theStage = source.getScene().getWindow();

            FileChooser fileChooser = new FileChooser();
            File selectedDirectory
                    = fileChooser.showOpenDialog(theStage);

            if (selectedDirectory == null) {
                packageFilePathTextFieldControl.setText("");
                throw new RuntimeException("No file was selected.");
            } else {
                packageFilePathTextFieldControl.setText(selectedDirectory.getAbsolutePath());
            }
        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: " + (ex == null ? " undefined error." : ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }

    }

    @FXML
    private void exportSFObjectsByPackageXML(ActionEvent event) {

        try {

            this.checkLoginFields();

            if (packageFilePathTextFieldControl.getText().trim().equalsIgnoreCase("")) {
                throw new RuntimeException("Package.xml file is not choosed.");
            }

            MetadataConnection metadataConnection = MetadataLoginUtil.login(
                    sfUsernameTextFieldControl.getText(),
                    sfPasswordPasswordFieldControl.getText() + sfSecurityTokenTextFieldControl.getText(),
                    sfLoginUrlControl.getValue().toString()
            );

            RetrieveSample sample = new RetrieveSample(
                    metadataConnection,
                    packageFilePathTextFieldControl.getText()
            );
            sample.retrieveZip();

            statusTextAreaControl.setText("Success!");
            statusTextAreaControl.setStyle(greenStyle);

        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: " + (ex == null ? " undefined error." : ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }

    }

    /*
     * Stuff for zipping/deploying statics
     */
    @FXML
    private TextField staticsFolderPathTextFieldControl;

    @FXML
    private TextField staticsNameTextFieldControl;
    
    @FXML
    private CheckBox cacheControlIsPublicChecboxFieldControl;

    @FXML
    private void chooseStaticsFolder(ActionEvent event) {

        try {

            Node source = (Node) event.getSource();
            Window theStage = source.getScene().getWindow();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory
                    = directoryChooser.showDialog(theStage);

            if (selectedDirectory == null) {
                staticsFolderPathTextFieldControl.setText("");
                throw new RuntimeException("No directory was selected.");
            } else {
                staticsFolderPathTextFieldControl.setText(selectedDirectory.getAbsolutePath());
            }

        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: " + (ex == null ? " undefined error." : ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        } finally {

        }

    }

    @FXML
    private void zipAndDeployStatics(ActionEvent event) {

        File staticsZip = null;

        try {

            if (staticsNameTextFieldControl.getText().trim().equalsIgnoreCase("")) {
                throw new RuntimeException("Statics name is empty.");
            }

            if (staticsFolderPathTextFieldControl.getText().trim().equalsIgnoreCase("")) {
                throw new RuntimeException("Statics folder path is empty.");
            }

            MetadataConnection metadataConnection = MetadataLoginUtil.login(
                    sfUsernameTextFieldControl.getText(),
                    sfPasswordPasswordFieldControl.getText() + sfSecurityTokenTextFieldControl.getText(),
                    sfLoginUrlControl.getValue().toString()
            );

            //staticsZip = Zip4JUtils.compress(staticsFolderPathTextFieldControl.getText());
            String tmpFileDir = System.getProperty("java.io.tmpdir");

            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();

            String tmpFileName = tmpFileDir + "/tmpf" + randomUUIDString + ".zip";
            staticsZip = new File(tmpFileName);
            
            ZipUtil.pack(new File(staticsFolderPathTextFieldControl.getText()), staticsZip);

            StaticResource statics = new StaticResource();
            statics.setFullName(staticsNameTextFieldControl.getText());
            try (FileInputStream fis = new FileInputStream(staticsZip)) {
                statics.setContent(FileUtil.toBytes(fis));
            }
            if(cacheControlIsPublicChecboxFieldControl.isSelected()){
                statics.setCacheControl(StaticResourceCacheControl.Public);
            }else{
                statics.setCacheControl(StaticResourceCacheControl.Private);
            }
            statics.setContentType("application/zip");

            Metadata[] metadata = new Metadata[]{statics};

            UpsertResult[] upsertMetadataResult = metadataConnection.upsertMetadata(metadata);

            if (!upsertMetadataResult[0].isSuccess()) {
                throw new RuntimeException(upsertMetadataResult[0].getErrors()[0].getMessage());
            }

            statusTextAreaControl.setText("Statics successfuly zipped/deployed.");
            statusTextAreaControl.setStyle(greenStyle);

        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: " + (ex == null ? " undefined error." : ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        } finally {
            if (staticsZip != null) {
                boolean delete = staticsZip.delete();
            }
        }

    }

}
