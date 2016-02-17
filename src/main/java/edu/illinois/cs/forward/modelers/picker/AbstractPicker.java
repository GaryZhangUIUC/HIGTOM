package edu.illinois.cs.forward.modelers.picker;

import edu.illinois.cs.forward.types.Node;

import java.util.Map;

/**
 * class to pick a candidate based on some information.
 */
public abstract class AbstractPicker {
    public abstract Node pickNode(Map<Node, Double> likelihoods);
    public abstract int pickLevel(double[] levelWeights);
}
