package ai.automation.model;


import ai.automation.enums.Status;

import java.util.ArrayList;

import java.util.List;

public class LocatorDetails {

    private String elementLabel = "";

    private String elementHTML = "";

    private String elementXpath = "";

    private String elementInputValue = "";

    private Status status = Status.YETTOSTART;

    private boolean isNeeded = false;

    private String applicationURL = "";

    private String action = "click";

    private String customPrompt = "";


    private List lisOfXpath = new ArrayList<>();


    public String getCustomPrompt() {
        return customPrompt;
    }

    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }

    public String getElementLabel() {
        return elementLabel;
    }

    public void setElementLabel(String elementLabel) {
        this.elementLabel = elementLabel;
    }

    public String getElementHTML() {
        return elementHTML;

    }

    public void setElementHTML(String elementHTML) {
        this.elementHTML = elementHTML;
    }

    public String getElementXpath() {
        return elementXpath;
    }

    public void setElementXpath(String elementXpath) {
        this.elementXpath = elementXpath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isNeeded() {
        return isNeeded;
    }

    public void setNeeded(boolean needed) {
        isNeeded = needed;
    }

    public String getApplicationURL() {
        return applicationURL;
    }

    public void setApplicationURL(String applicationURL) {
        this.applicationURL = applicationURL;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getElementInputValue() {
        return elementInputValue;
    }


    public void setElementInputValue(String elementInputValue) {
        this.elementInputValue = elementInputValue;
    }


    public List getLisOfXpath() {

        return lisOfXpath;

    }

    public void setLisOfXpath(List lisOfXpath) {

        this.lisOfXpath = lisOfXpath;
    }
}
