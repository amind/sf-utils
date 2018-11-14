package amind.sf.utils.export;

import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author acdc
 */
public class CreateSFPackageXMLUtils {

    public static final int SOAP_VERSION = 42;

    public static FileProperties[] getSFTypeMetadataList(String type, MetadataConnection metadataConnection) throws ConnectionException {
        ListMetadataQuery query = new ListMetadataQuery();
        query.setType(type);
        return metadataConnection.listMetadata(new ListMetadataQuery[]{query}, SOAP_VERSION);
    }

    public static FileProperties[] getAllTemplatesList(MetadataConnection metadataConnection) throws ConnectionException {
        List<FileProperties> emailTemplates = new ArrayList<>();

        ListMetadataQuery query = new ListMetadataQuery();
        query.setType("EmailTemplate");
        FileProperties[] lmr = metadataConnection.listMetadata(new ListMetadataQuery[]{query}, SOAP_VERSION);

        for (FileProperties fp : lmr) {
            emailTemplates.add(fp);
        }

        query.setType("EmailFolder");
        lmr = metadataConnection.listMetadata(new ListMetadataQuery[]{query}, SOAP_VERSION);

        for (FileProperties fp : lmr) {
            query.setType("EmailTemplate");
            query.setFolder(fp.getFullName());

            FileProperties[] lmr1 = metadataConnection.listMetadata(new ListMetadataQuery[]{query}, SOAP_VERSION);
            for (FileProperties fp1 : lmr1) {
                emailTemplates.add(fp1);
            }
        }

        return emailTemplates.toArray(lmr);

    }

}
