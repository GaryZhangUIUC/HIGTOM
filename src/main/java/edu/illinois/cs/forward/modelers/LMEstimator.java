package edu.illinois.cs.forward.modelers;

import edu.illinois.cs.forward.types.Node;

import java.util.Map;

/**
 * An estimator to calculate the probabilities for language modeling for the tree nodes.
 */
public class LMEstimator {
    // smoothing on word distributions
    double eta;
    double etaSum;

    public LMEstimator(double eta, int numWords) {
        this.eta = eta;
        this.etaSum = eta * numWords;
    }
    
    /**
     * Estimate the language model probabilities and update in place the likelihoods for the tree nodes.
     */
    public void updateLikelihoods(
            Map<Node, Double> likelihoods, Node root,
            Map<Integer, Integer>[] wordCounts4Levels) {
        double[] newWieghts4Levels = new double[wordCounts4Levels.length];
        for (int level = 0; level < wordCounts4Levels.length; level++) {
            int numWords = 0;
            for (double wordCount: wordCounts4Levels[level].values()) {
                for (int countIter = 0; countIter < wordCount; countIter++) {
                    newWieghts4Levels[level] += Math.log((eta + countIter) / (etaSum + numWords));
                    numWords++;
                }
            }
        }

        updateLikelihoods(likelihoods, root, 0.0, wordCounts4Levels, newWieghts4Levels);
    }

    public void updateLikelihoods(
            Map<Node, Double> likelihoods, Node currentNode, double likelihoodOffset,
            Map<Integer, Integer>[] wordCounts4Levels, double[] newWieghts4Levels) {
        double oldProb = likelihoods.getOrDefault(currentNode, 0.0);

        int level = currentNode.level;

        double languageLikelihood = 0.0;
        int numNewWords = 0;
        for (Map.Entry<Integer, Integer> wordCount: wordCounts4Levels[level].entrySet()) {
            int wordId = wordCount.getKey();
            int count = wordCount.getValue();
            for (int countIter = 0; countIter < count; countIter++) {
                languageLikelihood += Math.log(
                        (eta + currentNode.wordCounts.getOrDefault(wordId, 0) + countIter) /
                        (etaSum + currentNode.numWords + numNewWords));
                numNewWords += 1;
            }
        }

        for (Node child: currentNode.children) {
            updateLikelihoods(
                    likelihoods, child, likelihoodOffset + languageLikelihood,
                    wordCounts4Levels, newWieghts4Levels);
        }

        for (int newLevel = level + 1; newLevel < newWieghts4Levels.length; newLevel++) {
            languageLikelihood += newWieghts4Levels[level];
        }

        likelihoods.put(currentNode, oldProb + likelihoodOffset + languageLikelihood);
    }
}
