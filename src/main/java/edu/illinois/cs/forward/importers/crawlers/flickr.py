from __future__ import absolute_import, print_function

import math
import json
import xmltodict
import flickr_api
from flickr_api.api import flickr 
from pymongo import MongoClient

#################Mongo Here##################
client = MongoClient('localhost', 27017)
db = client['SocialBuzz']
collection = db['FlickrChicago']

    
def insert(data):
    #Remember to change collection when needed
    return db.FlickrChicago.insert_one(data)

##############################################


# Construct the client object
client = flickr_api.set_keys(api_key = '724f688166820f8005abceb30eb44421', api_secret = '940901eb0547b562')

def geoSearch(geo=None):


    photos = flickr.photos.search(lat=41.8830663, lon =-87.63293, radius = 20)
    photos = xmltodict.parse(photos)

    for photo in photos['photos']['photo']:

        photo_id = photo['@id']
        photo_id = json.dumps(photo_id)

        #location = venue['location']
        #lon = venue['location']['lat']
        #lng = venue['location']['lng']

        #comments = flickr.photos.comments.getList(lat=41.8830663, lon =-87.63293, radius = 20)
        info = flickr.photos.getInfo(photo_id = photo_id)
        info = xmltodict.parse(info)
        description = info['photo']['description']

        location = flickr.photos.geo.getLocation(photo_id = photo_id)
        location = xmltodict.parse(location)
        location = info['photo']['location']


        temp = {}
        temp['text'] = description
        temp['location'] = location

        insert(temp)
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




