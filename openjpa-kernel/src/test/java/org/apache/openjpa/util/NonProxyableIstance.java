package org.apache.openjpa.util;
// classe per simulare un oggetto non proxyabile

public final class NonProxyableIstance {
    private String ID;
    private String state;

    public NonProxyableIstance(String ID, String state) {
        this.ID = ID;
        this.state = state;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}