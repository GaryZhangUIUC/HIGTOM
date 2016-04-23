package edu.illinois.cs.forward.modelers;

import edu.illinois.cs.forward.types.Node;

import java.util.Map;
import java.util.Random;

/**
 * An estimator to calculate the probabilities for Nest Chinese Restaurant Process for the tree nodes.
 */
public class NCRPEstimator {
    // "imaginary" customers for estimating opening a new table
    double gamma;

    public NCRPEstimator(double gamma) {
        this.gamma = gamma;
    }

    /**
     * Estimate the NCRP probabilities and update in place the likelihoods for the tree nodes.
     */
    public void updateLikelihoods(Map<Node, Double> likelihoods, Node root) {
        updateLikelihoods(likelihoods, root, 0.0);
    }

    public void updateLikelihoods(Map<Node, Double> likelihoods, Node currentNode, double likelihoodOffset) {
        double oldProb = likelihoods.getOrDefault(currentNode, 0.0);

        if (currentNode.isLeaf()) {
            likelihoods.put(currentNode, oldProb + likelihoodOffset);
        } else {
            likelihoods.put(
                    currentNode,
                    oldProb + likelihoodOffset + Math.log(gamma / (currentNode.numCustomers + gamma)));
        }

        for (Node child: currentNode.children) {
            updateLikelihoods(likelihoods, child,
                    likelihoodOffset + Math.log((double)child.numCustomers / (currentNode.numCustomers + gamma)));
        }
    }

    public void updateChildLikelihoods(Node parent, Map<Node, Double> childLikelihoods) {
        for (Node child: parent.children) {
            double oldLikelihood = childLikelihoods.getOrDefault(child, 0.0);
            double ncrpProbability = (double)child.numCustomers / (gamma + parent.numCustomers);
            childLikelihoods.put(child, oldLikelihood + Math.log(ncrpProbability));
        }
        double oldLikelihood = childLikelihoods.getOrDefault(null, 0.0);
        double ncrpProbability = gamma / (gamma + parent.numCustomers);
        childLikelihoods.put(null, oldLikelihood + Math.log(ncrpProbability));
    }
}
