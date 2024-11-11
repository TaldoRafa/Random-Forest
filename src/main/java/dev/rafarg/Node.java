package dev.rafarg;

import java.util.HashMap;
import java.util.Map;

public class Node {
    String attribute;
    String value;
    boolean isLeaf;
    String prediction;
    Map<String, Node> children;

    public Node() {
        this.children = new HashMap<>();
    }
}