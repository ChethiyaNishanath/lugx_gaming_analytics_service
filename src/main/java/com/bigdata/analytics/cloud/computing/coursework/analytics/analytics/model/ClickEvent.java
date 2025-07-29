package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClickEvent extends BaseAnalyticsEvent {
    
    @JsonProperty("click_x")
    private Integer clickX = 0;
    
    @JsonProperty("click_y")
    private Integer clickY = 0;
    
    @JsonProperty("element_id")
    private String elementId = "";
    
    @JsonProperty("element_class")
    private String elementClass = "";
    
    @JsonProperty("element_tag")
    private String elementTag = "";
    
    @JsonProperty("element_text")
    private String elementText = "";
    
    @JsonProperty("click_type")
    private String clickType = "left"; // left, right, middle
    
    @JsonProperty("is_double_click")
    private Boolean isDoubleClick = false;
    
    // Constructors
    public ClickEvent() {
        super();
    }
    
    // Getters and Setters
    public Integer getClickX() { return clickX; }
    public void setClickX(Integer clickX) { this.clickX = clickX; }
    
    public Integer getClickY() { return clickY; }
    public void setClickY(Integer clickY) { this.clickY = clickY; }
    
    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }
    
    public String getElementClass() { return elementClass; }
    public void setElementClass(String elementClass) { this.elementClass = elementClass; }
    
    public String getElementTag() { return elementTag; }
    public void setElementTag(String elementTag) { this.elementTag = elementTag; }
    
    public String getElementText() { return elementText; }
    public void setElementText(String elementText) { this.elementText = elementText; }
    
    public String getClickType() { return clickType; }
    public void setClickType(String clickType) { this.clickType = clickType; }
    
    public Boolean getIsDoubleClick() { return isDoubleClick; }
    public void setIsDoubleClick(Boolean isDoubleClick) { this.isDoubleClick = isDoubleClick; }
}
