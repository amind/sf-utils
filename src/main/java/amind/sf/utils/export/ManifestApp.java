package amind.sf.utils.export;

import amind.sf.utils.MetadataLoginUtil;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManifestApp {

    private static final int MegaBytes = 10241024;

    public static void main__(String[] args) {

        final String username = "username";
        final String password = "password";

        MetadataConnection metadataConnection;
        try {
            metadataConnection = MetadataLoginUtil.login(username, password);

            Calendar periodStart = Calendar.getInstance();
            periodStart.set(2017, 1, 1);

            Calendar periodEnd = Calendar.getInstance();
            periodEnd.set(2018, 2, 10);

            CreateManifestService createManifestService = new CreateManifestService(metadataConnection, periodStart, periodEnd);
            //createManifestService.getlastModifiedComponents();
            List<FileProperties> lastModifiedMetadata = createManifestService.getLastModifiedMetadata("Layout");

            lastModifiedMetadata.forEach(mtd -> {

                System.out.println(mtd.getFullName());
                System.out.println(mtd.getFileName());
                System.out.println(mtd.getNamespacePrefix());
            });

        } catch (ConnectionException ex) {
            Logger.getLogger(ManifestApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
