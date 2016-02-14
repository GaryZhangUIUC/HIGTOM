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

    public Model(int numLevels) {
        this.numLevels = numLevels;
        this.root = new Node(null, 0);

        this.word2Id = null;
        this.id2Word = null;
    }

    public void setWordProfile(Map<String, Integer> word2Id, Map<Integer, String> id2Word) {
        this.word2Id = word2Id;
        this.id2Word = id2Word;
    }
}
