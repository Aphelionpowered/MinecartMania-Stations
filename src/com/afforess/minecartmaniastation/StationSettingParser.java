package com.afforess.minecartmaniastation;

import java.io.File;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.afforess.minecartmaniacore.config.MinecartManiaConfigurationParser;
import com.afforess.minecartmaniacore.config.SettingParser;
import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public class StationSettingParser implements SettingParser {
    private static final double version = 1.0;
    private static MinecartManiaLogger log = MinecartManiaLogger.getInstance();
    
    public boolean isUpToDate(final Document document) {
        try {
            final NodeList list = document.getElementsByTagName("version");
            final Double version = MinecartManiaConfigurationParser.toDouble(list.item(0).getChildNodes().item(0).getNodeValue(), 0);
            log.debug("Station Config read: version: " + list.item(0).getTextContent());
            if (version == 1.0) {
                //Place the code to update to the next version here
                //version = 1.1;	//This needs to be updated to the next version of the document.
                //list.item(0).setTextContent(version.toString());
            }
            return version == StationSettingParser.version;
        } catch (final Exception e) {
            return false;
        }
    }
    
    public boolean read(final Document document) {
        //Set the default configuration before we try to read anything.
        setDefaultConfiguration();
        
        NodeList list;
        try {
            list = document.getElementsByTagName("MinecartManiaConfiguration").item(0).getChildNodes(); //get the root nodes of the ConfigurationTree
            String elementChildName = ""; //holds the name of the node
            String elementChildValue = ""; //holds the value of the node
            //loop through each of the child nodes of the document
            for (int idx = 0; idx < list.getLength(); idx++) {
                final Node elementChild = list.item(idx); //extract the node
                elementChildName = ""; //reset the child name
                elementChildValue = null; //reset the child value
                //do we have a valid element node
                if (elementChild.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildName = elementChild.getNodeName(); //get the node name
                    elementChildValue = elementChild.getTextContent(); //get the node value
                    if ((elementChildValue != null) && (elementChildValue != "")) {
                        //Handle the possible nodes we have at this level.
                        if (elementChildName == "version") {
                            if (elementChildValue != String.valueOf(version)) { /* documentUpgrade(document); */
                            }
                        } else if ((elementChildName == "IntersectionPrompts") || (elementChildName == "StationSignParsingMethod")) {
                            MinecartManiaWorld.getConfiguration().put(elementChildName, MinecartManiaConfigurationParser.toInt(elementChildValue, getDefaultConfigurationIntegerValue(elementChildName)));
                            log.debug("Station Config read: " + elementChildName + " = " + elementChildValue);
                        } else if (elementChildName == "StationCommandSavesAfterUse") {
                            MinecartManiaWorld.getConfiguration().put(elementChildName, MinecartManiaConfigurationParser.toBool(elementChildValue));
                            log.debug("Station Config read: " + elementChildName + " = " + elementChildValue);
                        } else {
                            log.info("Station Config read unknown node: " + elementChildName);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            return false;
        }
        debugShowConfigs();
        return true;
    }
    
    private void setDefaultConfiguration() {
        //Create the default Configuration values
        MinecartManiaWorld.getConfiguration().put("IntersectionPrompts", getDefaultConfigurationIntegerValue("IntersectionPrompts"));
        MinecartManiaWorld.getConfiguration().put("StationSignParsingMethod", getDefaultConfigurationIntegerValue("StationSignParsingMethod"));
        MinecartManiaWorld.getConfiguration().put("StationCommandSavesAfterUse", false);
    }
    
    private int getDefaultConfigurationIntegerValue(final String ConfigName) {
        if (ConfigName == "IntersectionPrompts")
            return (0);
        if (ConfigName == "StationSignParsingMethod")
            return (0);
        return 0;
    }
    
    private void debugShowConfigs() {
        //Display global configuration values
        for (final Enumeration<String> ConfigKeys = MinecartManiaWorld.getConfiguration().keys(); ConfigKeys.hasMoreElements();) {
            final String temp = ConfigKeys.nextElement();
            final String value = MinecartManiaWorld.getConfigurationValue(temp).toString();
            if ((temp == "IntersectionPrompts") || (temp == "StationSignParsingMethod") || (temp == "StationCommandSavesAfterUse")) {
                log.debug("Station Config: " + temp + " = " + value);
            }
        }
    }
    
    public boolean write(final File configuration, Document document) {
        try {
            if (document == null) {
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                //root elements
                document = docBuilder.newDocument();
                document.setXmlStandalone(true);
                final Element rootElement = document.createElement("MinecartManiaConfiguration");
                document.appendChild(rootElement);
                
                Element setting = document.createElement("version");
                setting.appendChild(document.createTextNode(String.valueOf(version)));
                rootElement.appendChild(setting);
                
                setting = document.createElement("IntersectionPrompts");
                Comment comment = document.createComment("If set to 0, Will prompt users for their intended direction when a player reaches an intersection. If set to 1, will prompt users only if the intersection has a station block underneath. If set to 2 players will never be prompted at intersections.");
                setting.appendChild(document.createTextNode("0"));
                rootElement.appendChild(setting);
                rootElement.insertBefore(comment, setting);
                
                setting = document.createElement("StationSignParsingMethod");
                comment = document.createComment("0 - simple parsing with no pattern matching. 1 - simple pattern matching. 2 - full regex parsing.");
                setting.appendChild(document.createTextNode("0"));
                rootElement.appendChild(setting);
                rootElement.insertBefore(comment, setting);
                
                setting = document.createElement("StationCommandSavesAfterUse");
                comment = document.createComment("After passing one intersection, the /st command will not be cleared.");
                setting.appendChild(document.createTextNode("false"));
                rootElement.appendChild(setting);
                rootElement.insertBefore(comment, setting);
            }
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            final DOMSource source = new DOMSource(document);
            final StreamResult result = new StreamResult(configuration);
            transformer.transform(source, result);
        } catch (final Exception e) {
            return false;
        }
        return true;
    }
}
