package amind.sf.utils;

import com.sforce.soap.metadata.MetadataConnection;
import amind.sf.utils.export.CreateManifestService;
import amind.sf.utils.export.MetadataLoginUtil;
import amind.sf.utils.export.RetrieveSample;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

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
    private TextField packageFilePathTextFieldControl;
    
    private final String redStyle = "-fx-highlight-fill: #ff0000; -fx-highlight-text-fill: #000000; -fx-text-fill: #ff0000; ";
    private final String greenStyle = "-fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ";
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        objectTypesTextAreaControl.setText("ApexClass, ApexTrigger, ApexPage, \n CustomObject, CustomField, CustomLabels, \n StaticResource, Layout, WorkflowRule, WorkflowOutboundMessage, WorkflowFieldUpdate, Report, ReportType, StandardValueSet, Profile, StandardValueSetTranslation, CustomObjectTranslation, RemoteSiteSetting, SamlSsoConfig, ConnectedApp, Document, Folder, GlobalValueSet");
        objectTypesTextAreaControl.setText("Layout");
        sfUsernameTextFieldControl.setText("artem.nakhapetiani@amindsolutions.com");
        sfPasswordPasswordFieldControl.setText("");
        sfSecurityTokenTextFieldControl.setText("0NdPumPeRinbgUHltCaJnYcOv");
        fromDatePickerControl.setValue(LocalDate.now().minusYears(1));
        tillDatePickerControl.setValue(LocalDate.now());
    }  
    
    private void checkLoginFields(){
        if(sfUsernameTextFieldControl.getText().trim().equalsIgnoreCase("")){
            throw new RuntimeException("Please enter SF username.");
        }

        if(sfPasswordPasswordFieldControl.getText().trim().equalsIgnoreCase("")){
            throw new RuntimeException("Please enter SF password.");
        }

        if(sfSecurityTokenTextFieldControl.getText().trim().equalsIgnoreCase("")){
            throw new RuntimeException("Please enter SF security token.");
        }
    }
    
    @FXML
    private void exportManifestAction(ActionEvent event) {
        
        try {
            
            this.checkLoginFields();
            
            if(fromDatePickerControl.getValue()==null || tillDatePickerControl.getValue()==null){
                throw new RuntimeException("Please enter export dates.");
            }
            
            Calendar periodStart = Calendar.getInstance();
            periodStart.set(fromDatePickerControl.getValue().getYear(), fromDatePickerControl.getValue().getMonthValue()-1, fromDatePickerControl.getValue().getDayOfMonth());

            Calendar periodEnd = Calendar.getInstance();
            periodEnd.set(tillDatePickerControl.getValue().getYear(), tillDatePickerControl.getValue().getMonthValue()-1, tillDatePickerControl.getValue().getDayOfMonth());
            
            MetadataConnection metadataConnection = MetadataLoginUtil.login(
                                                                        sfUsernameTextFieldControl.getText(), 
                                                                        sfPasswordPasswordFieldControl.getText()+sfSecurityTokenTextFieldControl.getText()
                                                                    );
            CreateManifestService createManifestService = new CreateManifestService(metadataConnection, periodStart, periodEnd);
            
            List<String> typesList = Arrays.asList(objectTypesTextAreaControl.getText().replace(" ", "").replace("\n", "").split(","));
            
            createManifestService.getlastModifiedComponents(typesList);
            
            statusTextAreaControl.setText("Success!");
            statusTextAreaControl.setStyle(greenStyle);
            
        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: "+(ex==null?" undefined error.": ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }
        
        
    }
    
    @FXML
    private void chooseExportPackageXML(ActionEvent event){
        
        try{
            
            Node source = (Node) event.getSource();
            Window theStage = source.getScene().getWindow();
            
            FileChooser fileChooser = new FileChooser();
            File selectedDirectory = 
                    fileChooser.showOpenDialog(theStage);

            if(selectedDirectory == null){
                packageFilePathTextFieldControl.setText("");
                throw new RuntimeException("No file was selected.");
            }else{
                packageFilePathTextFieldControl.setText(selectedDirectory.getAbsolutePath());
            }
        }catch(Exception ex){
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: "+(ex==null?" undefined error.": ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }
        
    }
    
    @FXML
    private void exportSFObjectsByPackageXML(ActionEvent event){
        
        try{
            
            this.checkLoginFields();
            
            if(packageFilePathTextFieldControl.getText().trim().equalsIgnoreCase("")){
                throw new RuntimeException("Package.xml file is not choosed.");
            }
            
            MetadataConnection metadataConnection = MetadataLoginUtil.login(
                                                                        sfUsernameTextFieldControl.getText(), 
                                                                        sfPasswordPasswordFieldControl.getText()+sfSecurityTokenTextFieldControl.getText()
                                                                    );
            
            RetrieveSample sample = new RetrieveSample(
                                            metadataConnection,
                                            packageFilePathTextFieldControl.getText()
                                        );
            sample.retrieveZip();
            
            statusTextAreaControl.setText("Success!");
            statusTextAreaControl.setStyle(greenStyle);
            
        }catch(Exception ex){
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: "+(ex==null?" undefined error.": ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }
        
        
    }
    
}
