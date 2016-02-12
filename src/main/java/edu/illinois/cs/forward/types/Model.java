package edu.illinois.cs.forward.types;

/**
 * Containing the hierarchical geographical tree and some related information.
 */
public class Model {
    public Node root;
    public int numLevels;

    public Model(int numLevels) {
        this.numLevels = numLevels;
        this.root = null;
    }
}
