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
 * An importer to get a data set from a Twitter json file. A stop word set is also used here.
 */
public class TwitterJsonImporter extends AbstractImporter {
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

    @Override
    public DataSet getDataSet(Map<String, Integer> word2Id, Map<Integer, String> id2Word) {
        List<Instance> data = new ArrayList<Instance>();

        Set<String> stopWords = getStopWords();

        int nextId = findNextId(id2Word);

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
* Sample Twitter Json File
*
{
      "metadata":  {
        "iso_language_code": "pt",
        "result_type": "recent"
      },
      "created_at": "Wed Feb 17 02:30:29 +0000 2016",
      "id": 699782944081252400,
      "id_str": "699782944081252352",
      "text": "RT @tavinho11_sfc: @LeandroAlmeida4 vem pro Peixe !!!",
      "source": "<a href="http://twitter.com/download/iphone" rel="nofollow">Twitter for iPhone</a>",
      "truncated": false,
      "in_reply_to_status_id": null,
      "in_reply_to_status_id_str": null,
      "in_reply_to_user_id": null,
      "in_reply_to_user_id_str": null,
      "in_reply_to_screen_name": null,
      "user":  {
        "id": 155768040,
        "id_str": "155768040",
        "name": "BROOKLYN 1986! ⓟ",
        "screen_name": "leolimaasep",
        "location": "Allianz Park, São Paulo ",
        "description": "⠀MEU PALMEIRAS LEVO NO PEITO ✺ MCMXIV ⠀⠀⠀⠀⠀Scoppia Che la Vittoria è Nostra!⠀⠀⠀⠀⠀⠀⠀Snapchat: leo.limaa56, 21 ⠀⠀@MiamiHeat || @SEPalmeiras || @Packers",
        "url": "https://t.co/EEmxbqK7NN",
        "entities":  {
          "url":  {
            "urls":  [
               {
                "url": "https://t.co/EEmxbqK7NN",
                "expanded_url": "http://www.instagram.com/leo.limaa",
                "display_url": "instagram.com/leo.limaa",
                "indices":  [
                  0,
                  23
                ]
              }
            ]
          },
          "description":  {
            "urls":  []
          }
        },
        "protected": false,
        "followers_count": 2642,
        "friends_count": 334,
        "listed_count": 9,
        "created_at": "Tue Jun 15 01:33:48 +0000 2010",
        "favourites_count": 12629,
        "utc_offset": -7200,
        "time_zone": "Brasilia",
        "geo_enabled": true,
        "verified": false,
        "statuses_count": 22661,
        "lang": "pt",
        "contributors_enabled": false,
        "is_translator": false,
        "is_translation_enabled": false,
        "profile_background_color": "022330",
        "profile_background_image_url": "http://pbs.twimg.com/profile_background_images/753715311/f206c6f87537082a7a154dbe2ee01dfa.jpeg",
        "profile_background_image_url_https": "https://pbs.twimg.com/profile_background_images/753715311/f206c6f87537082a7a154dbe2ee01dfa.jpeg",
        "profile_background_tile": false,
        "profile_image_url": "http://pbs.twimg.com/profile_images/696933526772568065/lU5bGJpE_normal.jpg",
        "profile_image_url_https": "https://pbs.twimg.com/profile_images/696933526772568065/lU5bGJpE_normal.jpg",
        "profile_banner_url": "https://pbs.twimg.com/profile_banners/155768040/1454997161",
        "profile_link_color": "0084B4",
        "profile_sidebar_border_color": "000000",
        "profile_sidebar_fill_color": "C0DFEC",
        "profile_text_color": "333333",
        "profile_use_background_image": true,
        "has_extended_profile": true,
        "default_profile": false,
        "default_profile_image": false,
        "following": false,
        "follow_request_sent": false,
        "notifications": false
      },
      "geo": null,
      "coordinates": null,
      "place": null,
      "contributors": null,
      "retweeted_status":  {
        "metadata":  {
          "iso_language_code": "pt",
          "result_type": "recent"
        },
        "created_at": "Tue Jan 20 01:11:00 +0000 2015",
        "id": 557344509726830600,
        "id_str": "557344509726830592",
        "text": "@LeandroAlmeida4 vem pro Peixe !!!",
        "source": "<a href="http://twitter.com/download/iphone" rel="nofollow">Twitter for iPhone</a>",
        "truncated": false,
        "in_reply_to_status_id": 495664213885734900,
        "in_reply_to_status_id_str": "495664213885734912",
        "in_reply_to_user_id": 104917980,
        "in_reply_to_user_id_str": "104917980",
        "in_reply_to_screen_name": "LeandroAlmeida4",
        "user":  {
          "id": 300286578,
          "id_str": "300286578",
          "name": "Tavinho",
          "screen_name": "tavinho11_sfc",
          "location": "Rio de Janeiro, Brasil",
          "description": "Nem melhor nem pior,apenas diferente.",
          "url": null,
          "entities":  {
            "description":  {
              "urls":  []
            }
          },
          "protected": false,
          "followers_count": 519,
          "friends_count": 535,
          "listed_count": 1,
          "created_at": "Tue May 17 14:22:56 +0000 2011",
          "favourites_count": 10004,
          "utc_offset": -7200,
          "time_zone": "Brasilia",
          "geo_enabled": true,
          "verified": false,
          "statuses_count": 30902,
          "lang": "pt",
          "contributors_enabled": false,
          "is_translator": false,
          "is_translation_enabled": false,
          "profile_background_color": "FA743E",
          "profile_background_image_url": "http://pbs.twimg.com/profile_background_images/570189460860461056/oDK91WvB.jpeg",
          "profile_background_image_url_https": "https://pbs.twimg.com/profile_background_images/570189460860461056/oDK91WvB.jpeg",
          "profile_background_tile": true,
          "profile_image_url": "http://pbs.twimg.com/profile_images/696183300969402368/9yxsV91D_normal.jpg",
          "profile_image_url_https": "https://pbs.twimg.com/profile_images/696183300969402368/9yxsV91D_normal.jpg",
          "profile_banner_url": "https://pbs.twimg.com/profile_banners/300286578/1453904269",
          "profile_link_color": "DD2E44",
          "profile_sidebar_border_color": "FFFFFF",
          "profile_sidebar_fill_color": "EFEFEF",
          "profile_text_color": "333333",
          "profile_use_background_image": true,
          "has_extended_profile": false,
          "default_profile": false,
          "default_profile_image": false,
          "following": false,
          "follow_request_sent": false,
          "notifications": false
        },
        "geo":  {
          "type": "Point",
          "coordinates":  [
            -22.90512006,
            -43.23253602
          ]
        },
        "coordinates":  {
          "type": "Point",
          "coordinates":  [
            -43.23253602,
            -22.90512006
          ]
        },
        "place":  {
          "id": "97bcdfca1a2dca59",
          "url": "https://api.twitter.com/1.1/geo/id/97bcdfca1a2dca59.json",
          "place_type": "city",
          "name": "Rio de Janeiro",
          "full_name": "Rio de Janeiro, Brazil",
          "country_code": "BR",
          "country": "Brasil",
          "contained_within":  [],
          "bounding_box":  {
            "type": "Polygon",
            "coordinates":  [
               [
                 [
                  -43.795449,
                  -23.08302
                ],
                 [
                  -43.0877068,
                  -23.08302
                ],
                 [
                  -43.0877068,
                  -22.7398234
                ],
                 [
                  -43.795449,
                  -22.7398234
                ]
              ]
            ]
          },
          "attributes":  {}
        },
        "contributors": null,
        "is_quote_status": false,
        "retweet_count": 3,
        "favorite_count": 1,
        "entities":  {
          "hashtags":  [],
          "symbols":  [],
          "user_mentions":  [
             {
              "screen_name": "LeandroAlmeida4",
              "name": "Leandro Almeida",
              "id": 104917980,
              "id_str": "104917980",
              "indices":  [
                0,
                16
              ]
            }
          ],
          "urls":  []
        },
        "favorited": false,
        "retweeted": false,
        "lang": "pt"
      },
      "is_quote_status": false,
      "retweet_count": 3,
      "favorite_count": 0,
      "entities":  {
        "hashtags":  [],
        "symbols":  [],
        "user_mentions":  [
           {
            "screen_name": "tavinho11_sfc",
            "name": "Tavinho",
            "id": 300286578,
            "id_str": "300286578",
            "indices":  [
              3,
              17
            ]
          },
           {
            "screen_name": "LeandroAlmeida4",
            "name": "Leandro Almeida",
            "id": 104917980,
            "id_str": "104917980",
            "indices":  [
              19,
              35
            ]
          }
        ],
        "urls":  []
      },
      "favorited": false,
      "retweeted": false,
      "lang": "pt"
    },
 */