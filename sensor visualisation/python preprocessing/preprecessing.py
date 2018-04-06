import  json
import  requests
import time
import matplotlib.pyplot as plt
import numpy as np
from copy import  deepcopy as dc
import itertools as it
import math
from pynput import keyboard
import sys


#this should be called everytime you create a new plot
# 1 to move to the previous anamoly
# 2 to move to the next anamoly
# q to exit the
def setKeyPressHandler():
    fig, ax = plt.subplots()
    fig.canvas.mpl_connect('key_press_event', press)

def press(event):
    global indexDirction
    global endPloting
    global lookingFor
    print('press', event.key)
    sys.stdout.flush()
    if event.key == '1':
        indexDirction = -1 #change the direction to be negative i.e. move backwards
        plt.close()
    elif event.key == '2':
        indexDirction = 1#change the direction to be positive i.e. move forward
        plt.close()

    elif event.key == 'q':
        endPloting = True
    else:
        lookingFor = event.key


# writes some data to a file
#input: fileName: string of the name of the file
#       content: string containing the data to be written ;
#output: -----
def writeToFile( fileName , content):
    f = open(fileName, "w+", encoding='utf8')
    f.write(content)
    print("wrote to file" + fileName)
    f.close()
# writes some data to a file
#input: fileName: string of the name of the file
#       jsonObjArray: array containing the json objects to be written to the file ;
#output: -----
def storeJsonArray (fileName , jsonObjArray ):
    ArrayString = "[" ;
    for json in jsonObjArray:
        ArrayString += (str(json) + ",")
    ArrayString= ArrayString[: len(ArrayString)-1] + "]" # overwrite the last elemet which will be the ',' with ']'
    writeToFile(fileName, ArrayString)


# gets all the data from the server and store it in the file named "recievedJson.txt"
#input: -----
#output: -----
def getDataFromServer():
    url = "https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps"
    print('sending the url')
    r = requests.get(url, auth=('somishopperchousesingetc', '6be49dadc1332531c1f128d871d02e05a5469f71'))
    print("got response " + str(r.status_code))
    writeToFile("recievedJson.txt" , r.text)

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

def getDataForTest():
    f = open("testFile.txt", "r+", encoding='utf8')
    x = f.read()
    print("file is read")
    print ( x )
    obj = json.loads(x.replace("'" , "\""))
    print("object is loaded")
    return obj
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
#input: Anamoly Array // array of tubles of anamolies to be printed and thier titles
#output: -----
def plotMultipleAnamolies(anamolyArray , numberOfCols =2 , index=""  ):
    numberOfPlots = len(anamolyArray)
    numberOfRows = math.ceil(numberOfPlots/numberOfCols)
    plotNumber = 1
    for i in range (0 , numberOfPlots):
        subPlotNumber= numberOfRows*100 + numberOfCols * 10 + plotNumber
        plotNumber =  plotNumber + numberOfCols
        plotNumberMod = plotNumber%(numberOfCols*numberOfRows+1)
        if (plotNumber != plotNumberMod ):
            plotNumber = plotNumberMod + 2
        else :
            plotNumber = plotNumberMod

        anamoly , title = anamolyArray[i]
        plt.subplot(subPlotNumber)
        plt.plot(anamoly.accelTime , anamoly.accelValues)
        plt.title(title)
        plt.grid()

    figManager = plt.get_current_fig_manager()
    figManager.window.state('zoomed')
    plt.suptitle(getTypeName(anamoly.anamolyType)+" "+ str(index))
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
    #
    Filter = []
    for i in range(1, fsize):
        Filter.append(1 / fsize)

    newValues = np.convolve(anamoly.accelValues, Filter, 'same')
    maxAfter = np.amax(newValues)

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

def getDisplacement (anamoly , anamolyArray = [] , smoothingWindow=10 ):
    anamoly2 = ApplySmoothingFilter(anamoly, smoothingWindow)
    anamoly3 = shiftCurve(anamoly2);
    anamolyArray.append((anamoly3,"Shifting and smoothing"))

    anamolySpeed = Anamoly(anamoly=anamoly3)
    anamolySpeed.accelValues = list(it.accumulate(anamolySpeed.accelValues))
    anamolyArray.append((anamolySpeed,"Speed"))

    # anamolySpeedSifted = Anamoly(anamoly=anamolySpeed)
    # anamolySpeedSifted = shiftCurve(anamolySpeedSifted)
    # anamolyArray.append((anamolySpeedSifted,"Speed Shifting and smoothing"))

    anamolyDisp = Anamoly(anamoly=anamolySpeed)
    anamolyDisp.accelValues = list(it.accumulate(anamolyDisp.accelValues))

    return anamolyDisp

def interpolate (y1 , x1 , y0 , x0 , x):
    return y0 + (x-x0)* (y1-y0)/ (x1-x0);
