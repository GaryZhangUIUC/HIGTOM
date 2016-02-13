package edu.illinois.cs.forward.modelers;

/**
 * Utility class to store some helper functions.
 */
public class Util {
    public static double gaussianProbability2d(
            double mean1, double mean2, double variance1, double variance2, double correlation,
            double target1, double target2) {
        double normalizedDeviation = (target1 - mean1) * (target1 - mean1) / variance1 +
                (target2 - mean2) * (target2 - mean2) / variance2 -
                2 * correlation * (target1 - mean1) * (target2 - mean2) / Math.sqrt(variance1 * variance2);
        double exponentialPower = -normalizedDeviation / (2.0 * (1 - correlation * correlation));
        double probability = Math.exp(exponentialPower) / (2 * Math.PI) /
                Math.sqrt(variance1 * variance2 * (1 - correlation * correlation));
        return probability;
    }

    public static double gaussianProbability1d(double mean, double variance, double target) {
        double normalizedDeviation = (target - mean) * (target - mean) / (2 * variance);
        double exponentialPower = -normalizedDeviation;
        double probability = Math.exp(exponentialPower) / Math.sqrt(variance * 2 * Math.PI);
        return probability;
    }
}
