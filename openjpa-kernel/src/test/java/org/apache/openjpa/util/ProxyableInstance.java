package org.apache.openjpa.util;

public class ProxyableInstance {
    private String dummy;

    public ProxyableInstance() {
        this.dummy = "Hello World";
    }

    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }
}