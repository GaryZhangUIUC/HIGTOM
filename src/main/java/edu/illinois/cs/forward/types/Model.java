package edu.illinois.cs.forward.types;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import java.io.Serializable;

/**
 * Containing the hierarchical geographical tree and some related information.
 *
 * Yulun to Gary: Serialized the Model class so that it could be dumped to file and read back.
 * (Haven't tested the functionality yet.)
 */
public class Model implements Serializable {
    public Node root;
    public int numLevels;

    public Map<String, Integer> word2Id;
    public Map<Integer, String> id2Word;

    public Model(int numLevels) {
        this.numLevels = numLevels;
        this.root = new Node(null, 0);

        this.word2Id = new HashMap<>();
        this.id2Word = new HashMap<>();
    }

    public void setWordProfile(Map<String, Integer> word2Id, Map<Integer, String> id2Word) {
        this.word2Id = word2Id;
        this.id2Word = id2Word;
    }

    public void outputToJSON(String filePath, int minimumPopularity, int numTopWords, double boundaryFactor) {
        System.out.println("Writing down the model...");

        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();
        Queue<Node> nodeQueue = new LinkedList<>();
        nodeQueue.offer(root);
        while (!nodeQueue.isEmpty()) {
            Node currentNode = nodeQueue.poll();
            if (currentNode.numCustomers < minimumPopularity) {
                continue;
            }
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("id", currentNode.hashCode());

            JSONObject properties = new JSONObject();
            properties.put("level", currentNode.level + 1);
            properties.put("num_documents", currentNode.numCustomers);

            if (currentNode.parent != null) {
                properties.put("parent", currentNode.parent.hashCode());
            }

            if (!currentNode.isLeaf()) {
                JSONArray children = new JSONArray();
                for (Node child: currentNode.children) {
                    children.put(child.hashCode());
                }
                properties.put("children", children);
            }

            JSONArray center = new JSONArray();
            double longitude = currentNode.location.longitude;
            center.put(longitude);
            double latitude = currentNode.location.latitude;
            center.put(latitude);
            properties.put("center", center);

            JSONArray deviation = new JSONArray();
            double longitudeDeviation = Math.sqrt(currentNode.location.longitudeVariance);
            deviation.put(longitudeDeviation);
            double latitudeDeviation = Math.sqrt(currentNode.location.latitudeVariance);
            deviation.put(latitudeDeviation);
            properties.put("deviation", deviation);

            JSONArray topWords = new JSONArray();
            PriorityQueue<Map.Entry<Integer, Integer>> wordMinHeap = new PriorityQueue<>(
                    numTopWords,
                    new Comparator<Map.Entry<Integer, Integer>>() {
                        @Override
                        public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                            return o1.getValue() - o2.getValue();
                        }
                    }
            );
            for (Map.Entry<Integer, Integer> entry: currentNode.wordCounts.entrySet()) {
                if (wordMinHeap.size() < numTopWords) {
                    wordMinHeap.offer(entry);
                } else {
                    if (entry.getValue() > wordMinHeap.peek().getValue()) {
                        wordMinHeap.poll();
                        wordMinHeap.offer(entry);
                    }
                }
            }
            while (!wordMinHeap.isEmpty()) {
                topWords.put(this.id2Word.get(wordMinHeap.poll().getKey()));
            }
            properties.put("top_words", topWords);

            feature.put("properties", properties);

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");

            JSONArray coordinates = new JSONArray();
            JSONArray boundary = new JSONArray();
            double east = longitude + boundaryFactor * longitudeDeviation;
            double west = longitude - boundaryFactor * longitudeDeviation;
            double north = latitude + boundaryFactor * latitudeDeviation;
            double south = latitude - boundaryFactor * latitudeDeviation;
            boundary.put(new JSONArray(new double[]{west, north}));
            boundary.put(new JSONArray(new double[]{east, north}));
            boundary.put(new JSONArray(new double[]{east, south}));
            boundary.put(new JSONArray(new double[]{west, south}));
            boundary.put(new JSONArray(new double[]{west, north}));
            coordinates.put(boundary);
            geometry.put("coordinates", coordinates);

            feature.put("geometry", geometry);

            features.put(feature);

            for (Node child: currentNode.children) {
                nodeQueue.offer(child);
            }
        }

        featureCollection.put("features", features);

        try {
            PrintWriter writer = new PrintWriter(filePath);
            featureCollection.write(writer);
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Model JSON cannot be created.");
        }
    }
}
