from __future__ import division
import numpy as np
from copy import  deepcopy as dc
import itertools as it
import FileHandler as FH
import random
import math
import Server
from scipy import  signal
from sklearn.neighbors import*
from sklearn.model_selection import train_test_split


from scipy.signal import butter, lfilter


def smoothFreq(sig,cutoff,fs):
    B, A = butter(1, cutoff / (fs / 2) , btype='low') # 1st order Butterworth low-pass
    filtered_signal = lfilter(B, A, sig, axis=0)
    absAvgBefore = np.mean(np.abs(sig))
    absAvgAfter = np.mean(np.abs(filtered_signal))
    #filtered_signal = filtered_signal * absAvgBefore / absAvgAfter
    return filtered_signal

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

            if("speedValues" in JsonObj):
                self.speedValues=np.array(JsonObj["speedValues"])
            else:
                self.speedValues=[]
            self.anamolyType = JsonObj["anamolyType"]
            self.Comment = JsonObj["Comment"]
            self.id = JsonObj["_id"]
            self.rev = JsonObj["_rev"]
            if("speedValues" in JsonObj):
                self.speedValues=np.array(JsonObj["speedValues"])
                self.speedTime = np.array(JsonObj["speedTime"])
            else:
                self.speedValues=np.array([])
                self.speedTime=np.array([])
            self.accelStartAbsTime =  JsonObj["accelTime"][0]

        elif(anamoly!=0):
            self.accelValues = dc(anamoly.accelValues)
            self.accelTime = dc(anamoly.accelTime)
            self.speedValues=dc(anamoly.speedValues)
            self.speedTime =dc(anamoly.speedTime)
            self.anamolyType = anamoly.anamolyType
            self.Comment = anamoly.Comment
            self.id = anamoly.id
            self.rev = anamoly.rev
            self.accelStartAbsTime = anamoly.accelStartAbsTime
def partialSmoothingFilter (anamoly , fsize , startIndex,endIndex):
    anamoly1=Anamoly(anamoly=anamoly)
    anamoly2=Anamoly(anamoly=anamoly)
    anamoly1.accelValues = anamoly1.accelValues[:startIndex]
    anamoly2.accelValues = anamoly2.accelValues[endIndex:]
    result1= ApplySmoothingFilter(anamoly1, fsize)
    result2=ApplySmoothingFilter(anamoly2 ,fsize)
    anamoly1.accelValues = np.append(result1.accelValues , np.append(anamoly.accelValues[startIndex:endIndex] , result2.accelValues))
    return anamoly1

#apply sommothing filter on anamoly
#input: Anamoly , filter size
#output: New anamoly after modification
def ApplySmoothingFilter (anamoly , fsize ) :
    maxBefore = np.sum(np.abs(anamoly.accelValues))
    Filter = []
    for i in range(1, fsize):
        Filter.append(1 / fsize)

    # Filter=signal.gaussian(fsize,1)
    newValues = np.convolve(anamoly.accelValues, Filter, 'same')

    maxAfter = np.sum(np.abs(newValues)) ;

    anamoly2 = Anamoly(anamoly=anamoly)
    anamoly2.accelValues = newValues
    anamoly2.accelValues = newValues*(maxBefore/maxAfter)
    return anamoly2

def getInterestSpeed (anamoly  , interestPeriod , samplingRate ):
    startOfAccelTime = anamoly.accelStartAbsTime
    _ ,startIndex , endIndex = getAreaOfInterest( anamoly , interestPeriod )
    absStartTime = startIndex * 1/samplingRate* 10**9 + startOfAccelTime
    absEndTime = endIndex * 1 / samplingRate * 10**9 + startOfAccelTime

    for i in range (len(anamoly.speedTime)):
        speedTime = anamoly.speedTime[i]
        if(speedTime>absStartTime and speedTime<absEndTime):
            return anamoly.speedValues[i]

    return np.mean(anamoly.speedValues);


