package amind.sf.utils;

import com.sforce.soap.metadata.MetadataConnection;
import amind.sf.utils.export.CreateManifestService;
import amind.sf.utils.export.MetadataLoginUtil;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController implements Initializable {
    
    @FXML
    private DatePicker fromDatePickerControl;
    
    @FXML
    private DatePicker tillDatePickerControl;
    
    @FXML
    private TextArea statusTextAreaControl;
    
    @FXML
    private TextField sfUsernameTextFieldControl;
    
    @FXML
    private PasswordField sfPasswordPasswordFieldControl;
    
    @FXML
    private TextField sfSecurityTokenTextFieldControl;
    
    private final String redStyle = "-fx-highlight-fill: #ff0000; -fx-highlight-text-fill: #000000; -fx-text-fill: #ff0000; ";
    private final String greenStyle = "-fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ";
    
    @FXML
    private void exportManifestAction(ActionEvent event) {
        
        try {
            
            statusTextAreaControl.setText("In Progress...");
            
            if(sfUsernameTextFieldControl.getText().trim().equalsIgnoreCase("")){
                throw new RuntimeException("Please enter SF username.");
            }
            
            if(sfPasswordPasswordFieldControl.getText().trim().equalsIgnoreCase("")){
                throw new RuntimeException("Please enter SF password.");
            }
            
            if(sfSecurityTokenTextFieldControl.getText().trim().equalsIgnoreCase("")){
                throw new RuntimeException("Please enter SF security token.");
            }
            
            if(fromDatePickerControl.getValue()==null){
                throw new RuntimeException("Please enter export dates.");
            }
            
            
            
            Calendar periodStart = Calendar.getInstance();
            periodStart.set(fromDatePickerControl.getValue().getYear(), fromDatePickerControl.getValue().getMonthValue()-1, fromDatePickerControl.getValue().getDayOfMonth());

            Calendar periodEnd = Calendar.getInstance();
            periodEnd.set(tillDatePickerControl.getValue().getYear(), tillDatePickerControl.getValue().getMonthValue()-1, tillDatePickerControl.getValue().getDayOfMonth());
            
            MetadataConnection metadataConnection = MetadataLoginUtil.login(sfUsernameTextFieldControl.getText(), sfPasswordPasswordFieldControl.getText()+sfSecurityTokenTextFieldControl.getText());
            CreateManifestService createManifestService = new CreateManifestService(metadataConnection, periodStart, periodEnd);
            createManifestService.getlastModifiedComponents();
            
            statusTextAreaControl.setText("Success!");
            statusTextAreaControl.setStyle(greenStyle);
            
        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            statusTextAreaControl.setText("Error occured: "+(ex==null?" undefined error.": ex.getMessage()));
            statusTextAreaControl.setStyle(redStyle);
        }
        
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
