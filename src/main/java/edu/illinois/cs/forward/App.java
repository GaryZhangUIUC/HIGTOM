package edu.illinois.cs.forward;

import edu.illinois.cs.forward.importers.TwitterJsonImporter;
import edu.illinois.cs.forward.modelers.Modeler;
import edu.illinois.cs.forward.modelers.picker.AbstractPicker;
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
        DataSet dataSet = importer.getDataSet(new HashMap<String, Integer>(), new HashMap<Integer, String>());

        Model model = new Model(3);
        double[] smoothingVariance4Levels = {0.001, 0.0003, 0.0001};
        AbstractPicker picker = new RandomPicker();
        Modeler modeler = new Modeler(
                model, dataSet,
                1.0, 10.0, 0.1, 10.0,
                smoothingVariance4Levels, picker
        );
        modeler.estimate(2);
        System.out.println( "Good luck!" );
    }
}
