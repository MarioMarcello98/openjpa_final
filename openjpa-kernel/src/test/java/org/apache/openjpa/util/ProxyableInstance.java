package org.apache.openjpa.util;

public class ProxyableInstance {
    private String state;

    public ProxyableInstance() {
        this.state = "Proxyable";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}