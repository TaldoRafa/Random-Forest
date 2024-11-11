package dev.rafarg;

import java.util.Map;

public class ObjectData {
    Map<String, String> attributes;
    String classification;

    public ObjectData(Map<String, String> attributes, String classification) {
        this.attributes = attributes;
        this.classification = classification;
    }
}