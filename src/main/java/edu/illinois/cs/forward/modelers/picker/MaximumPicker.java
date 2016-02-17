package edu.illinois.cs.forward.modelers.picker;

import edu.illinois.cs.forward.types.Node;

import java.util.Map;

/**
 * A picker to pick the maximum value.
 */
public class MaximumPicker extends AbstractPicker {
    @Override
    public Node pickNode(Map<Node, Double> likelihoods) {
        Node pickedNode = null;
        for (Node node: likelihoods.keySet()) {
            if (pickedNode == null || likelihoods.get(node) > likelihoods.get(pickedNode)) {
                pickedNode = node;
            }
        }
        return pickedNode;
    }

    @Override
    public int pickLevel(double[] levelWeights) {
        int pickedLevel = 0;
        for (int level = 1; level < levelWeights.length; level++) {
            if (levelWeights[level] > levelWeights[pickedLevel]) {
                pickedLevel = level;
            }
        }
        return pickedLevel;
    }
}
