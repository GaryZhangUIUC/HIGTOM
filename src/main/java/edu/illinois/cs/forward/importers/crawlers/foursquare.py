from __future__ import absolute_import, print_function

import math
import json
import foursquare
from pymongo import MongoClient

#################Mongo Here##################
client = MongoClient('localhost', 27017)
db = client['SocialBuzz']
collection = db['FoursquareChicago']

    
def insert(data):
    #Remember to change collection when needed
    return db.FoursquareChicago.insert_one(data)

##############################################


# Construct the client object
client = foursquare.Foursquare(client_id='BDYBPYB1MDEDIGNYAQASE00HPRQGNXUSVG1HVHE1B5ZX3JPQ', client_secret='FL5O2XOF3HMOVAP05LIYYN3KBOBDBBEXZTKMYEUYVIMOYTD3', redirect_uri='http://fondu.com/oauth/authorize')

#client.venues.search(params={'sw':'41.8768736,-87.6358723', 'ne':'41.8830663,-87.63293', 'intent':'browse'})
#params={'sw':'41.8768736,-87.6358723', 'ne':'41.8830663,-87.63293', 'intent':'browse'}
#client.venues.tips('4b60d577f964a52061fc29e3', params = {'limit':'50'})
#params = {'limit':'5'}

def geoSearch(geo=None):

    venues = client.venues.search(params={'sw':'41.8768736,-87.6358723', 'ne':'41.8830663,-87.63293', 'intent':'browse', 'limit':'50'})

    for venue in venues['venues']:

        venue_id = venue['id']
        venue_id = json.dumps(venue_id)

        location = venue['location']
        lon = venue['location']['lat']
        lng = venue['location']['lng']

        tips = client.venues.tips(venue_id, params = {'limit':'50'})

        for tip in tips['tips']['items']:
            tip['location'] = location

            insert(tip)
            # or we can just dump to file
            #print json.dumps(tip)

        
    
def non_recursive_square(s, x1, y1, x2, y2):
    #When square, rad^2 = (s/2)^2 * 2 ==> rad = sqrt(2)/2*s
    rad = math.sqrt(2)/2*s
    center_x = x1 + (x2 - x1)/2
    center_y = y1 + (y2 - y1)/2
    
    geodata = str(center_x) + ',' + str(center_y) + ',' + str(rad) + 'km'
    geoSearch(geodata)
    
    
def non_recursive_rec(r, x1, y1, x2, y2):
    # r is already rad
    rad = r
    center_x = x1 + (x2 - x1)/2
    center_y = y1 + (y2 - y1)/2
    
    geodata = str(center_x) + ',' + str(center_y) + ',' + str(rad) + 'km'
    geoSearch(geodata)
    
    


if __name__ == '__main__':
    
    #Chicago Loop
    S_lat = 41.84465
    S_long = -87.64500
    N_lat = 41.91660
    N_long = -87.59671
    #Assumming it is a 2*1 rectangle. 0.07195 * 0.04829 (8km*4km)


    geoSearch()




