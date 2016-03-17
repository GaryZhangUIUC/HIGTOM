package edu.illinois.cs.forward.modelers;

import edu.illinois.cs.forward.types.Location;
import edu.illinois.cs.forward.types.Node;

import java.util.Map;

/**
 * An estimator to calculate the probabilities for language modeling for the tree nodes.
 */
public class GMEstimator {
    // imaginary number of coming instances
    public double kappa;
    double[] smoothingVariance4Levels;

    double boundaryFactor;

    public GMEstimator(double kappa, double[] smoothingVariance4Levels, double boundaryFactor) {
        this.kappa = kappa;
        this.smoothingVariance4Levels = smoothingVariance4Levels;
        this.boundaryFactor = boundaryFactor;
    }

    public void updateChildLikelihoods(
            Node parent, Map<Node, Double> childLikelihoods,
            Location target) {
        int childLevel = parent.level + 1;
        for (Node child: parent.children) {
            double oldLikelihood = childLikelihoods.getOrDefault(child, 0.0);
            double geographicProbability = smoothedGaussianProbability(
                    child.location, child.numCustomers, target, childLevel);
            childLikelihoods.put(
                    child,
                    oldLikelihood + Math.log(geographicProbability));
        }
        double oldLikelihood = childLikelihoods.getOrDefault(null, 0.0);
        double geographicProbability = uniformProbability(childLevel);
        childLikelihoods.put(
                null,
                oldLikelihood + Math.log(geographicProbability));
    }

    /**
     * Estimate the geographical probabilities and update in place the likelihoods for the tree nodes.
     * This is the absolute probability way.
     */
    public void updateLikelihoodsAbsolute(
            Map<Node, Double> likelihoods, Node root,
            Location target, double[] levelWeights) {
        double[] newLikelihoods = new double[levelWeights.length];
        newLikelihoods[levelWeights.length - 1] = 0.0;
        for (int level = levelWeights.length - 2; level >= 0; level--) {
            double geographicProbability = uniformProbability(level + 1);
            double geographicLikelihood = Math.log(geographicProbability);
            newLikelihoods[level] = newLikelihoods[level + 1] + levelWeights[level + 1] * geographicLikelihood;
        }

        updateLikelihoodsAbsolute(likelihoods, root, 0.0, target, levelWeights,
                newLikelihoods);
    }

    public void updateLikelihoodsAbsolute(
            Map<Node, Double> likelihoods, Node currentNode, double likelihoodOffset,
            Location target, double[] levelWeights,
            double[] newLikelihoods) {
        double oldProb = likelihoods.getOrDefault(currentNode, 0.0);

        int level = currentNode.level;

        double geographicProbability = smoothedGaussianProbability(
                currentNode.location, currentNode.numCustomers, target, level);

        double geographicLikelihood = levelWeights[currentNode.level] * Math.log(geographicProbability);

        for (Node child: currentNode.children) {
            updateLikelihoodsAbsolute(
                    likelihoods, child, likelihoodOffset + geographicLikelihood,
                    target, levelWeights,
                    newLikelihoods);
        }

        likelihoods.put(currentNode, oldProb + likelihoodOffset + geographicLikelihood + newLikelihoods[level]);
    }

    /**
     * Estimate the geographical probabilities and update in place the likelihoods for the tree nodes.
     * This is the chained probability way.
     */
    public void updateLikelihoodsChained(
            Map<Node, Double> likelihoods, Node root,
            Location target) {
        int numLevels = smoothingVariance4Levels.length;
        double[] newLikelihoods = new double[numLevels];
        newLikelihoods[numLevels - 1] = 0.0;
        for (int level = numLevels - 2; level >= 0; level--) {
            double geographicProbability = uniformProbability(level + 1);

            double geographicLikelihood = Math.log(geographicProbability);

            newLikelihoods[level] = newLikelihoods[level + 1] + geographicLikelihood;
        }


        updateLikelihoodsChained(likelihoods, root, 0.0, target, newLikelihoods);
    }

    public void updateLikelihoodsChained(
            Map<Node, Double> likelihoods, Node currentNode, double likelihoodOffset,
            Location target, double[] newLikelihoods) {
        double oldProb = likelihoods.getOrDefault(currentNode, 0.0);

        int level = currentNode.level;

        for (Node child: currentNode.children) {
            double geographicProbability = smoothedGaussianProbability(
                    currentNode.location, currentNode.numCustomers, child.location, level);

            double geographicLikelihood = Math.log(geographicProbability);

            updateLikelihoodsChained(
                    likelihoods, child, likelihoodOffset + geographicLikelihood,
                    target, newLikelihoods);
        }

        double geographicProbability = smoothedGaussianProbability(
                currentNode.location, currentNode.numCustomers, target, level);

        double geographicLikelihood = Math.log(geographicProbability);

        if (Double.isNaN(geographicLikelihood)) {
            System.err.println("Encountered an NaN geographic probability.");
        }

        likelihoods.put(currentNode, oldProb + likelihoodOffset + geographicLikelihood + newLikelihoods[level]);
    }

    /**
     * Using Pdf to represent probability here.
     * The reason being that probability = pdf * A for a constant sampling area.
     */
    public double smoothedGaussianProbability(
            Location source, int numSourceInstances,
            Location target, int level) {
        double smoothingVariance = smoothingVariance4Levels[level];
        double longitudeVariance = (source.longitudeVariance * numSourceInstances + smoothingVariance * kappa) /
                (numSourceInstances + kappa);
        double latitudeVariance = (source.latitudeVariance * numSourceInstances + smoothingVariance * kappa) /
                (numSourceInstances + kappa);
        double longitudeLatitudeCorrelation = source.longitudeLatitudeCovariance * numSourceInstances /
                (numSourceInstances + kappa) / Math.sqrt(longitudeVariance * latitudeVariance);

        double normalizedDeviation =
                (target.longitude - source.longitude) * (target.longitude - source.longitude) /
                        longitudeVariance +
                (target.latitude - source.latitude) * (target.latitude - source.latitude) /
                        latitudeVariance -
                2 * longitudeLatitudeCorrelation *
                        (target.longitude - source.longitude) * (target.latitude - source.latitude) /
                        Math.sqrt(longitudeVariance * latitudeVariance);
        double exponentialPower = -normalizedDeviation /
                (2.0 * (1 - longitudeLatitudeCorrelation * longitudeLatitudeCorrelation));
        return Math.exp(exponentialPower) / (2 * Math.PI) /
                Math.sqrt(longitudeVariance * latitudeVariance *
                        (1 - longitudeLatitudeCorrelation * longitudeLatitudeCorrelation));
    }

    public double uniformProbability(int level) {
        return 0.25 / (boundaryFactor * boundaryFactor * smoothingVariance4Levels[level]);
    }
}
