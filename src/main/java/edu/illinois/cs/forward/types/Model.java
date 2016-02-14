package edu.illinois.cs.forward.types;

import java.util.Map;

/**
 * Containing the hierarchical geographical tree and some related information.
 */
public class Model {
    public Node root;
    public int numLevels;

    public Map<String, Integer> word2Id;
    public Map<Integer, String> id2Word;
    public int nextId;

    public Model(int numLevels) {
        this.numLevels = numLevels;
        this.root = null;

        this.word2Id = null;
        this.id2Word = null;
        this.nextId = -1;
    }
}
