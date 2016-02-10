package edu.illinois.cs.forward.types;

import java.util.List;

/**
 * A single instance, where words are hashed into ids
 */
public class Instance {
    public List<Integer> wordIds;
    public Location location;

    public Instance(List<Integer> wordIds, Location location) {
        this.wordIds = wordIds;
        this.location = location;
    }
}
