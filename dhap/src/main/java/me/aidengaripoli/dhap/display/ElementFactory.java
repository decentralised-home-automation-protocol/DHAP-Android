package me.aidengaripoli.dhap.display;

import org.w3c.dom.Element;

import java.util.ArrayList;

import me.aidengaripoli.dhap.display.elements.BaseElementFragment;
import me.aidengaripoli.dhap.display.elements.ButtonGroupFragment;
import me.aidengaripoli.dhap.display.elements.ButtonToggleFragment;
import me.aidengaripoli.dhap.display.elements.DirectionalButtonsFragment;
import me.aidengaripoli.dhap.display.elements.PasswordFragment;
import me.aidengaripoli.dhap.display.elements.ProgressFragment;
import me.aidengaripoli.dhap.display.elements.RangeInputFragment;
import me.aidengaripoli.dhap.display.elements.SchedulerFragment;
import me.aidengaripoli.dhap.display.elements.SelectionFragment;
import me.aidengaripoli.dhap.display.elements.StatusFragment;
import me.aidengaripoli.dhap.display.elements.StepperFragment;
import me.aidengaripoli.dhap.display.elements.SwitchToggleFragment;
import me.aidengaripoli.dhap.display.elements.TextInputFragment;

class ElementFactory {
    static BaseElementFragment getElement(Element element) {
        XmlParser xmlParser = new XmlParser();
        ArrayList<String> displaySettings = xmlParser.getDisplaySettings(element);
        String type = xmlParser.getElementType(element);

        switch (type) {
            case ButtonToggleFragment.BUTTON_TOGGLE: {
                return ButtonToggleFragment.newInstance(displaySettings);
            }

            case ProgressFragment.PROGRESS: {
                return ProgressFragment.newInstance(displaySettings);
            }

            case SelectionFragment.SELECTION: {
                return SelectionFragment.newInstance(displaySettings);
            }

            case RangeInputFragment.RANGE_INPUT: {
                return RangeInputFragment.newInstance(displaySettings);
            }

            case StepperFragment.STEPPER: {
                return StepperFragment.newInstance(displaySettings);
            }

            case DirectionalButtonsFragment.DIRECTIONAL_BUTTONS: {
                return DirectionalButtonsFragment.newInstance(displaySettings);
            }

            case SwitchToggleFragment.SWITCH_TOGGLE: {
                return SwitchToggleFragment.newInstance(displaySettings);
            }

            case StatusFragment.STATUS: {
                return StatusFragment.newInstance(displaySettings);
            }

            case TextInputFragment.TEXT_INPUT: {
                return TextInputFragment.newInstance(displaySettings);
            }

            case ButtonGroupFragment.BUTTON_GROUP: {
                return ButtonGroupFragment.newInstance(displaySettings);
            }

            case PasswordFragment.PASSWORD: {
                return PasswordFragment.newInstance(displaySettings);
            }

            case SchedulerFragment.SCHEDULER: {
                return SchedulerFragment.newInstance(displaySettings);
            }

            default: {
                // TODO: handle invalid element type
                return null;
            }
        }
    }
}
