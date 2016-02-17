package edu.illinois.cs.forward.types;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by yulundu on 2/17/16.
 * test serialization of Model object and the presentation of the model
 */
public class testModel {


    public void saveModel(Model m, String filename){

        Model temp = m;

        try{
            FileOutputStream fout = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(filename);
            oos.close();

            System.out.println("Model Saved");
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public Model readModel(Model m, String filename){

        try{
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Model result = (Model) ois.readObject();
            ois.close();
            System.out.println("Model Read");

            return result;

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void presentModel(Model m){
        System.out.println( "Unfinished here.");
    }

    public static void main( String[] args ) {

        System.out.println("hmmm");

    }
}
