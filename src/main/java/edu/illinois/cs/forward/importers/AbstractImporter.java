package edu.illinois.cs.forward.importers;

import edu.illinois.cs.forward.types.DataSet;

import java.util.HashMap;
import java.util.Map;

/**
 * The rule to be an importer.
 */
public abstract class AbstractImporter {
    public DataSet getDataSet() {
        return getDataSet(new HashMap<String, Integer>(), new HashMap<Integer, String>());
    }

    public abstract DataSet getDataSet(Map<String, Integer> word2Id, Map<Integer, String> id2Word);

    public int findNextId(Map<Integer, String> id2Word) {
        int nextId = 0;
        for (int id: id2Word.keySet()) {
            if (id >= nextId) {
                nextId = id + 1;
            }
        }
        return nextId;
    }
}
