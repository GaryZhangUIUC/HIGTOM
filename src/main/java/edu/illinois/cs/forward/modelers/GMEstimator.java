package edu.illinois.cs.forward.modelers;

import edu.illinois.cs.forward.types.Location;
import edu.illinois.cs.forward.types.Node;

import java.util.Map;

/**
 * An estimator to calculate the probabilities for language modeling for the tree nodes.
 */
public class GMEstimator {
    // TODO chained probability model
    // TODO use parent or ancestor or sibling variance to smooth or initialize variance
    // TODO fix variance
    // smoothing on geographical distributions
    public double kappa;
    public double uniformPdf;

    // the minimum longitude sd as well as the probability sampling height.
    public double longitudeStep;
    // the minimum latitude sd as well as the probability sampling width.
    public double latitudeStep;

    public GMEstimator(double kappa, double uniformPdf) {
        this.kappa = kappa;
        this.uniformPdf = uniformPdf;

        this.longitudeStep = 0.00001;
        this.latitudeStep = 0.00001;
    }

    /**
     * Create a Geographic Model estimator with a full custom setting.
     * @param kappa smoothing parameter.
     * @param uniformPdf smoothing pdf.
     * @param longitudeStep serves as the minimum longitude sd as well as the probability sampling height.
     * @param latitudeStep serves as the minimum latitude sd as well as the probability sampling width.
     */
    public GMEstimator(double kappa, double uniformPdf, double longitudeStep, double latitudeStep) {
        this.kappa = kappa;
        this.uniformPdf = uniformPdf;

        this.longitudeStep = longitudeStep;
        this.latitudeStep = latitudeStep;
    }

    /**
     * Estimate the geographical probabilities and update in place the likelihoods for the tree nodes.
     * This is the absolute probability way.
     */
    public void updateLikelihoodsAbsolute(
            Map<Node, Double> likelihoods, Node root,
            Location target, double[] levelWeights) {
        updateLikelihoodsAbsolute(likelihoods, root, 0.0, target, levelWeights);
    }

    public void updateLikelihoodsAbsolute(
            Map<Node, Double> likelihoods, Node currentNode, double likelihoodOffset,
            Location target, double[] levelWeights) {
        double oldProb = likelihoods.getOrDefault(currentNode, 0.0);

        double gaussianPdf = gaussianPdf(currentNode.location, target);
        double smoothedPdf = (gaussianPdf * currentNode.numCustomers + uniformPdf * kappa) /
                (currentNode.numCustomers + kappa);

        // Should add Math.log(smoothedPdf) + Math.log(longitudeStep * latitudeStep).
        // However, all path will missed the same amount and thus not needed.
        double geometricLikelihood = levelWeights[currentNode.level] * Math.log(smoothedPdf);

        for (Node child: currentNode.children) {
            updateLikelihoodsAbsolute(
                    likelihoods, child, likelihoodOffset + geometricLikelihood,
                    target, levelWeights);
        }

        for (int newLevel = currentNode.level + 1; newLevel < levelWeights.length; newLevel++) {
            geometricLikelihood += levelWeights[newLevel] * Math.log(uniformPdf);
        }

        likelihoods.put(currentNode, oldProb + likelihoodOffset + geometricLikelihood);
    }

    public double gaussianPdf(Location source, Location target) {
        double longitudeSd = Math.max(Math.sqrt(source.longitudeVariance), longitudeStep);
        double latitudeSd = Math.max(Math.sqrt(source.latitudeVariance), latitudeStep);

        double normalizedDeviation =
                (target.longitude - source.longitude) * (target.longitude - source.longitude) /
                        (longitudeSd * longitudeSd) +
                (target.latitude - source.latitude) * (target.latitude - source.latitude) /
                        (latitudeSd * latitudeSd);
        double exponentialPower = -normalizedDeviation / 2.0;
        double pdf = Math.exp(exponentialPower) / (2 * Math.PI * longitudeSd * latitudeSd);
        return pdf;
    }
}
