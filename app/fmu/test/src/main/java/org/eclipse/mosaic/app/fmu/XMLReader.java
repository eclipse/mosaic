package org.eclipse.mosaic.app.fmu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;

public class XMLReader {
    public Hashtable<String, Hashtable<String, String>> fmuVars;

    private static Document doc;

    public static void main(String[] args){
        String PATH_FMU_CONFIG = "app/fmu/test/config/fmu_config.xml";
        new XMLReader(PATH_FMU_CONFIG);
    }

    XMLReader(String configPath){
        doc = readConfig(configPath);

        Hashtable<String, Hashtable<String, String>> varOut = new Hashtable<>();
        varOut.put("inputVars", childrenToDict("fmuIn"));
        varOut.put("outputVars", childrenToDict("fmuOut"));
        varOut.put("path", getValuesByName(new String[] {"fmuPathAbs", "fmuPathRel"}));

        this.fmuVars = varOut;
//        System.out.println(this.fmuVars);
    }

    private static Document readConfig(String configPath){
        File f = new File(configPath);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;

        try{
            db = dbf.newDocumentBuilder();
            doc = db.parse(f);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }

        Objects.requireNonNull(doc, "Path not found: " + configPath);
        doc.getDocumentElement().normalize();

        return doc;
    }

    private static Hashtable<String, String> childrenToDict(String elementName){
        Hashtable<String, String> retHt = new Hashtable<>();

        Node elementToActOn = doc.getElementsByTagName(elementName).item(0);
        NodeList elementChildren = elementToActOn.getChildNodes();

        for(int i = 1; i < elementChildren.getLength(); i += 2){
            Node currentVar = elementChildren.item(i);
            if(currentVar.getNodeType() == Node.ELEMENT_NODE && currentVar.getNodeName() != null){
                retHt.put(currentVar.getNodeName(), currentVar.getTextContent());
            }
        }
        return retHt;
    }

    private static Hashtable<String, String> getValuesByName(String[] varNames){
        Hashtable<String, String> retHt = new Hashtable<>();

        for(String varName : varNames){
            String val = doc.getElementsByTagName(varName).item(0).getTextContent();
            retHt.put(varName, val);
        }

        return retHt;
    }
}

