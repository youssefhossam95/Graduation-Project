import  json
import  requests
import time
import matplotlib.pyplot as plt
import numpy as np
from copy import  deepcopy as dc
import itertools as it

# gets all the data from the server and store it in the file named "recievedJson.txt"
#input: -----
#output: -----
def getDataFromServer():
    url = "https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps"
    print('sending the url')
    r = requests.get(url, auth=('somishopperchousesingetc', '6be49dadc1332531c1f128d871d02e05a5469f71'))
    print("got response " + str(r.status_code))
    f = open("recievedJson.txt", "w+", encoding='utf8')
    f.write(r.text)
    print("wrote to file!")
    f.close()

# gets the local stored json rows
#input: -----
#output: -----
def getStoredJsonRows():
    f = open("recievedJson.txt", "r+", encoding='utf8')
    x = f.read()
    print("file is read")
    obj=  json.loads(x)
    print("object is loaded")
    return obj["rows"]

# converts time stamps stored in anamoly into range 0 -> end of time (10)
#input: Anamoly
#output: -----
def convertToRelativeTime(anamoly):
    timeArray = anamoly.accelTime
    ref = timeArray[0]
    if(ref == 0): #so if it was called twice by mistake nothing wrong happens
        return
    timeArray = np.array(timeArray)
    relativeTime  = (timeArray-ref) / pow(10, 9)
    anamoly.accelTime  = relativeTime

# converts anamoly type which is an INT into a string representing this INT
#input: INT Type
#output: string Type name
def getTypeName(type):
    names =["MATAB" , 'HOFRA' ,'GALAT', 'TAKSER' ,'UNKNWON']
    return names[type]

#plot a single anamoly
#input: Anamoly
#output: -----
def plotAnamoly (anamoly):
    plt.plot(anamoly.accelTime, anamoly.accelValues)
    plt.title(getTypeName(anamoly.anamolyType))
    plt.show()

#plot multiple anamolies in vertical sub plots  represented by anamolyArray
#input: Anamoly Array
#output: -----
def plotMultipleAnamolies(anamolyArray):
    numberOfPlots = len(anamolyArray)
    for i in range (0 , numberOfPlots):
        subPlotNumber= numberOfPlots*100 + 10 +i+1
       # subPlotNumber = numberOfPlots/2*100 + 20 + i +1
        print(subPlotNumber)
        plt.subplot(subPlotNumber)
        plt.plot(anamolyArray[i].accelTime , anamolyArray[i].accelValues)
        plt.title(getTypeName(anamolyArray[i].anamolyType)+" "+str(i))

    figManager = plt.get_current_fig_manager()
    figManager.window.state('zoomed')
    plt.show()

#class that represent anamolies can be initiated with json object or another anamoly object
class Anamoly:
    def __init__(self , JsonObj = 0 , anamoly = 0  ):
        if(JsonObj != 0 ):
            self.accelValues = JsonObj["accelValues"]
            self.accelTime = JsonObj["accelTime"]
            self.anamolyType = JsonObj["anamolyType"]
            self.Comment = JsonObj["Comment"]
        elif(anamoly!=0):
            self.accelValues = dc(anamoly.accelValues)
            self.accelTime = dc(anamoly.accelTime)
            self.anamolyType = anamoly.anamolyType
            self.Comment = anamoly.Comment

#apply sommothing filter on anamoly
#input: Anamoly , filter size
#output: New anamoly after modification
def ApplySmoothingFilter (anamoly , fsize) :
    maxBefore = np.amax(anamoly.accelValues)
    print("max before conv", maxBefore)
    #
    Filter = []
    for i in range(1, fsize):
        Filter.append(1 / fsize)

    newValues = np.convolve(anamoly.accelValues, Filter, 'same')
    maxAfter = np.amax(newValues)
    print("max after conv", maxAfter)

    anamoly2 = Anamoly(anamoly=anamoly)
    anamoly2.accelValues = newValues * maxBefore / maxAfter;

    return anamoly2

#function to shift curve of accelerations to be around zero
#input: anamoly
#ouput: New shifted anamoly
def shiftCurve(anamoly):


    newAnamoly = Anamoly(anamoly=anamoly)

    mean = sum(newAnamoly.accelValues)/len(newAnamoly.accelValues) ;
    newAnamoly.accelValues -= mean;
    return newAnamoly


##### code starts from here ######

rows = getStoredJsonRows() ;

#for loop on the index of rows you wnat to apply preprocessing on
for i in range (100 , 120):
    anamoly = Anamoly(JsonObj=rows[i]['value'])
    convertToRelativeTime(anamoly)

    anamoly2 = ApplySmoothingFilter(anamoly , 10)
    anamoly3 = shiftCurve(anamoly2) ;

    anamolySpeed = Anamoly(anamoly=anamoly3)
    anamolySpeed.accelValues = list(it.accumulate(anamolySpeed.accelValues))

    anamolySpeedSifted = Anamoly(anamoly=anamolySpeed)
    anamolySpeedSifted = shiftCurve(anamolySpeedSifted)

    anamolyDisp = Anamoly(anamoly=anamolySpeedSifted)
    anamolyDisp.accelValues = list(it.accumulate(anamolyDisp.accelValues))

    anamolyArray = [anamoly , anamoly2 , anamoly3, anamolyDisp] ;
    plotMultipleAnamolies(anamolyArray) ;


# anamoly = Anamoly(JsonObj=rows[4]['value'])
# convertToRelativeTime(anamoly)
#
# anamoly2 = ApplySmoothingFilter(anamoly , 10 )
# anamoly3 = shiftCurve(anamoly2) ;
#
# anamoly4 = Anamoly(anamoly=anamoly3)
# anamoly4.accelValues = list(it.accumulate(anamoly4.accelValues))
#
# anamolyArray = [anamoly , anamoly2 , anamoly3  ,anamoly4] ;
# plotMultipleAnamolies(anamolyArray) ;


