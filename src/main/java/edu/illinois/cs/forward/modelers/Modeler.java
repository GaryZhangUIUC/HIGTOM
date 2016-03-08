package edu.illinois.cs.forward.modelers;

import edu.illinois.cs.forward.modelers.picker.AbstractPicker;
import edu.illinois.cs.forward.types.DataSet;
import edu.illinois.cs.forward.types.Instance;
import edu.illinois.cs.forward.types.Model;
import edu.illinois.cs.forward.types.Node;

import java.util.*;

/**
 * main entry point to estimate a model.
 */
public class Modeler {
    public Model model;
    public int numLevels;
    public Node root;

    public DataSet dataSet;
    public List<Instance> data;

    public NCRPEstimator ncrpEstimator;
    public LMEstimator lmEstimator;
    public GMEstimator gmEstimator;

    public List<List<Integer>> wordLevels4Documents;
    public List<Node> leaf4Documents;

    public AbstractPicker picker;

    public Modeler(
            Model model, DataSet dataSet,
            double gamma, double alpha, double eta, double kappa,
            double[] smoothingVariance4Levels, AbstractPicker picker) {
        this.model = model;
        this.numLevels = model.numLevels;
        this.root = model.root;

        this.dataSet = dataSet;
        this.data = dataSet.data;

        this.ncrpEstimator = new NCRPEstimator(gamma);
        this.lmEstimator = new LMEstimator(alpha, eta, dataSet.id2Word.size());
        this.gmEstimator = new GMEstimator(kappa, smoothingVariance4Levels);

        this.picker = picker;

        wordLevels4Documents = new ArrayList<List<Integer>>();
        leaf4Documents = new ArrayList<Node>();
        Random random = new Random();
        Node[] tempPath = new Node[numLevels];
        for (Instance instance : data) {
            tempPath[0] = root;
            root.addCustomer();
            root.updateLocationAfterAdding(instance.location);
            for (int pathIndex = 1; pathIndex < numLevels; pathIndex++) {
                Node nextNode = ncrpEstimator.selectNodeChild(tempPath[pathIndex - 1], random);
                nextNode.addCustomer();
                nextNode.updateLocationAfterAdding(instance.location);
                tempPath[pathIndex] = nextNode;
            }
            leaf4Documents.add(tempPath[numLevels - 1]);

            List<Integer> wordIds = instance.wordIds;
            List<Integer> wordLevels = new ArrayList<Integer>();
            for (Integer wordId : wordIds) {
                int randomLevel = random.nextInt(numLevels);
                wordLevels.add(randomLevel);
                tempPath[randomLevel].addWord(wordId);
            }
            wordLevels4Documents.add(wordLevels);
        }
    }

    public void estimate(int numIterations) {
        for (int iteration = 1; iteration <= numIterations; iteration++) {
            for (int docId = 0; docId < data.size(); docId++) {
                estimatePath(docId);
            }
            for (int docId = 0; docId < data.size(); docId++) {
                estimateWordLevels(docId);
            }
        }
    }

    public void estimatePath(int docId) {
        Instance instance = data.get(docId);

        Node tempNode = leaf4Documents.get(docId);
        Node[] tempPath = new Node[numLevels];
        for (int level = numLevels - 1; level >= 0; level--) {
            tempNode.removeCustomer();
            tempNode.updateLocationAfterRemoving(instance.location);
            tempPath[level] = tempNode;
            tempNode = tempNode.parent;
        }
        List<Integer> wordIds = instance.wordIds;
        List<Integer> wordLevels = wordLevels4Documents.get(docId);
        List<Map<Integer, Integer>> wordCounts4Levels = new ArrayList<Map<Integer, Integer>>();
        for (int level = 0; level < numLevels; level++) {
            wordCounts4Levels.add(new HashMap<Integer, Integer>());
        }
        for (int wordIndex = 0; wordIndex < wordIds.size(); wordIndex++) {
            int wordId = wordIds.get(wordIndex);
            int wordLevel = wordLevels.get(wordIndex);
            Map<Integer, Integer> wordCounts = wordCounts4Levels.get(wordLevel);
            int oldCount = wordCounts.getOrDefault(wordId, 0);
            wordCounts.put(wordId, oldCount + 1);
            tempPath[wordLevel].removeWord(wordId);
        }

        Map<Node, Double> likelihoods = new HashMap<Node, Double>();

        ncrpEstimator.updateLikelihoods(likelihoods, root);
        lmEstimator.updateLikelihoods(likelihoods, root, wordCounts4Levels);
        gmEstimator.updateLikelihoodsChained(likelihoods, root, instance.location);

        Node selectedNode = picker.pickNode(likelihoods);

        if (selectedNode == null) {
            System.err.println("Failed to select a node.");
        }

        selectedNode.addCustomer();
        selectedNode.updateLocationAfterAdding(instance.location);
        tempPath[selectedNode.level] = selectedNode;
        for (int level = selectedNode.level - 1; level >= 0; level--) {
            tempPath[level] = tempPath[level + 1].parent;
            tempPath[level].addCustomer();
            tempPath[level].updateLocationAfterAdding(instance.location);
        }
        for (int level = selectedNode.level + 1; level < numLevels; level++) {
            tempPath[level] = tempPath[level - 1].addChild();
            tempPath[level].addCustomer();
            tempPath[level].updateLocationAfterAdding(instance.location);
        }

        for (int wordIndex = 0; wordIndex < wordIds.size(); wordIndex++) {
            int wordId = wordIds.get(wordIndex);
            int wordLevel = wordLevels.get(wordIndex);
            tempPath[wordLevel].addWord(wordId);
        }

        leaf4Documents.set(docId, tempPath[numLevels - 1]);
    }

    public void estimateWordLevels(int docId) {
        List<Integer> wordIds = data.get(docId).wordIds;
        List<Integer> wordLevels = wordLevels4Documents.get(docId);

        int[] levelCounts = new int[numLevels];
        for (int wordIndex = 0; wordIndex < wordIds.size(); wordIndex++) {
            int wordLevel = wordLevels.get(wordIndex);
            levelCounts[wordLevel]++;
        }

        Node tempNode = leaf4Documents.get(docId);
        Node[] tempPath = new Node[numLevels];
        for (int level = numLevels - 1; level >= 0; level--) {
            tempPath[level] = tempNode;
            tempNode = tempNode.parent;
        }

        double[] levelWeights = new double[numLevels];
        for (int wordIndex = 0; wordIndex < wordIds.size(); wordIndex++) {
            int wordId = wordIds.get(wordIndex);
            int wordLevel = wordLevels.get(wordIndex);
            tempPath[wordLevel].removeWord(wordId);
            levelCounts[wordLevel]--;

            for (int level = 0; level < numLevels; level++) {
                levelWeights[level] = lmEstimator.calculateLevelWeight(tempPath[level], levelCounts[level], wordId);
            }

            wordLevel = picker.pickLevel(levelWeights);
            wordLevels.set(wordIndex, wordLevel);

            levelCounts[wordLevel]++;
            tempPath[wordLevel].addWord(wordId);
        }
    }
}
