package amind.sf.utils.export;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

import java.io.FileNotFoundException;
import java.util.Calendar;

public class ManifestApp {
    private static final int MegaBytes = 10241024;

    public static void main__(String[] args) throws FileNotFoundException, ConnectionException {
        
        final String username = "username";
        final String password = "password";
        
        MetadataConnection metadataConnection = MetadataLoginUtil.login(username, password);
        
        Calendar periodStart = Calendar.getInstance();
        periodStart.set(2017, 9, 28);

        Calendar periodEnd = Calendar.getInstance();
        periodEnd.set(2017, 6, 13);
        
        CreateManifestService createManifestService = new CreateManifestService(metadataConnection, periodStart, periodEnd);
        createManifestService.getlastModifiedComponents();
        
    }
}
