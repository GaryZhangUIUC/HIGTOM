package edu.illinois.cs.forward.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representing an area in the hierarchical structure.
 */
public class Node {
    public Node parent;
    public List<Node> children;

    public int level;

    public int numCustomers;

    public int numWords;
    public Map<Integer, Integer> wordCounts;

    public Location location;

    public Node(Node parent, int level) {
        this.parent = parent;
        this.level = level;

        this.children = new ArrayList<Node>();
        this.numCustomers = 0;
        this.numWords = 0;
        this.wordCounts = new HashMap<Integer, Integer>();
        this.location = null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Node addChild() {
        Node child = new Node(this, level + 1);
        children.add(child);
        return child;
    }

    public void removeChild(Node child) {
        children.remove(child);
    }

    public void addCustomer() {
        numCustomers++;
    }

    public void removeCustomer() {
        numCustomers--;
        if (parent != null && numCustomers == 0) {
            parent.removeChild(this);
        }
    }

    /**
     * It's set to update the location after adding the customer.
     */
    public void updateLocationAfterAdding(Location locationAdded) {
        if (location == null) {
            location = locationAdded;
        } else {
            double newLongitudeMean = (location.longitude * (numCustomers - 1) + locationAdded.longitude) /
                    numCustomers;
            double newLatitudeMean = (location.latitude * (numCustomers - 1) + locationAdded.latitude) /
                    numCustomers;

            location.longitudeVariance = (location.longitudeVariance * (numCustomers - 1) +
                    (newLongitudeMean - location.longitude) * (newLongitudeMean - location.longitude) *
                            (numCustomers - 1) +
                    (locationAdded.longitude - newLongitudeMean) * (locationAdded.longitude - newLongitudeMean)) /
                    numCustomers;
            location.longitudeLatitudeCovariance = (location.longitudeLatitudeCovariance * (numCustomers - 1) +
                    (newLongitudeMean - location.longitude) * (newLatitudeMean - location.latitude) *
                            (numCustomers - 1) +
                    (locationAdded.longitude - newLongitudeMean) * (locationAdded.latitude - newLatitudeMean)) /
                    numCustomers;
            location.latitudeVariance = (location.latitudeVariance * (numCustomers - 1) +
                    (newLatitudeMean - location.latitude) * (newLatitudeMean - location.latitude) *
                            (numCustomers - 1) +
                    (locationAdded.latitude - newLatitudeMean) * (locationAdded.latitude - newLatitudeMean)) /
                    numCustomers;
            if (location.latitudeVariance * location.longitudeVariance * 1.1 <
                    location.longitudeLatitudeCovariance * location.longitudeLatitudeCovariance) {
                System.err.println("Invalid covariance matrix.");
            }
            location.longitude = newLongitudeMean;
            location.latitude = newLatitudeMean;
        }
    }

    /**
     * It's set to update the location after removing the customer.
     */
    public void updateLocationAfterRemoving(Location locationRemoved) {
        if (numCustomers != 0) {
            double oldLongitudeMean = (location.longitude * (numCustomers + 1) - locationRemoved.longitude) /
                    numCustomers;
            double oldLatitudeMean = (location.latitude * (numCustomers + 1) - locationRemoved.latitude) /
                    numCustomers;

            location.longitudeVariance = (location.longitudeVariance * (numCustomers + 1) -
                    (oldLongitudeMean - location.longitude) * (oldLongitudeMean - location.longitude) * numCustomers -
                    (locationRemoved.longitude - location.longitude) *
                            (locationRemoved.longitude - location.longitude)) /
                    numCustomers;
            location.longitudeLatitudeCovariance = (location.longitudeLatitudeCovariance * (numCustomers + 1) -
                    (oldLongitudeMean - location.longitude) * (oldLatitudeMean - location.latitude) * numCustomers -
                    (locationRemoved.longitude - location.longitude) *
                            (locationRemoved.latitude - location.latitude)) /
                    numCustomers;
            location.latitudeVariance = (location.latitudeVariance * (numCustomers + 1) -
                    (oldLatitudeMean - location.latitude) * (oldLatitudeMean - location.latitude) * numCustomers -
                    (locationRemoved.latitude - location.latitude) *
                            (locationRemoved.latitude - location.latitude)) /
                    numCustomers;
            if (location.latitudeVariance * location.longitudeVariance * 1.1 <
                    location.longitudeLatitudeCovariance * location.longitudeLatitudeCovariance) {
                System.err.println("Invalid covariance matrix.");
            }
            location.longitude = oldLongitudeMean;
            location.latitude = oldLatitudeMean;
        }
    }

    public void addWord(int wordId) {
        numWords++;
        int oldCount = wordCounts.getOrDefault(wordId, 0);
        wordCounts.put(wordId, oldCount + 1);
    }

    public void removeWord(int wordId) {
        numWords--;
        int oldCount = wordCounts.get(wordId);
        if (oldCount == 1) {
            wordCounts.remove(wordId);
        } else {
            wordCounts.put(wordId, oldCount - 1);
        }
    }
}
