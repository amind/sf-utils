package amind.sf.utils.export;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ProfileFieldLevelSecurity;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLFileBuilder {

    public void createPackageXml(Map<String, List<FileProperties>> props) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Package");
            doc.appendChild(rootElement);

            // type elements
            for (Map.Entry<String, List<FileProperties>> prop : props.entrySet()) {
                Element types = doc.createElement("types");
                rootElement.appendChild(types);

                prop.getValue().forEach(component->setNode(types, doc, component.getFullName(), "members"));

                setNode(types, doc, prop.getKey(), "name");
            }

            setNode(rootElement, doc, "36.0", "version");

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("package.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public void createProfilePackageXml(List<ProfileFieldLevelSecurity> fieldSecurity) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Package");
            doc.appendChild(rootElement);

            // type elements
            Element types = doc.createElement("types");
            rootElement.appendChild(types);


            fieldSecurity.forEach(field->setNode(types, doc, field.getField(), "members"));

            setNode(types, doc, "CustomField", "name");

            setNode(rootElement, doc, "36.0", "version");

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("package1.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    private void setNode(Element type, Document doc, String textNode, String tagNmae){
        Element member = doc.createElement(tagNmae);
        member.appendChild(doc.createTextNode(textNode));
        type.appendChild(member);
    }
}
