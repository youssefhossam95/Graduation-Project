import requests
import json
import FileHandler  as FH

# gets all the data from the server and store it in the file named "recievedJson.txt"
#input: -----
#output: -----
def getDataFromServer():
    url = "https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps"
    print('sending the url')
    r = requests.get(url, auth=('somishopperchousesingetc', '6be49dadc1332531c1f128d871d02e05a5469f71'))
    print("got response " + str(r.status_code))
    obj = json.loads(r.text)
    FH.writeObjToFile("AllJsonFiles.txt" , obj['rows']) #store the rows comming from the server

#update a certain document in the server to have the value of JsonObj
#to do that the _rev parameter must be the same for JsonObj and the object that will be updated in the server
#input: JsonObj the new json object that will be sent to the DataBase
#output: server response
def updateValueInServer(JsonObj):
    url = 'https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/'
    r = requests.post(url, json=JsonObj, auth=('somishopperchousesingetc', '6be49dadc1332531c1f128d871d02e05a5469f71'))
    print('got Response', r.status_code)
    print('Response Data: ', r.text)
    return r