#function to shift curve of accelerations to be around zero
#input: anamoly
#ouput: New shifted anamoly
def shiftCurve(anamoly , startIndex , endIndex):
    newAnamoly = Anamoly(anamoly=anamoly)
    numberOfSamples = len(newAnamoly.accelValues[:startIndex]) + len(newAnamoly.accelValues[endIndex:])
    mean = (sum(newAnamoly.accelValues[:startIndex]) + sum(newAnamoly.accelValues[endIndex:]))/numberOfSamples
    newAnamoly.accelValues -= mean;
    return newAnamoly

## perform integration on some sampled values
#input:  array of values to be integrated
#output: numpy array of values affter integration
def integrate (inputArray  , samplingRate):
    output = list(it.accumulate(inputArray));
    output = np.array(output)
    output /= samplingRate
# output= signal.detrend(output)
    return output

#get displacement form acceleration values
#input: anamoly: input anamoly with accelValues representing the acceleration values
#       anamolyArray: the array to which  the function append intermediate results, for later use or ploting
#output: anamoly:with accelValues representing the displacement values
def getPureDisplacement ( anamoly , samplingRate , anamolyArray=[]):

    anamolySpeed = Anamoly(anamoly=anamoly)
    _,start,end=getAreaOfInterest(anamoly,3)
    anamoly = shiftCurve(anamoly,start,end)
    anamolySpeed.accelValues = integrate(anamolySpeed.accelValues , samplingRate)
    anamolyArray.append((anamolySpeed,"Speed"))
    anamolySpeed.accelValues=signal.detrend(anamolySpeed.accelValues)
    anamolyDisp = Anamoly(anamoly=anamolySpeed)
    anamolyDisp.accelValues = integrate(anamolyDisp.accelValues , samplingRate)

    return anamolyDisp

#get displacement form acceleration values and perform shifting and smothing between different steps
#input: anamoly: input anamoly with accelValues representing the acceleration values
#       anamolyArray: the array to which  the function append intermediate results, for later use or ploting
#       smoothing window the window used in smoothing intermediate values
#output: anamoly:with accelValues representing the displacement values
def getDisplacement (anamoly, samplingRate , anamolyArray = [] , smoothingWindow=10 ):
    anamoly2 = ApplySmoothingFilter(anamoly, smoothingWindow)
    anamoly3 = shiftCurve(anamoly2);
    anamolyArray.append((anamoly3,"Shifting and smoothing"))

    anamolySpeed = Anamoly(anamoly=anamoly3)
    anamolySpeed.accelValues = list(it.accumulate(anamolySpeed.accelValues))
    anamolySpeed.accelValues /=samplingRate
    anamolyArray.append((anamolySpeed,"Speed"))

    anamolySpeedSifted = Anamoly(anamoly=anamolySpeed)
    anamolySpeedSifted = shiftCurve(anamolySpeedSifted)
    anamolyArray.append((anamolySpeedSifted,"Speed Shifting and smoothing"))

    anamolyDisp = Anamoly(anamoly=anamolySpeed)
    anamolyDisp.accelValues = list(it.accumulate(anamolyDisp.accelValues))
    anamolyDisp.accelValues /=samplingRate

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
#gets the indecies of the part of the signal with maximum absotute sum
def getAreaOfInterest(anamoly , periodOfInterest):
    convertToRelativeTime(anamoly)
    startIndex = 0
    endTime =  periodOfInterest
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


def padAuto(anamoly , endOfTime , samplingRate):
    _,startIndex, endIndex = getAreaOfInterest(anamoly , 3)
    accelArray = np.append(anamoly.accelValues[:startIndex] , anamoly.accelValues[endIndex:])
    if(len(accelArray > 2)):
        mean = np.mean(accelArray)
        var = np.var(accelArray)
    else:
        mean=0
        var =0.5
    # print(mean , var)
    return paddding(anamoly,endOfTime,samplingRate,mean , var)
def normalize(anamoly):
    newAnamoly=Anamoly(anamoly=anamoly)
    absAccels=[abs(number) for number in newAnamoly.accelValues]
    maxx=max(absAccels)
    newAnamoly.accelValues=[x /maxx  for x in newAnamoly.accelValues]
    return newAnamoly
