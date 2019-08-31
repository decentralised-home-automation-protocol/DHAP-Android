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

public class DeviceDescriptionLayout {

    private static final String TAG = DeviceDescriptionLayout.class.getSimpleName();

    private static DeviceDescriptionParser parser = new DeviceDescriptionParser();

    private FragmentManager fragmentManager;
    private Context context;

    private HashMap<String, BaseElementFragment> elements;

    public DeviceDescriptionLayout(FragmentManager fragmentManager, Context context) {
        this.fragmentManager = fragmentManager;
        this.context = context;
        elements = new HashMap<>();
    }

    public ViewGroup create(DeviceDescription description) {
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setId(View.generateViewId());
        rootLayout.setGravity(Gravity.CENTER);

        try {
            NodeList groupNodeList = parser.getGroups(description.getXml());

            if (groupNodeList == null) {
                return rootLayout;
            }

            addTitle(groupNodeList, rootLayout);

            // iterate through all the <group> elements
            for (int i = 1; i < groupNodeList.getLength(); i++) {
                Element element = (Element) groupNodeList.item(i);

                String groupId = parser.getId(element);
                NodeList guiNodeList = parser.getGuiElementsInGroup(element);

                LinearLayout groupLayout = createLinearLayout(parser.getGroupLayoutOrientation(element));

                if (guiNodeList.getLength() == 1) {
                    addElementToLayout((Element) guiNodeList.item(0), groupLayout, groupId);
                } else {
                    createGroupOfElements(groupLayout, guiNodeList, groupId);
                }

                if(parser.doesGroupHaveBorderAttribute(element)){
                    groupLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.border));
                }

                rootLayout.addView(groupLayout);
            }

        } catch (Exception e) {
            Log.e(TAG, "ERROR WHILST GENERATING UI: " + e.getMessage());
        }

        return rootLayout;
    }

    private void addTitle(NodeList groupNodeList, LinearLayout rootLayout) {
        Element groupElement = (Element) groupNodeList.item(0);

        String name = parser.getName(groupElement);

        TextView title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        title.setText(name);

        LinearLayout groupLayout = createLinearLayout(true);
        groupLayout.addView(title);
        rootLayout.addView(groupLayout);
    }

    private void createGroupOfElements(LinearLayout groupLayout, NodeList guiNodeList, String groupId) {
        // iterate through all the <gui_element> elements in the group
        for (int i = 0; i < guiNodeList.getLength(); i++) {
            Element element = (Element) guiNodeList.item(i);
            addElementToLayout(element, groupLayout, groupId);
        }
    }

    private void addElementToLayout(Element element, LinearLayout layout, String groupId) {
        // generate a view (widget) for each gui_element
        BaseElementFragment fragment = ElementFactory.getElement(element);

        String fragmentTag = groupId + "-" + parser.getId(element);

        // add the view to the groups layout
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.add(layout.getId(), fragment, fragmentTag);
            fragmentTransaction.commit();

            elements.put(fragmentTag, fragment);
        }
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
