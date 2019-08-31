package me.aidengaripoli.dhap.display;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DeviceDescriptionParser {

    private final String TYPE = "type";
    private final String DISPLAY_SETTINGS = "disp_settings";
    private final String LABEL = "label";
    private final String NAME = "name";
    private final String ID = "id";
    private final String GROUP = "group";
    private final String GUI_ELEMENT = "gui_element";
    private final String DELIM = ",";


    /**
     * Used to retrieve the string found inside the <Type/> tag.
     * Will return the string in the first <Type/> tag if multiple are specified.
     *
     * @param element Parameter 1.
     * @return A string with the type of element.
     */
    String getElementType(Element element) {
        String value = getTagData(element, TYPE);
        return value.toLowerCase().trim();
    }

    /**
     * Used to retrieve the comma separated values in the <disp_settings/> tag.
     *
     * @param element Parameter 1.
     * @return An Arraylist of the comma separated data found in the display settings tag.
     */
    ArrayList<String> getDisplaySettings(Element element) {
        ArrayList<String> displaySettings = new ArrayList<>();
        String value = getTagData(element, DISPLAY_SETTINGS);

        if (value == null)
            return displaySettings; //No display settings found for this element

        StringTokenizer st = new StringTokenizer(value, DELIM);
        while (st.hasMoreTokens()) {
            displaySettings.add(st.nextToken());
        }

        return displaySettings;
    }

    /**
     * Used to retrieve the value in the <label/> tag.
     *
     * @param element Parameter 1.
     * @return A String with the value in the label tag.
     */
    String getLabel(Element element) {
        return getTagData(element, LABEL);
    }

    /**
     * Used to retrieve the value in the <name/> tag.
     *
     * @param element Parameter 1.
     * @return A String with the value in the name tag.
     */
    String getName(Element element) {
        return getTagData(element, NAME);
    }

    /**
     * Used to retrieve the name of an element from an input stream of an xml file.
     *
     * @param file Parameter 1.
     * @return A String with the value in the name tag.
     */
    public String getDeviceName(String file) {
        NodeList groupsList = getGroups(file);

        if(groupsList != null){
            return getName((Element) groupsList.item(0));
        }
        return "";
    }

    /**
     * Used to retrieve the value in the id attribute.
     *
     * @param element Parameter 1.
     * @return A String with the value in the id attribute.
     */
    String getId(Element element) {
        return element.getAttribute(ID);
    }

    /**
     * Used to retrieve the string found inside a specific tag.
     *
     * @param element Parameter 1.
     * @param tag     Parameter 2.
     * @return A string with the data found inside the tag.
     */
    private String getTagData(Element element, String tag) {
        if (element == null)
            return null;

        NodeList nodeList = element.getElementsByTagName(tag);
        Node node = nodeList.item(0);

        if (node == null)
            return null;

        Node typeNode = node.getFirstChild();

        if (typeNode == null)
            return null;

        return typeNode.getNodeValue();
    }

    /**
     * Used to get all the group nodes in an xml file from and Inputstream.
     *
     * @param xml Parameter 1.
     * @return A Nodelist of each group node.
     */
    NodeList getGroups(String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // get all of the <group> elements
        NodeList groupNodeList = null;
        try {
            if (builder != null) {
                groupNodeList = builder.parse(inputStream).getElementsByTagName(GROUP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return groupNodeList;
    }

    /**
     * Used to retrieve all the guiElements in a group element.
     *
     * @param element Parameter 1.
     * @return A Nodelist of each node called gui_element.
     */
    NodeList getGuiElementsInGroup(Element element) {
        return element.getElementsByTagName(GUI_ELEMENT);
    }


    /**
     * Used to get the widget tag in a status update command.
     *
     * @param command Parameter 1.
     * @return An string of the widgets tag.
     */
    String getCommandTag(String command) {
        StringTokenizer st = new StringTokenizer(command, DELIM);
        return st.nextToken();
    }

    /**
     * Used to get the status data in a status update command.
     *
     * @param command Parameter 1.
     * @return An arrayList of the comma separated status data.
     */
    ArrayList<String> getCommandData(String command) {
        ArrayList<String> statusData = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(command, DELIM);
        st.nextToken();
        while (st.hasMoreTokens()) {
            statusData.add(st.nextToken());
        }

        return statusData;
    }

    boolean doesGroupHaveBorderAttribute(Element element){
        String value = element.getAttribute("frame");

        if(value == null){
            return false;
        }else {
            return value.equals("true");
        }
    }

    boolean getGroupLayoutOrientation(Element element){
        String value = element.getAttribute("orientation");

        if(value == null){
            return false;
        }else {
            return value.equals("horizontal");
        }
    }

}
