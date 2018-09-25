package amind.sf.utils;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.enterprise.PackageVersion;
import com.sforce.soap.enterprise.fault.LoginFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Login utility.
 */
public class MetadataLoginUtil {

    public static MetadataConnection login(String username, String password, String loginUrl) throws ConnectionException {
        try {
            final String URL = loginUrl==null? "https://test.salesforce.com/services/Soap/c/41.0": 
                    "https://"+loginUrl+"/services/Soap/c/41.0";
            final LoginResult loginResult = loginToSalesforce(username, password, URL);
            return createMetadataConnection(loginResult);
        } catch (LoginFault exp) {
            throw new RuntimeException(exp.getExceptionMessage());
        }
    }

    private static MetadataConnection createMetadataConnection(
            final LoginResult loginResult) throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setServiceEndpoint(loginResult.getMetadataServerUrl());
        config.setSessionId(loginResult.getSessionId());
        return new MetadataConnection(config);
    }

    private static LoginResult loginToSalesforce(
            final String username,
            final String password,
            final String loginUrl) throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(loginUrl);
        config.setServiceEndpoint(loginUrl);
        config.setManualLogin(true);
        return (new EnterpriseConnection(config)).login(username, password);
    }
}
