package me.aidengaripoli.dhap.display;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;

import me.aidengaripoli.dhap.R;
import me.aidengaripoli.dhap.display.elements.BaseElementFragment;

public class DeviceLayoutBuilder {

    private static final String TAG = DeviceLayoutBuilder.class.getSimpleName();

    private static XmlParser parser = new XmlParser();

    private FragmentManager fragmentManager;
    private Context context;

    private HashMap<String, BaseElementFragment> elements;

    public DeviceLayoutBuilder(FragmentManager fragmentManager, Context context) {
        this.fragmentManager = fragmentManager;
        this.context = context;
        elements = new HashMap<>();
    }

    public ViewGroup create(DeviceLayout description, String deviceName) {
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setId(View.generateViewId());
        rootLayout.setGravity(Gravity.CENTER);

        try {
            NodeList groupNodeList = parser.getGroups(description.getXml());

            if (groupNodeList == null) {
                return rootLayout;
            }

            addTitle(deviceName, rootLayout);

            // iterate through all the <group> elements
            for (int i = 0; i < groupNodeList.getLength(); i++) {
                Element group = (Element) groupNodeList.item(i);

                String groupId = parser.getId(group);

                NodeList guiNodeList = parser.getGuiElementsInGroup(group);

                LinearLayout groupLayout = createLinearLayout(parser.getGroupLayoutOrientation(group));

                if (guiNodeList.getLength() == 1) {
                    addElementToLayout((Element) guiNodeList.item(0), groupLayout, groupId);
                } else {
                    createGroupOfElements(groupLayout, guiNodeList, groupId);
                }

                if (parser.doesGroupHaveBorderAttribute(group)) {
                    groupLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.border));
                }

                rootLayout.addView(groupLayout);
            }

        } catch (Exception e) {
            Log.e(TAG, "ERROR WHILST GENERATING UI: " + e.getMessage());
            return null;
        }

        description.setElements(elements);

        return rootLayout;
    }

    static boolean isValidXml(String xml) {
        try {
            NodeList groupNodeList = parser.getGroups(xml);

            if (groupNodeList == null) {
                Log.e(TAG, "isValidXml: No groups found");
                return false;
            }

            // iterate through all the <group> elements
            for (int i = 0; i < groupNodeList.getLength(); i++) {
                Element group = (Element) groupNodeList.item(i);
                String groupID = parser.getId(group);
                if (groupID == null) {
                    Log.e(TAG, "isValidXml: Group has no ID");
                    return false;
                }

                NodeList elementsInGroup = parser.getGuiElementsInGroup(group);
                if (elementsInGroup == null) {
                    Log.e(TAG, "isValidXml: Group has no elements");
                    return false;
                }

                for (int j = 0; j < elementsInGroup.getLength(); j++) {
                    Element element = (Element) elementsInGroup.item(j);

                    BaseElementFragment fragment = ElementFactory.getElement(element);
                    if (fragment == null) {
                        Log.e(TAG, "isValidXml: element type not found");
                        return false;
                    }

                    String elementID = parser.getId(element);
                    if (elementID == null) {
                        Log.e(TAG, "isValidXml: Element has no ID");
                        return false;
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "error in device xml. " + e.getMessage());
            return false;
        }

        return true;
    }

    private void addTitle(String deviceName, LinearLayout rootLayout) {
        TextView title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        title.setText(deviceName);

        LinearLayout groupLayout = createLinearLayout(true);
        groupLayout.addView(title);
        rootLayout.addView(groupLayout);
    }

    private void createGroupOfElements(LinearLayout groupLayout, NodeList guiNodeList, String groupId) {
        // iterate through all the <gui_element> elements in the name
        for (int i = 0; i < guiNodeList.getLength(); i++) {
            Element element = (Element) guiNodeList.item(i);
            addElementToLayout(element, groupLayout, groupId);
        }
    }

    private void addElementToLayout(Element element, LinearLayout layout, String groupId) {
        // generate a view (widget) for each gui_element
        BaseElementFragment fragment = ElementFactory.getElement(element);

        if (fragment == null) {
            return;
        }

        String elementId = parser.getId(element);
        fragment.setId(groupId + "-" + elementId);

        //Get value in the status_location tag
        String fragmentTag = parser.getStatusLocation(element);

        // add the view to the groups layout
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(layout.getId(), fragment, fragmentTag);
        fragmentTransaction.commit();

        elements.put(fragmentTag, fragment);
    }

    private LinearLayout createLinearLayout(boolean horizontalLayout) {
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.setOrientation(horizontalLayout ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        layout.setLayoutParams(layoutParams);
        layout.setGravity(Gravity.CENTER);
        layout.setId(View.generateViewId());

        return layout;
    }

}
