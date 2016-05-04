package edu.illinois.cs.forward;

import edu.illinois.cs.forward.importers.TwitterJsonImporter;
import edu.illinois.cs.forward.modelers.Modeler;
import edu.illinois.cs.forward.modelers.picker.AbstractPicker;
import edu.illinois.cs.forward.modelers.picker.MaximumPicker;
import edu.illinois.cs.forward.modelers.picker.RandomPicker;
import edu.illinois.cs.forward.types.DataSet;
import edu.illinois.cs.forward.types.Model;

import java.util.HashMap;

/**
 * The main program to run the framework
 *
 */
public class App {
    public static void main( String[] args ) {
        TwitterJsonImporter importer = new TwitterJsonImporter(
                "src/main/resources/chicago75000s.txt", "src/main/resources/stopwords.txt");
        DataSet dataSet = importer.getDataSet(new HashMap<>(), new HashMap<>());

        Model model = new Model(3);
        model.setWordProfile(dataSet.word2Id, dataSet.id2Word);
        double[] smoothingVariance4Levels = {1e-3, 1e-6, 1e-7};
        AbstractPicker pathPicker = new RandomPicker();
        AbstractPicker levelPicker = new RandomPicker();
        Modeler modeler = new Modeler(
                model, dataSet,
                0.01, 10.0, 0.1,
                10.0, smoothingVariance4Levels, 9.0,
                pathPicker, levelPicker
        );
        modeler.estimate(10);
        model.outputToJSON("src/main/resources/model.json", 10, 1.0);
    }
}
