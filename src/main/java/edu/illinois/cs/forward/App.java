package edu.illinois.cs.forward;

import edu.illinois.cs.forward.importers.TwitterJsonImporter;
import edu.illinois.cs.forward.modelers.Modeler;
import edu.illinois.cs.forward.modelers.picker.AbstractPicker;
import edu.illinois.cs.forward.modelers.picker.RandomPicker;
import edu.illinois.cs.forward.types.DataSet;
import edu.illinois.cs.forward.types.Model;

import java.util.HashMap;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.*;
import edu.illinois.cs.forward.types.Node;
/**
 * The main program to run the framework
 *
 */
public class App {
    public static void printNode(Modeler m, Node n, Writer writer, int parent, int id, int level){

        try {
            writer.write(level + "");
            writer.write("\t");
            writer.write(id + "");
            writer.write("\t");
            writer.write(parent + "");
            writer.write("\t");
            writer.write(String.valueOf(n.location.latitude) + ", " + String.valueOf(n.location.longitude));
            writer.write("\t");
            for (Map.Entry<Integer,Integer> entry : n.wordCounts.entrySet()) {
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                writer.write(m.dataSet.id2Word.get(key));
                writer.write(":");
                writer.write(value + "");
                writer.write(",");
            }
            writer.write("\t");
            writer.write(String.valueOf(n.location.latitudeVariance) + " " + String.valueOf(n.location.longitudeVariance));
            writer.write("\n");
        }
        catch (IOException ex) {
            // report
            System.out.println( "error!" );
        }

    }

    public static void main( String[] args ) {
        TwitterJsonImporter importer = new TwitterJsonImporter(
                "src/main/resources/chicago75000s.txt", "src/main/resources/stopwords.txt");
        DataSet dataSet = importer.getDataSet(new HashMap<>(), new HashMap<>());

        Model model = new Model(3);
        model.setWordProfile(dataSet.word2Id, dataSet.id2Word);
        double[] smoothingVariance4Levels = {1e-4, 1e-5, 1e-6};
        AbstractPicker picker = new RandomPicker();
        Modeler modeler = new Modeler(
                model, dataSet,
                0.1, 10.0, 0.1,
                10.0, smoothingVariance4Levels, 9.0,
                picker
        );
        modeler.estimate(10);
        System.out.println( "Good luck!" );



                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("out.txt"), "utf-8"))) {


            Node temp = modeler.root;

            if( !temp.isLeaf()){

                // root
                printNode(modeler, temp, writer, 0, 0, 0);

                for(int i = 0; i < temp.children.size(); i++){

                    // 2nd level
                    printNode(modeler, temp.children.get(i), writer, 0, i, 1);

                    for(int j = 0; j < temp.children.get(i).children.size(); j++){

                        // leafs
                        printNode(modeler, temp.children.get(i).children.get(j), writer, i, j, 2);

                    }
                }


            }




        }
        catch (IOException ex) {
            // report
            System.out.println( "file error!" );
        }

        
    }








}
