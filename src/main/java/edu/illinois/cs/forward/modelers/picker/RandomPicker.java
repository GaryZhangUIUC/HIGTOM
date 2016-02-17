package edu.illinois.cs.forward.modelers.picker;

import edu.illinois.cs.forward.types.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A picker to randomly pick items based on weights.
 */
public class RandomPicker extends AbstractPicker {
    Random random = new Random();

    @Override
    public Node pickNode(Map<Node, Double> likelihoods) {
        double max = -1.0;
        for (double likelihood: likelihoods.values()) {
            max = Math.max(max, likelihood);
        }
        double sum = 0.0;
        Map<Node, Double> probabilities = new HashMap<Node, Double>();
        for (Map.Entry<Node, Double> node2Likelihood: likelihoods.entrySet()) {
            double probability = Math.exp(node2Likelihood.getValue() - max);
            sum += probability;
            probabilities.put(node2Likelihood.getKey(), probability);
        }
        double randomNum = random.nextDouble() * sum;
        for (Map.Entry<Node, Double> node2Probability: probabilities.entrySet()) {
            if (randomNum < node2Probability.getValue()) {
                return node2Probability.getKey();
            } else {
                randomNum -= node2Probability.getValue();
            }
        }
        return null;
    }

    @Override
    public int pickLevel(double[] levelWeights) {
        double sum = 0.0;
        for (double weight: levelWeights) {
            sum += weight;
        }
        double randomNum = random.nextDouble() * sum;
        for (int level = 0; level < levelWeights.length; level++) {
            if (randomNum < levelWeights[level]) {
                return level;
            } else {
                randomNum -= levelWeights[level];
            }
        }
        return -1;
    }
}
