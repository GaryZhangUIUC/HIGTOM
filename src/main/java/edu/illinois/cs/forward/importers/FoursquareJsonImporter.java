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
 *  An importer to get a data set from a Foursquare json file. A stop word set is also used here..
 */
public class FoursquareJsonImporter extends AbstractImporter {
    public String FoursquareJsonPath;
    public String stopWordFilePath;

    public FoursquareJsonImporter(String FoursquareJsonPath, String stopWordFilePath) {
        this.FoursquareJsonPath = FoursquareJsonPath;
        this.stopWordFilePath = stopWordFilePath;
    }

    public FoursquareJsonImporter(String FoursquareJsonPath) {
        this.FoursquareJsonPath = FoursquareJsonPath;
        this.stopWordFilePath = null;
    }

    @Override
    public DataSet getDataSet(Map<String, Integer> word2Id, Map<Integer, String> id2Word) {
        List<Instance> data = new ArrayList<Instance>();

        Set<String> stopWords = getStopWords();

        int nextId = findNextId(id2Word);

        try {
            Scanner twitterReader = new Scanner(new File(FoursquareJsonPath));
            while(twitterReader.hasNextLine()) {
                String tweetJson = twitterReader.nextLine();
                JSONObject tweet = new JSONObject(tweetJson);

                Double coordinates_lat = tweet.getJSONObject("venue").getJSONObject("location").getDouble("lat");
                Double coordinates_lon = tweet.getJSONObject("venue").getJSONObject("location").getDouble("lng");

                Location location = new Location(coordinates_lat, coordinates_lon);

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
*
* The importer assumes the file contains one Foursquare tip json per line
*
* Foursquare APIs are very friendly, you can easily search for up to 500 tips(tweets) with one location coordinates
* via https://api.foursquare.com/v2/tips/search?
*
* Sample Foursquare Json

{
  "meta":  {
    "code": 200,
    "requestId": "56c3f834498ec8b649cd264d"
  },
  "notifications":  [
     {
      "type": "notificationTray",
      "item":  {
        "unreadCount": 0
      }
    }
  ],
  "response":  {
    "tips":  [



    // Importer is written as one tip per line. need hacks when the data is available.



       {
        "id": "4e754fa0fa76059700f6a42e",
        "createdAt": 1316310944,
        "text": "2nd floor :)",
        "type": "user",
        "canonicalUrl": "https://foursquare.com/item/4e754fa0fa76059700f6a42e",
        "likes":  {
          "count": 1,
          "groups":  [
             {
              "type": "others",
              "count": 1,
              "items":  []
            }
          ],
          "summary": "1 like"
        },
        "like": false,
        "logView": true,
        "todo":  {
          "count": 0
        },
        "venue":  {
          "id": "4b16e4e6f964a52015bf23e3",
          "name": "Grainger Engineering Library",
          "contact":  {
            "phone": "2173333576",
            "formattedPhone": "(217) 333-3576"
          },
          "location":  {
            "address": "1301 W Springfield Ave",
            "crossStreet": "btw S Wright St & Matthews Ave",
            "lat": 40.112435344372024,
            "lng": -88.22688817977905,
            "distance": 33,
            "postalCode": "61801",
            "cc": "US",
            "city": "Urbana",
            "state": "IL",
            "country": "United States",
            "formattedAddress":  [
              "1301 W Springfield Ave (btw S Wright St & Matthews Ave)",
              "Urbana, IL 61801"
            ]
          },
          "categories":  [
             {
              "id": "4bf58dd8d48988d1a7941735",
              "name": "College Library",
              "pluralName": "College Libraries",
              "shortName": "Library",
              "icon":  {
                "prefix": "https://ss3.4sqi.net/img/categories_v2/building/library_",
                "suffix": ".png"
              },
              "primary": true
            }
          ],
          "verified": false,
          "stats":  {
            "checkinsCount": 5314,
            "usersCount": 962,
            "tipCount": 24
          },
          "like": false
        },
        "user":  {
          "id": "11856725",
          "firstName": "Joyce",
          "lastName": "An",
          "gender": "female",
          "photo":  {
            "prefix": "https://irs1.4sqi.net/img/user/",
            "suffix": "/0YIZLI1BJPOKONLS.jpg"
          }
        }
      },
       {
        "id": "4e09348b8877394591f0f3b9",
        "createdAt": 1309226123,
        "text": "Love the basement. Great place to study :)",
        "type": "user",
        "canonicalUrl": "https://foursquare.com/item/4e09348b8877394591f0f3b9",
        "likes":  {
          "count": 1,
          "groups":  [
             {
              "type": "others",
              "count": 1,
              "items":  []
            }
          ],
          "summary": "1 like"
        },
        "like": false,
        "logView": true,
        "todo":  {
          "count": 0
        },
        "venue":  {
          "id": "4b16e4e6f964a52015bf23e3",
          "name": "Grainger Engineering Library",
          "contact":  {
            "phone": "2173333576",
            "formattedPhone": "(217) 333-3576"
          },
          "location":  {
            "address": "1301 W Springfield Ave",
            "crossStreet": "btw S Wright St & Matthews Ave",
            "lat": 40.112435344372024,
            "lng": -88.22688817977905,
            "distance": 33,
            "postalCode": "61801",
            "cc": "US",
            "city": "Urbana",
            "state": "IL",
            "country": "United States",
            "formattedAddress":  [
              "1301 W Springfield Ave (btw S Wright St & Matthews Ave)",
              "Urbana, IL 61801"
            ]
          },
          "categories":  [
             {
              "id": "4bf58dd8d48988d1a7941735",
              "name": "College Library",
              "pluralName": "College Libraries",
              "shortName": "Library",
              "icon":  {
                "prefix": "https://ss3.4sqi.net/img/categories_v2/building/library_",
                "suffix": ".png"
              },
              "primary": true
            }
          ],
          "verified": false,
          "stats":  {
            "checkinsCount": 5314,
            "usersCount": 962,
            "tipCount": 24
          },
          "like": false
        },
        "user":  {
          "id": "8950394",
          "firstName": "Ben",
          "lastName": "Ruettiger",
          "gender": "male",
          "photo":  {
            "prefix": "https://irs0.4sqi.net/img/user/",
            "suffix": "/EVD20IA40GAPJ1VW.jpg"
          }
        }
      },
       {
        "id": "518b0972498e4bb1c5c19d46",
        "createdAt": 1368066418,
        "text": "Remember that this is a library, and in a library, you shut the fuck up.",
        "type": "user",
        "canonicalUrl": "https://foursquare.com/item/518b0972498e4bb1c5c19d46",
        "likes":  {
          "count": 0,
          "groups":  []
        },
        "like": false,
        "logView": true,
        "todo":  {
          "count": 0
        },
        "venue":  {
          "id": "4b16e4e6f964a52015bf23e3",
          "name": "Grainger Engineering Library",
          "contact":  {
            "phone": "2173333576",
            "formattedPhone": "(217) 333-3576"
          },
          "location":  {
            "address": "1301 W Springfield Ave",
            "crossStreet": "btw S Wright St & Matthews Ave",
            "lat": 40.112435344372024,
            "lng": -88.22688817977905,
            "distance": 33,
            "postalCode": "61801",
            "cc": "US",
            "city": "Urbana",
            "state": "IL",
            "country": "United States",
            "formattedAddress":  [
              "1301 W Springfield Ave (btw S Wright St & Matthews Ave)",
              "Urbana, IL 61801"
            ]
          },
          "categories":  [
             {
              "id": "4bf58dd8d48988d1a7941735",
              "name": "College Library",
              "pluralName": "College Libraries",
              "shortName": "Library",
              "icon":  {
                "prefix": "https://ss3.4sqi.net/img/categories_v2/building/library_",
                "suffix": ".png"
              },
              "primary": true
            }
          ],
          "verified": false,
          "stats":  {
            "checkinsCount": 5314,
            "usersCount": 962,
            "tipCount": 24
          },
          "like": false
        },
        "user":  {
          "id": "26087",
          "firstName": "Brett",
          "lastName": "Jackson",
          "gender": "male",
          "photo":  {
            "prefix": "https://irs3.4sqi.net/img/user/",
            "suffix": "/FGSXLYA4Y4NEEI13.jpg"
          }
        }
      },
       {
        "id": "4cbb3614cdccb7136d9b6e79",
        "createdAt": 1287337492,
        "text": "There's silence and any kind of book! Amazing!",
        "type": "user",
        "canonicalUrl": "https://foursquare.com/item/4cbb3614cdccb7136d9b6e79",
        "likes":  {
          "count": 1,
          "groups":  [
             {
              "type": "others",
              "count": 1,
              "items":  []
            }
          ],
          "summary": "1 like"
        },
        "like": false,
        "logView": true,
        "todo":  {
          "count": 0
        },
        "venue":  {
          "id": "4b16e4e6f964a52015bf23e3",
          "name": "Grainger Engineering Library",
          "contact":  {
            "phone": "2173333576",
            "formattedPhone": "(217) 333-3576"
          },
          "location":  {
            "address": "1301 W Springfield Ave",
            "crossStreet": "btw S Wright St & Matthews Ave",
            "lat": 40.112435344372024,
            "lng": -88.22688817977905,
            "distance": 33,
            "postalCode": "61801",
            "cc": "US",
            "city": "Urbana",
            "state": "IL",
            "country": "United States",
            "formattedAddress":  [
              "1301 W Springfield Ave (btw S Wright St & Matthews Ave)",
              "Urbana, IL 61801"
            ]
          },
          "categories":  [
             {
              "id": "4bf58dd8d48988d1a7941735",
              "name": "College Library",
              "pluralName": "College Libraries",
              "shortName": "Library",
              "icon":  {
                "prefix": "https://ss3.4sqi.net/img/categories_v2/building/library_",
                "suffix": ".png"
              },
              "primary": true
            }
          ],
          "verified": false,
          "stats":  {
            "checkinsCount": 5314,
            "usersCount": 962,
            "tipCount": 24
          },
          "like": false
        },
        "user":  {
          "id": "2517911",
          "firstName": "Annalisa",
          "lastName": "Minelli",
          "gender": "female",
          "photo":  {
            "prefix": "https://irs3.4sqi.net/img/user/",
            "suffix": "/JPHSD0HUFHM30WFA.jpg"
          }
        }
      },
       {
        "id": "5051e6cfe4b05b8794a455ff",
        "createdAt": 1347544783,
        "text": "The 4th floor has super comfy couches to read on.",
        "type": "user",
        "canonicalUrl": "https://foursquare.com/item/5051e6cfe4b05b8794a455ff",
        "likes":  {
          "count": 0,
          "groups":  []
        },
        "like": false,
        "logView": true,
        "todo":  {
          "count": 0
        },
        "venue":  {
          "id": "4b16e4e6f964a52015bf23e3",
          "name": "Grainger Engineering Library",
          "contact":  {
            "phone": "2173333576",
            "formattedPhone": "(217) 333-3576"
          },
          "location":  {
            "address": "1301 W Springfield Ave",
            "crossStreet": "btw S Wright St & Matthews Ave",
            "lat": 40.112435344372024,
            "lng": -88.22688817977905,
            "distance": 33,
            "postalCode": "61801",
            "cc": "US",
            "city": "Urbana",
            "state": "IL",
            "country": "United States",
            "formattedAddress":  [
              "1301 W Springfield Ave (btw S Wright St & Matthews Ave)",
              "Urbana, IL 61801"
            ]
          },
          "categories":  [
             {
              "id": "4bf58dd8d48988d1a7941735",
              "name": "College Library",
              "pluralName": "College Libraries",
              "shortName": "Library",
              "icon":  {
                "prefix": "https://ss3.4sqi.net/img/categories_v2/building/library_",
                "suffix": ".png"
              },
              "primary": true
            }
          ],
          "verified": false,
          "stats":  {
            "checkinsCount": 5314,
            "usersCount": 962,
            "tipCount": 24
          },
          "like": false
        },
        "user":  {
          "id": "2767289",
          "firstName": "The Tower",
          "lastName": "at Third",
          "gender": "male",
          "photo":  {
            "prefix": "https://irs0.4sqi.net/img/user/",
            "suffix": "/FCR54TAVTNUTG2PP.jpg"
          }
        }
      }
    ]
  }
}
 */