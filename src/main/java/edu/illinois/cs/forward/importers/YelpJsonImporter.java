package edu.illinois.cs.forward.importers;

import edu.illinois.cs.forward.types.Location;
import edu.illinois.cs.forward.types.DataSet;
import edu.illinois.cs.forward.types.Instance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *  An importer to get a data set from a Yelp json file. A stop word set is also used here.
 */
public class YelpJsonImporter extends AbstractImporter {

    public String yelpJsonPath;
    public String stopWordFilePath;

    //A business2coordinates map
    public String b2cPath;
    public Map<String, Location> b2c;

    public YelpJsonImporter(String yelpJsonPath, String stopWordFilePath, String b2cPath) {
        this.yelpJsonPath = yelpJsonPath;
        this.stopWordFilePath = stopWordFilePath;
        this.b2cPath = b2cPath;
        b2cMake();
    }

    public YelpJsonImporter(String yelpJsonPath, String b2cPath) {
        this.yelpJsonPath = yelpJsonPath;
        this.stopWordFilePath = null;
        this.b2cPath = b2cPath;
        b2cMake();
    }

    /*
       Json object used for mapping business id to location
       here we assume the file contains all the businesses from one area,
       with one business per line in the file.
     {
     'type': 'business',
     'business_id': (a unique identifier for this business),
     'name': (the full business name),
     'neighborhoods': (a list of neighborhood names, might be empty),
     'full_address': (localized address),
     'city': (city),
     'state': (state),
     'latitude': (latitude),
     'longitude': (longitude),
     'stars': (star rating, rounded to half-stars),
     'review_count': (review count),
     'photo_url': (photo url),
     'categories': [(localized category names)]
     'open': (is the business still open for business?),
     'schools': (nearby universities),
     'url': (yelp url)
     }
     */
    public void b2cMake(){
        try {
            Scanner mapReader = new Scanner(new File(this.b2cPath));
            while(mapReader.hasNextLine()) {
                String line = mapReader.nextLine();
                JSONObject business = new JSONObject(line);
                String id = business.getString("business_id");
                Double lat = business.getDouble("latitude");
                Double lon = business.getDouble("longitude");

                this.b2c.put(id, new Location(lat, lon));
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
            Scanner yelpReader = new Scanner(new File(yelpJsonPath));
            while(yelpReader.hasNextLine()) {
                //Here we assume json results are dumped in a text file with one result per line.
                String YelpJson = yelpReader.nextLine();
                JSONObject tweet = new JSONObject(YelpJson);

                String business_id = tweet.getString("business_id");

                Location location = b2c.get(business_id);

                String text = tweet.getString("text");

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
            yelpReader.close();
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
*
* Sample Yelp review Json File
*
{
    'type': 'review',
    'business_id': (the identifier of the reviewed business),
    'user_id': (the identifier of the authoring user),
    'stars': (star rating, integer 1-5),
    'text': (review text),
    'date': (date, formatted like '2011-04-19'),
    'votes': {
        'useful': (count of useful votes),
        'funny': (count of funny votes),
        'cool': (count of cool votes)
    }
}
 */