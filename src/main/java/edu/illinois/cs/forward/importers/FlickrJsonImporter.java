package edu.illinois.cs.forward.importers;

import edu.illinois.cs.forward.types.Location;
import edu.illinois.cs.forward.types.DataSet;
import edu.illinois.cs.forward.types.Instance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * An importer to get a data set from a Flickr json file. A stop word set is also used here..
 */
public class FlickrJsonImporter extends AbstractImporter {
    public String FlickrJsonPath;
    public String stopWordFilePath;

    //A photo2coordinates map
    public String p2cPath;
    public Map<String, Location> p2c;


    public FlickrJsonImporter(String FlickrJsonPath, String stopWordFilePath, String p2cPath) {
        this.FlickrJsonPath = FlickrJsonPath;
        this.stopWordFilePath = stopWordFilePath;
        this.p2cPath = p2cPath;
        p2cMake();
    }

    public FlickrJsonImporter(String FlickrJsonPath, String p2cPath) {
        this.FlickrJsonPath = FlickrJsonPath;
        this.stopWordFilePath = null;
        this.p2cPath = p2cPath;
        p2cMake();
    }

    /*
       Json object used for mapping photo id to location
       here we assume the file contains all the photos in one area,
       with one photo per line in the file.
     {

         "id": "123",
         "location": {

             "latitude": -17.685895,
             "longitude": -63.36914,
             "accuracy": "6"

        }
     }
     */
    public void p2cMake(){
        try {
            Scanner mapReader = new Scanner(new File(this.p2cPath));
            while(mapReader.hasNextLine()) {
                String line = mapReader.nextLine();
                JSONObject business = new JSONObject(line);
                String id = business.getString("id");
                Double lat = business.getDouble("latitude");
                Double lon = business.getDouble("longitude");

                this.p2c.put(id, new Location(lat, lon));
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("Error loading the mapping file.");
            e.printStackTrace();
        }
    }

    @Override
    public DataSet getDataSet(Map<String, Integer> word2Id, Map<Integer, String> id2Word) {
        List<Instance> data = new ArrayList<Instance>();

        Set<String> stopWords = getStopWords();

        int nextId = findNextId(id2Word);

        try {
            Scanner twitterReader = new Scanner(new File(FlickrJsonPath));
            while(twitterReader.hasNextLine()) {
                String tweetJson = twitterReader.nextLine();
                JSONObject tweet = new JSONObject(tweetJson);

                String photo_id = tweet.getString("id");

                Location location = p2c.get(photo_id);

                String text = tweet.getString("description");

                String[] words = text.split("\\s+");
                List<Integer> wordIds = new ArrayList<Integer>();
                for (String word : words) {
                    word = word.toLowerCase();
                    if (!stopWords.contains(word) &&
                            word.length() > 1 &&
                            !word.startsWith("http") &&
                            !word.matches("[^a-zA-Z]+") &&
                            word.matches("[a-zA-Z0-9-'@#]+")) {
                        int wordId;
                        if (word2Id.containsKey(word)) {
                            wordId = word2Id.get(word);
                        } else {
                            wordId = nextId;
                            nextId++;
                            word2Id.put(word, wordId);
                            id2Word.put(wordId, word);
                        }
                        wordIds.add(wordId);
                    }
                }
                Instance instance = new Instance(wordIds, location);
                data.add(instance);
            }
            twitterReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error loading the instance file.");
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Error parsing the instance jsons.");
            e.printStackTrace();
        }

        return new DataSet(data, id2Word, word2Id);
    }

    private Set<String> getStopWords() {
        Set<String> stopWords = new HashSet<String>();

        try {
            if (stopWordFilePath != null) {
                Scanner stopWordReader = new Scanner(new File(stopWordFilePath));
                while (stopWordReader.hasNextLine()) {
                    String stopWord = stopWordReader.nextLine();
                    stopWords.add(stopWord);
                }
                stopWordReader.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error loading the stop-word file.");
            e.printStackTrace();
        }

        return stopWords;
    }

}


/*
* Flickr API: flickr.photos.geo.photosForLocation(location)-->photos ids;
*             flickr.photos.getInfo(photos id) --> descriptions;
*             flickr.photos.geo.getLocation(photo id) --> location;
*
*             additionally, photo.comments might be useful too.
*
* Sample Flickr photo info Json file (Note: Flickr APIs return XML, so we need to convert XML to Json)


{
   "@id": "2733",
   "@secret": "123456",
   "@server": "12",
   "@isfavorite": "0",
   "@license": "3",
   "@rotation": "90",
   "@originalsecret": "1bc09ce34a",
   "@originalformat": "png",
   "owner": {
      "@nsid": "12037949754@N01",
      "@username": "Bees",
      "@realname": "Cal Henderson",
      "@location": "Bedford, UK"
   },
   "title": "orford_castle_taster",
   "description": "hello!",
   "visibility": {
      "@ispublic": "1",
      "@isfriend": "0",
      "@isfamily": "0"
   },
   "dates": {
      "@posted": "1100897479",
      "@taken": "2004-11-19 12:51:19",
      "@takengranularity": "0",
      "@lastupdate": "1093022469"
   },
   "permissions": {
      "@permcomment": "3",
      "@permaddmeta": "2"
   },
   "editability": {
      "@cancomment": "1",
      "@canaddmeta": "1"
   },
   "comments": "1",
   "notes": [
      {
         "@id": "313",
         "@author": "12037949754@N01",
         "@authorname": "Bees",
         "@x": "10",
         "@y": "10",
         "@w": "50",
         "@h": "50",
         "#text": "foo"
      }
   ],
   "tags": [
      {
         "@id": "1234",
         "@author": "12037949754@N01",
         "@raw": "woo yay",
         "#text": "wooyay"
      },
      {
         "@id": "1235",
         "@author": "12037949754@N01",
         "@raw": "hoopla",
         "#text": "hoopla"
      }
   ],
   "urls": [
      "http://www.flickr.com/photos/bees/2733/"
   ]
}
 */