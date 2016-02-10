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
 * An importer to get a data set from a Twitter json file.
 */
public class TwitterJsonImporter implements CanImport {
    public String twitterJsonPath;
    public String stopWordFilePath;

    public TwitterJsonImporter(String twitterJsonPath, String stopWordFilePath) {
        this.twitterJsonPath = twitterJsonPath;
        this.stopWordFilePath = stopWordFilePath;
    }

    public TwitterJsonImporter(String twitterJsonPath) {
        this.twitterJsonPath = twitterJsonPath;
        this.stopWordFilePath = null;
    }

    public DataSet getDataSet() {
        List<Instance> data = new ArrayList<Instance>();
        Map<String, Integer> word2Id = new HashMap<String, Integer>();
        Map<Integer, String> id2Word = new HashMap<Integer, String>();
        int currentId = 0;

        Set<String> stopWords = getStopWords();

        try {
            Scanner twitterReader = new Scanner(new File(twitterJsonPath));
            while(twitterReader.hasNextLine()) {
                String tweetJson = twitterReader.nextLine();
                JSONObject tweet = new JSONObject(tweetJson);

                JSONArray coordinates = tweet.getJSONObject("coordinates").getJSONArray("coordinates");
                Location location = new Location(coordinates.getDouble(0), coordinates.getDouble(1));

                String text = tweet.getString("text");
                String[] words = text.split("\\s+");
                List<Integer> wordIds = new ArrayList<Integer>();
                for (String word : words) {
                    if (!stopWords.contains(word) &&
                            word.length() > 1 &&
                            !word.startsWith("http") &&
                            !word.matches("[^a-zA-Z]+") &&
                            word.matches("[a-zA-Z0-9-'@#]+")) {
                        int wordId;
                        if (word2Id.containsKey(word)) {
                            wordId = word2Id.get(word);
                        } else {
                            wordId = currentId;
                            currentId++;
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
