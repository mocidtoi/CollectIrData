package com.thanhnv.collectirdata;

/**
 * Created by thanhnv on 05/10/2016.
 */

public class ItemIrData {
    private int id;
    private String deviceType, model, name, irData, label, icon;

    public ItemIrData(int id, String deviceType, String model, String name, String irData, String label, String icon) {
        this.id = id;
        this.deviceType = deviceType;
        this.model = model;
        this.name = name;
        this.irData = irData;
        this.label = label;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIrData() {
        return irData;
    }

    public void setIrData(String irData) {
        this.irData = irData;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
