package me.aidengaripoli.dhap;

import org.w3c.dom.Element;

import java.util.ArrayList;

import me.aidengaripoli.dhap.elements.BaseElementFragment;
import me.aidengaripoli.dhap.elements.ButtonGroupFragment;
import me.aidengaripoli.dhap.elements.ButtonToggleFragment;
import me.aidengaripoli.dhap.elements.DirectionalButtonsFragment;
import me.aidengaripoli.dhap.elements.PasswordFragment;
import me.aidengaripoli.dhap.elements.ProgressFragment;
import me.aidengaripoli.dhap.elements.RangeInputFragment;
import me.aidengaripoli.dhap.elements.SchedulerFragment;
import me.aidengaripoli.dhap.elements.SelectionFragment;
import me.aidengaripoli.dhap.elements.StatusFragment;
import me.aidengaripoli.dhap.elements.StepperFragment;
import me.aidengaripoli.dhap.elements.SwitchToggleFragment;
import me.aidengaripoli.dhap.elements.TextInputFragment;

public class ElementFactory {
    static BaseElementFragment getElement(Element element) {
        DeviceDescriptionParser xmlParser = new DeviceDescriptionParser();
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
