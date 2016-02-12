package edu.illinois.cs.forward.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representing an area in the hierarchical structure.
 */
public class Node {
    public Node parent;
    public List<Node> children;

    public int level;

    public int numCustomers;

    public int numWords;
    public Map<Integer, Integer> wordCounts;

    public Location location;

    public Node(Node parent, int level) {
        this.parent = parent;
        this.level = level;

        this.children = new ArrayList<Node>();
        this.numCustomers = 0;
        this.numWords = 0;
        this.wordCounts = new HashMap<Integer, Integer>();
        this.location = null;
    }
}