def sample( anamoly , samplingRate = 1  ) :
    convertToRelativeTime(anamoly)
    sampledAnamoly = Anamoly(anamoly=anamoly)
    sampledAnamoly.accelTime=[]
    sampledAnamoly.accelValues= []
    samplingTime = 1/samplingRate ;
    time = 0
    index = 0
    while (time<max(anamoly.accelTime)):
        while (anamoly.accelTime[index] < time):
            index+=1
        if(anamoly.accelTime[index] == time):
            sampledAnamoly.accelValues.append(anamoly.accelValues[index])
        else:
            y1=anamoly.accelValues[index];
            x1=anamoly.accelTime[index];
            y0 = anamoly.accelValues[index-1];
            x0 = anamoly.accelTime[index-1];
            x = time ;
            sampledAnamoly.accelValues.append(interpolate(y1,x1 ,y0,x0 ,x))

        sampledAnamoly.accelTime.append(time)
        time+=samplingTime
    return sampledAnamoly

def getAreaOfInterest(anamoly , windowSize):
    startIndex = 0
    endTime =  windowSize
    accel = anamoly.accelValues
    time = anamoly.accelTime

    maxSum = 0 #the maximum sum of abselutes
    maxStart = 0 #the index of the start of the max Sum window
    maxEnd = 0
    index = 0 #index of the acceleration/time value
    tempTime = 0 #time start from 0

    while tempTime <= endTime and index < len(time):
        maxSum += abs(accel[index])
        index+=1
        tempTime=time[index]

    endIndex = index # now start and end are start and end indices of the window
    maxEnd= endIndex
    tempSum = maxSum # the summation of the current window abs

    while(endIndex<len(time)-1):
        endIndex+=1
        tempSum += abs(accel[endIndex])
        tempSum -= abs(accel[startIndex])
        startIndex+=1
        if(tempSum>maxSum):
            maxSum=tempSum
            maxStart= startIndex
            maxEnd = endIndex

    newAnamoly = Anamoly(anamoly=anamoly)
    newAnamoly.accelTime=time[maxStart:maxEnd] - time[maxStart]
    newAnamoly.accelValues=accel[maxStart:maxEnd]
    return newAnamoly

#check if the type of the anamoly is the type that the user wants
#input: lookingFor: char representing what the user is looking for
#       anamolyType: int the type of the current anamoly
#output: boolean true if type of anamoly is what we are looking OR it is not in the list for else false
def isLookingFor(lookingFor , type):
#   names = ["MATAB", 'HOFRA', 'GALAT', 'TAKSER', 'UNKNWON']
#   type will be as the index of the names array
    types = ['m', 'h' ,'g', 't', 'u']
    if(lookingFor in types):
        return types.index(lookingFor)==type
    else:
        return True



##### code starts from here ######



# rows = getStoredJsonRows() ;
#
# storeJsonArray("testFile.txt" , [rows[0] , rows[1] , rows[2]])
rows = getStoredJsonRows()
plotingIndex = 1
endPloting = False
setKeyPressHandler()
lookingFor = 'a'
indexDirction = 1 # 1 for positive direction and -1 for negative
while ( True ):
    anamolyArray = [];
    anamoly = Anamoly(JsonObj=rows[plotingIndex]['value'])
    if(isLookingFor(lookingFor , anamoly.anamolyType)):
        convertToRelativeTime(anamoly)
        anamolyArray.append((anamoly, 'original'))
        sampledAnamoly = sample(anamoly, 40)
        shiftedAnamoly = shiftCurve(sampledAnamoly)
        interestAnamoly = getAreaOfInterest(anamoly,2)
        anamolyArray.append((interestAnamoly , "Interest"))
        anamolyDisplacement= getDisplacement ( interestAnamoly)
        anamolyArray.append((anamolyDisplacement,"displacement"))
        plotMultipleAnamolies(anamolyArray , numberOfCols=1 , index=plotingIndex )
        if(endPloting):
            break
        setKeyPressHandler()

    plotingIndex += indexDirction
    if(plotingIndex > len(rows) or plotingIndex < 0):  # if out of the bounds of the rows array
        plotingIndex-=indexDirction  # reverse the last step so nothing will happen in the algorithm

# anamolyArray.append( ( Anamoly(JsonObj=rows[0]['value']), 'no' )  )
# anamolyArray.append( (Anamoly(JsonObj=rows[1]['value']) , 'yes') )
#
# plotMultipleAnamolies(anamolyArray)

#for loop on the index of rows you wnat to apply preprocessing on
# for i in range (100 , 120):
#     anamoly = Anamoly(JsonObj=rows[i]['value'])
#     convertToRelativeTime(anamoly)
#
#
#     anamolyArray = []
#     anamolyArray.append((anamoly , "Original"))
#
#     sampledAnamoly = sample(anamoly,30) ;
#     anamolyArray.append((sampledAnamoly,"Sampling"))
#     anamolyDisp = getDisplacement(anamoly , anamolyArray)
#     anamolyArray.append((anamolyDisp , "Displacement"))
#     plotMultipleAnamolies(anamolyArray) ;