def avgAbsRatio(anamoly , interestPeriod):
    _,start,end = getAreaOfInterest(anamoly, interestPeriod)
    avgAbsInterest = np.sum(np.abs(anamoly.accelValues[start:end]))/len(anamoly.accelValues[start:end])
    avgAbsTotal = np.sum(np.abs(anamoly.accelValues))/len(anamoly.accelValues)
    return avgAbsInterest/avgAbsTotal
def getNumberOfPeaks(anamoly):
    values = anamoly.accelValues
    peakCount = 0
    for i in range (1,len(values)-1):
        if((values[i]<values[i-1] and values[i]< values[i+1]) or (values[i]>values[i-1] and values[i]> values[i+1]) ):
            peakCount+=1
    return peakCount
def preprossing(anamoly, padding=False, shifting=False, smoothing=False, sFilterSize=5, areaOfInterest=False,
                interestPeriod=3, normalizing=False, samplingRate=50):
    newAnamoly = sample(anamoly, samplingRate)

    if (padding and not areaOfInterest):
        newAnamoly = padAuto(newAnamoly, endOfTime=10, samplingRate=samplingRate)

    if (smoothing):
        newAnamoly = ApplySmoothingFilter(newAnamoly, sFilterSize)

    if (shifting):
        _, start, end = getAreaOfInterest(newAnamoly, interestPeriod)
        newAnamoly = shiftCurve(newAnamoly, start, end)

    if (areaOfInterest):
        newAnamoly, _, _ = getAreaOfInterest(newAnamoly, interestPeriod)

    if (normalizing):
        newAnamoly = normalize(newAnamoly)


    return newAnamoly
            ##### code starts from here ######

#Server.getDataFromServer()
# def DTWDistance(s1, s2,w):
#     DTW={}
#
#     w = max(w, abs(len(s1)-len(s2)))
#
#     for i in range(-1,len(s1)):
#         for j in range(-1,len(s2)):
#             DTW[(i, j)] = float('inf')
#     DTW[(-1, -1)] = 0
#
#     for i in range(len(s1)):
#         for j in range(max(0, i-w), min(len(s2), i+w)):
#             dist= (s1[i]-s2[j])**2
#             DTW[(i, j)] = dist + min(DTW[(i-1, j)],DTW[(i, j-1)], DTW[(i-1, j-1)])
#
#     return math.sqrt(DTW[len(s1)-1, len(s2)-1])
#
#
# rows=FH.loadObjFromFile("MatabsJsonFiles.txt")
# count=0
# X=[]
# y=[]
# for row in rows:
#     if "Reviewed" not in row["value"]:
#         continue
#     anamoly=Anamoly(JsonObj=row["value"])
#     anamoly1=sample(anamoly,50)
#     anamoly2, _, _ = getAreaOfInterest(anamoly1, 3)
#     anamoly3=ApplySmoothingFilter(anamoly2,5)
#     if (len(anamoly3.accelValues) != 151):
#         continue
#     if anamoly.anamolyType==0:
#         y.append(1)
#     else:
#         y.append(0)
#     X.append(anamoly3.accelValues)
#
# X=np.array(X)
# y=np.array(y)
#
#
#
#
# t_size=20
# (Xtrain, Xtest, ytrain, ytest) = train_test_split(
#     X, y, test_size=t_size,random_state=100)
#
# knr=KNeighborsClassifier(n_neighbors=5,n_jobs=1,metric=DTWDistance,metric_params={"w":1000})
# print(Xtrain.shape)
# print(ytrain.shape)
# print("[KNN] Training")
# # TODO: Fit the model using trainFeatures and trainLabels
# tf=knr.fit(Xtrain, ytrain)
#
#
# print('get Score')
# # TODO: Get the accuracy of the model using model.score(). Pass testFeatures and testLabes as parameters.
# acc = tf.score(Xtest, ytest)

def normalize(anamoly):
  newAnamoly=Anamoly(anamoly=anamoly)
  absAccels=[abs(number) for number in newAnamoly.accelValues]
  maxx=max(absAccels)
  newAnamoly.accelValues=[x /maxx  for x in newAnamoly.accelValues]
  return newAnamoly