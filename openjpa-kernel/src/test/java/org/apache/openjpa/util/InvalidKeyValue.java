package org.apache.openjpa.util;

public class InvalidKeyValue {
    @Override
    public int hashCode() {
        return (int) (Math.random() * 1000);
    }
}