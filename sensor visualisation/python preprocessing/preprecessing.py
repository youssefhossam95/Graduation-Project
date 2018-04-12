import numpy as np
from copy import  deepcopy as dc
import itertools as it
import FileHandler as FH
import random
import math


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



#class that represent anamolies can be initiated with json object or another anamoly object
class Anamoly:
    def __init__(self , JsonObj = 0 , anamoly = 0  ):
        if(JsonObj != 0 ):
            self.accelValues = np.array(JsonObj["accelValues"])
            self.accelTime = np.array(JsonObj["accelTime"])
            self.anamolyType = JsonObj["anamolyType"]
            self.Comment = JsonObj["Comment"]
            self.id = JsonObj["_id"]
            self.rev = JsonObj["_rev"]
        elif(anamoly!=0):
            self.accelValues = dc(anamoly.accelValues)
            self.accelTime = dc(anamoly.accelTime)
            self.anamolyType = anamoly.anamolyType
            self.Comment = anamoly.Comment
            self.id = anamoly.id
            self.rev = anamoly.rev

#apply sommothing filter on anamoly
#input: Anamoly , filter size
#output: New anamoly after modification
def ApplySmoothingFilter (anamoly , fsize) :
    maxBefore = np.amax(anamoly.accelValues)
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

    anamolySpeedSifted = Anamoly(anamoly=anamolySpeed)
    anamolySpeedSifted = shiftCurve(anamolySpeedSifted)
    anamolyArray.append((anamolySpeedSifted,"Speed Shifting and smoothing"))

    anamolyDisp = Anamoly(anamoly=anamolySpeed)
    anamolyDisp.accelValues = list(it.accumulate(anamolyDisp.accelValues))

    return anamolyDisp

def interpolate (y1 , x1 , y0 , x0 , x):
    return y0 + (x-x0)* (y1-y0)/ (x1-x0)


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
    convertToRelativeTime(anamoly)
    startIndex = 0
    endTime =  windowSize
    accel = anamoly.accelValues
    time = np.array(anamoly.accelTime)

    maxSum = 0 #the maximum sum of abselutes
    maxStart = 0 #the index of the start of the max Sum window
    maxEnd = 0
    index = 0 #index of the acceleration/time value
    tempTime = 0 #time start from 0

    while tempTime < endTime and index < len(time):
        maxSum += abs(accel[index])
        tempTime=time[index]
        index+=1

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

    print(maxStart , ' ' , maxEnd)
    newAnamoly = Anamoly(anamoly=anamoly)
    newAnamoly.accelTime= time[maxStart:maxEnd]-time[maxStart]
    newAnamoly.accelValues=accel[maxStart:maxEnd]
    return newAnamoly , maxStart , maxEnd

def convertToRelativeTimeWithScales(anamoly,scale):
    timeArray = anamoly.accelTime
    ref = timeArray[0]
    if(ref == 0): #so if it was called twice by mistake nothing wrong happens
        return
    timeArray = np.array(timeArray)
    relativeTime  = (timeArray-ref) / pow(10, 9)
    relativeTime=relativeTime/scale
    anamoly.accelTime  = relativeTime

def paddding( anamoly , endOfTime , samplingRate , mean , var):
    newAnamoly = Anamoly(anamoly=anamoly)
    samplingTime = 1 / samplingRate
    convertToRelativeTime(anamoly)

    maxTime = max(newAnamoly.accelTime)
    maxTime += samplingTime
    times = []
    values = []
    while maxTime <= endOfTime:
        times.append(maxTime)
        #np.random.normal(0 , 0.5)
        values.append(np.random.normal(mean, var))
        maxTime += samplingTime

    # print(times)
    newAnamoly.accelTime = np.append(newAnamoly.accelTime, times)
    newAnamoly.accelValues = np.append(newAnamoly.accelValues, values)

    return newAnamoly

def loadDatasetFromFile(fileName):
    rows = FH.loadObjFromFile(fileName)
    m= len(rows)
    Tx = len(rows[0]['value']['accelValues'])
    X= np.zeros((m , Tx))
    Y = np.zeros((m , 1))
    random.seed(1)
    random.shuffle(rows)
    trainingPrecentage = 0.8
    trainingIndex = math.ceil(trainingPrecentage * m)
    for i in range(len(rows)):
        X[i][:] = np.array(rows[i]['value']['accelValues'])
        Y[i] = rows[i]['value']['anamolyType']==0
    xTrain= X[0:trainingIndex][:]
    yTrain =Y[0:trainingIndex]
    xTest = X[trainingIndex:][:]
    yTest =Y[trainingIndex:]

    return xTrain , yTrain , xTest , yTest


##### code starts from here ######

