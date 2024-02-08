package com.braunclown.kortiiko.services.iiko;

public class GroupPrototype {
    private String name;
    private String iikoId;
    private String parentIikoId;

    public GroupPrototype() {
    }

    public GroupPrototype(String name, String iikoId, String parentIikoId) {
        this.name = name;
        this.iikoId = iikoId;
        this.parentIikoId = parentIikoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIikoId() {
        return iikoId;
    }

    public void setIikoId(String iikoId) {
        this.iikoId = iikoId;
    }

    public String getParentIikoId() {
        return parentIikoId;
    }

    public void setParentIikoId(String parentIikoId) {
        this.parentIikoId = parentIikoId;
    }
}
