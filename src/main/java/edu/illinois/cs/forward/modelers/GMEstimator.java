package edu.illinois.cs.forward.modelers;

import edu.illinois.cs.forward.types.Location;
import edu.illinois.cs.forward.types.Node;

import java.util.Map;

/**
 * An estimator to calculate the probabilities for language modeling for the tree nodes.
 */
public class GMEstimator {
    // smoothing on geographical distributions
    public double kappa;
    public double uniformProbability;

    public GMEstimator(double kappa, double uniformProbability) {
        this.kappa = kappa;
        this.uniformProbability = uniformProbability;
    }

    /**
     * Estimate the geographical probabilities and update in place the likelihoods for the tree nodes.
     */
    public void updateLikelihoods(
            Map<Node, Double> likelihoods, Node root,
            Location target, double[] levelWeights) {

    }

    public void updateLikelihoods(
            Map<Node, Double> likelihoods, Node currentNode, double likelihoodOffset,
            Location target, double[] levelWeights) {

    }
}
