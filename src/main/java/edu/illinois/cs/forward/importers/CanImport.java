package edu.illinois.cs.forward.importers;

import edu.illinois.cs.forward.types.DataSet;

/**
 * The rule to be an importer.
 */
public interface CanImport {
    DataSet getDataSet();
}
