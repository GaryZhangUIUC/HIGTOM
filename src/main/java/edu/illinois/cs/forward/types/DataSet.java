package edu.illinois.cs.forward.types;

import java.util.List;
import java.util.Map;

/**
 * A data set, including a collection of instances and the dictionary used to convert words from/to ids
 */
public class DataSet {
    public List<Instance> data;
    public Map<Integer, String> id2Word;
    public Map<String, Integer> word2Id;

    public DataSet(List<Instance> data, Map<Integer, String> id2Word, Map<String, Integer> word2Id) {
        this.data = data;
        this.id2Word = id2Word;
        this.word2Id = word2Id;
    }
}