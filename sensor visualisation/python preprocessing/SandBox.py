import csv

import FileHandler as FH
from FileHandler import *
from preprecessing import *
import Ploter
from scipy import signal
import matplotlib.pyplot as plt
ploter= Ploter.Ploter()
import math


anamolyArray=[]
def ScalingFN():
    plotingIndex = 0
    anamolies=[]
    #Server.getDataFromServer()
    rows = FH.loadObjFromFile('AllJsonFiles.txt')
    scales=[1,21/14 ,1]
    for row in rows:
        if row['value']['anamolyType']==4:
            if plotingIndex==1281:
                anamolies.append((Anamoly(rows[plotingIndex]['value']),"zeby"+str(plotingIndex)+" batee2"))
            anamolies.append((Anamoly(rows[plotingIndex]['value']), "zeby" + str(plotingIndex)))
        plotingIndex=plotingIndex+1

    ploter.plotMultipleAnamoliesWithScaling(anamolies[0:3] , numberOfCols=1 , index=plotingIndex,scales=scales)

def sampleAndPad(srcFileName , sampledDataFileName):
    rows = FH.loadObjFromFile(srcFileName)
    newRows = []
    for row in rows:
        anamoly = Anamoly(row['value'])
        samplingRate = 50

        anamoly = sample(anamoly , samplingRate)
        anamoly = paddding(anamoly , windowSize=10  ,samplingRate=samplingRate)

        row['value']['accelTime'] = anamoly.accelTime
        row['value']['accelValues']= anamoly.accelValues

        newRows.append(row)

    FH.writeObjToFile(sampledDataFileName , newRows)

def printDataLengthes(fileName):
    rows = FH.loadObjFromFile(fileName)
    lengthes = []
    index = 0
    for row in rows:
        anamoly = Anamoly(JsonObj=row['value'])
        lengthes.append((len(anamoly.accelTime) , index) )
        index+=1

    lengthes.sort()
    print(lengthes)


def printAnamolyData(anamoly):
    print("ID: ",anamoly.id)
    print("rev: ",anamoly.rev)
    print("comment: ",anamoly.Comment)
    print("Type: " , ploter.getTypeName(anamoly.anamolyType))

# sampleAndPad('DevData.txt' , 'DevDataSampledRandomHalf.txt')


def showDifferenceInPadding() :

    rows = FH.loadObjFromFile('DevData.txt')
    rowsZeros = FH.loadObjFromFile('DevDataSampledZero.txt')
    rowsRandom2 = FH.loadObjFromFile('DevDataSampledRandom.txt')
    rowsRandom1 = FH.loadObjFromFile('DevDataSampledRandom1.txt')
    rowsRandomHalf = FH.loadObjFromFile('DevDataSampledRandomHalf.txt')


    for i in range(len(rows)):
        anamoly = Anamoly(JsonObj=rows[i]['value'])
        anamolyZeros = Anamoly(JsonObj=rowsZeros[i]['value'])
        anamolyRandom2 = Anamoly(JsonObj=rowsRandom2[i]['value'])
        anamolyRandom1 = Anamoly(JsonObj=rowsRandom1[i]['value'])
        anamolyRandomHalf = Anamoly(JsonObj=rowsRandomHalf[i]['value'])

        anamolyArray = [(anamoly, 'original')  , (anamolyZeros , 'Zeros')  ,
                        (anamolyRandomHalf , 'random var = 0.5'),(anamolyRandom1 , 'random var = 1'),
                        (anamolyRandom2 , 'random var = 2')]

        ploter.plotMultipleAnamolies(anamolyArray , 2 , i )

def visualizeSpectrogram():
    samplingRate = 50
    xTrain, yTrain, _, _ = loadDatasetFromFile('DevDataSampledZero.txt')
    for i in range(xTrain.shape[0]):
        f, t, Sxx = signal.spectrogram(xTrain[i][:], samplingRate, nperseg=150)
        ploter.dataVsSpectrogram(f, t, Sxx, samplingRate, xTrain[i][0:500], int(yTrain[i][0]))

def augmentAnamoly (anamoly , accelFactor, timeFactor=1):
    newAnamoly=Anamoly(anamoly=anamoly)
    for i in range(len(newAnamoly.accelTime)):
        newAnamoly.accelValues[i] *= accelFactor
        newAnamoly.accelTime[i] *=timeFactor
    return newAnamoly

def augmentAreaOfInterest(anamoly , accelFactor , timeFactor = 1):
    _,startIndex, endIndex = getAreaOfInterest(anamoly , 3)
    newAnamoly=Anamoly(anamoly=anamoly)
    for i in range(startIndex , endIndex):
        newAnamoly.accelValues[i] *= accelFactor
    for i in range(len(anamoly.accelTime)):
        newAnamoly.accelTime[i]*=timeFactor

    return newAnamoly


#crop the sides of the signal (NOT TESTED YET)
def cropSignal(anamoly, targetTime , samplingRate, periodOfInterest ):
    newAnamoly = Anamoly(anamoly=anamoly)
    anamolyMaxTime= np.max(anamoly.accelTime)
    numberOfSamplesToCrop = (anamolyMaxTime- targetTime) * samplingRate
    if(numberOfSamplesToCrop <= 0):
        return anamoly
    cropIndexStart = numberOfSamplesToCrop
    cropIndexEnd = len(anamoly.accelTime)-numberOfSamplesToCrop
    _, startInterestIndex , endInterestIndex = getAreaOfInterest(anamoly , periodOfInterest)

    if(cropIndexStart > startInterestIndex and cropIndexStart < endInterestIndex):
        newAnamoly.accelValues= anamoly.accelValues[:cropIndexEnd]
        newAnamoly.accelTime = anamoly.accelTime[:cropIndexEnd]

    elif( cropIndexEnd > startInterestIndex and cropIndexEnd < endInterestIndex):
        newAnamoly.accelValues = anamoly.accelValues[cropIndexStart:]
        newAnamoly.accelTime = anamoly.accelTime[cropIndexStart:]
    else:
        sumStart = np.sum(np.abs(newAnamoly.accelValues[:cropIndexStart]))
        sumEnd = np.sum(np.abs(newAnamoly.accelValues[:cropIndexStart]))
        if(sumStart>sumEnd):
            newAnamoly.accelValues = anamoly.accelValues[cropIndexStart:]
            newAnamoly.accelTime = anamoly.accelTime[cropIndexStart:]
        else:
            newAnamoly.accelValues = anamoly.accelValues[:cropIndexEnd]
            newAnamoly.accelTime = anamoly.accelTime[:cropIndexEnd]

    return newAnamoly

#perform Random Speed like augmentations
def augmentSpeed (anamoly, numberOfAugments , samplingRate=50 , anamolyArray=[]):
    maxAccel = 10
    minAccel = 2
    maxTime = 10
    minTime = 5

    anamolyMaxAccel = np.max(anamoly.accelValues)
    anamolyMaxTime = np.max(anamoly.accelTime)

    accelStep = (maxAccel-minAccel)/numberOfAugments
    accelAugmentTarget = [i for i in np.arange(minAccel, maxAccel, accelStep)]

    accelFactors = accelAugmentTarget / anamolyMaxAccel
    accelFactors = accelFactors[anamolyMaxTime / accelFactors  <= maxTime and anamolyMaxTime / accelFactors  >= minTime]


def augmentRandom(anamoly , numberOfAugments ,samplingRage=50, anamolyArray=[]):
    maxAccel = 10
    minAccel = 2
    maxTime=10
    minTime=5

    anamolyMaxAccel = np.max(anamoly.accelValues)
    anamolyMaxTime = np.max(anamoly.accelTime)

    accelAugmentTarget = [i for i in np.arange(minAccel, maxAccel , 1)]
    timeAugmentTarget = [i for i in np.arange(minTime , maxTime , 1)]

    accelFactors = accelAugmentTarget / anamolyMaxAccel
    timeFactors = timeAugmentTarget/anamolyMaxTime
    print(accelFactors)
    choosenPairs =[]
    while len(choosenPairs) < numberOfAugments:

        if(len(choosenPairs) > len(accelAugmentTarget) * len(timeAugmentTarget)): # if the number of augments required is larger
            break                                                                  #than the possible combinations
        accelIndex = np.random.randint(0,len(accelFactors)-1)
        timeIndex = np.random.randint(0,len(timeFactors)-1)

        if((accelIndex , timeIndex) not in choosenPairs):
            if(accelAugmentTarget[accelIndex] < anamolyMaxAccel):
                newAnamoly = augmentAnamoly(anamoly,accelFactors[accelIndex], timeFactors[timeIndex])
            else:
                newAnamoly = augmentAreaOfInterest(anamoly,accelFactors[accelIndex], timeFactors[timeIndex])
            print(accelFactors[accelIndex])
            title = 'augmented accel Factor: '+str(accelAugmentTarget[accelIndex]) + ' and accelTime: '+str(timeAugmentTarget[timeIndex])
            choosenPairs.append((accelIndex,timeIndex))
            anamolySampled = sample(newAnamoly, samplingRage)
            paddedAnamoly = padAuto(anamolySampled ,10,samplingRage)
            anamolyArray.append((paddedAnamoly,title))

def shifting(anamoly , timeShift , samplingRate):
    indexShifting = int(timeShift * samplingRate)
    newAnamoly = Anamoly(anamoly=anamoly)
    for i in range (len (newAnamoly.accelTime)):
        newAnamoly.accelValues[i] = anamoly.accelValues[(i+indexShifting)%len(newAnamoly.accelValues)]
    return newAnamoly


def analyzeSpeed ():
    ploter.reviewMode = False
    fileName= 'AllJsonFiles.txt'
    rows = FH.loadObjFromFile(fileName)
    plotingIndex = 0
    samplingRate = 50
    matabVars = []
    galatVars = []
    matabMeans = []
    galatMeans = []
    for row in rows:
        anamoly= Anamoly(JsonObj=row['value'])
        if(len(anamoly.speedValues)==0):
            continue
        speedVar = round(np.var(anamoly.speedValues)**1/2)
        speedMean= np.mean(anamoly.speedValues)
        if(anamoly.anamolyType == 0 and speedMean!=0):
            matabVars.append(speedVar)
            matabMeans.append(speedMean)
        else:
            galatVars.append(speedVar)
            galatMeans.append(speedMean)


    print(len(matabMeans))
    print(len(galatMeans))
    plt.subplot(121)
    binwidth = 1
    plt.hist(matabVars, bins=range( 0, 20 + binwidth, binwidth) )
    plt.subplot(122)
    plt.hist(galatVars, bins=range( 0, 20 + binwidth, binwidth) )
    plt.show()

def normalize(anamoly):
    newAnamoly = Anamoly(anamoly=anamoly)
    absAccels = [abs(number) for number in newAnamoly.accelValues]
    maxx = max(absAccels)
    newAnamoly.accelValues = np.array([x / maxx for x in newAnamoly.accelValues])
    return newAnamoly

def compineData(x_matab, y_matab, x_ghalat, y_ghalat , shuffle = True):
    y_matab = y_matab.reshape(y_matab.shape[0], 1)
    y_ghalat = y_ghalat.reshape(y_matab.shape[0], 1)
    x = np.vstack((x_matab, x_ghalat))
    y = np.vstack((y_matab, y_ghalat))
    if(shuffle):
        perm = np.random.permutation(len(x))
        x = x[perm, : ]
        y = y[perm, :]
    return x, y
#extract data set such that training and dev set come from different destribution
def extractDataSetDiffDist(fileName , areaOfInterest= False , Balanced = True):
    rows = loadObjFromFile(fileName)
    xTrainM =[]
    yTrainM = []
    xDevM = []
    yDevM =[]
    xTrainG = []
    yTrainG = []
    xDevG = []
    yDevG=[]
    random.seed(1)
    random.shuffle(rows)
    index =0
    for row in rows:

        if(index%100==0):
            print("finished:",index , "rows")
        index+=1
        if(index > 200 ):
            break
        anamoly = Anamoly(JsonObj=row["value"])

        if(len(anamoly.accelTime)<400):
            continue

        ######################## preprocessing ##########################
        # if we want area of interest we will normalize
        anamoly = preprossing(anamoly, smoothing=True , padding=True , areaOfInterest=areaOfInterest, normalizing=areaOfInterest)

        ######### elemenate anamolies with missing accel Values #########
        if(areaOfInterest):
            expectedAccelSamples=151
        else:
            expectedAccelSamples=501
        if(len(anamoly.accelValues)!= expectedAccelSamples):
            continue
        ######################  Training Set  ##########################

        if anamoly.anamolyType == 0 and "Reviewed" in row['value']:
            xTrainM.append(anamoly.accelValues)
            yTrainM.append(1)

        if(anamoly.anamolyType >0 and anamoly.anamolyType <4 and "test" not in anamoly.id):
            xTrainG.append(anamoly.accelValues)
            yTrainG.append(0)

        ######################## Dev Set ################################

        if anamoly.anamolyType == 0 and "test" in anamoly.id:
            xDevM.append(anamoly.accelValues)
            yDevM.append(1)

        if (anamoly.anamolyType > 0 and anamoly.anamolyType < 4 and "test" in anamoly.id):
            xDevG.append(anamoly.accelValues)
            yDevG.append(0)


    xTrainG = np.asarray(xTrainG)
    xTrainM = np.asarray(xTrainM)
    yTrainM = np.asarray(yTrainM)
    yTrainG = np.asarray(yTrainG)
    xDevG = np.asarray(xDevG)
    yDevG = np.asarray(yDevG)
    xDevM = np.asarray(xDevM)
    yDevM = np.asarray(yDevM)

    ######################## Balancing the Data ############################
    # assuming that galat is always larger than matab
    if(Balanced):
        trainPerm = np.random.permutation(len(xTrainM))
        devPerm = np.random.permutation(len(xDevM))
        xTrainG = xTrainG[trainPerm ]
        yTrainG = yTrainG[trainPerm  ]
        xDevG = xDevG[devPerm  ]
        yDevG= yDevG[devPerm  ]

    xTrain,yTrain = compineData(xTrainM , yTrainM , xTrainG,  yTrainG)
    xDev,yDev = compineData(xDevM , yDevM , xDevG,  yDevG)


    return xTrain , yTrain , xDev, yDev



rows = loadObjFromFile("AllJsonFilesLatest.txt")

anamoly = Anamoly(rows[20]['value'])

print(len(anamoly.accelValues))
print(np.mean(anamoly.accelValues))

with open('C:\\Users\\Waleed Mousa\\IdeaProjects\\PreProcessingFuctions\\accelValues.txt', 'w') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter=',')
    spamwriter.writerow(anamoly.accelValues)

with open('C:\\Users\\Waleed Mousa\\IdeaProjects\\PreProcessingFuctions\\accelTime.txt', 'w') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter=',')
    spamwriter.writerow(anamoly.accelTime)

#
# rows = loadObjFromFile("AllJsonFilesLatest.txt") ;
#
# for row in rows:
#     anamoly = Anamoly(row['value'])
#     if(len(anamoly.speedValues) ==0 or len(anamoly.accelValues) == 0):
#         continue
#     print(len(anamoly.speedValues))
#     for i in range(len(anamoly.speedValues)):
#         if(anamoly.speedTime[i] > anamoly.accelTime[0] and anamoly.speedTime[i] < anamoly.accelTime[len(anamoly.accelTime)-1]):
#             print("yes")
#     convertToRelativeTime(anamoly)
#     anamoly = sample(anamoly,50)
#     areaOfInterest , _ , _ = getAreaOfInterest( anamoly , 3 )
#     interestSpeed = getInterestSpeed(anamoly, 2 , 50)
#     meanSpeed = np.mean(anamoly.speedValues)
#     anamolyArray = [(anamoly , 'original, Mean: ' + str(meanSpeed)+ " Interest: " + str(interestSpeed)) ]
#
#     ploter.plotMultipleAnamolies(anamolyArray , 1)
#     if(ploter.endPloting):
#         break
#
# rows = loadObjFromFile("AllJsonFilesLatest.txt")
# mAvgAbs=[]
# gAvgAbs=[]
# mNumberOfPeaks=[]
# gNumberOfPeaks=[]
# index = 0
# for row in rows:
#     anamoly = Anamoly(row['value'])
#     if(len(anamoly.accelValues) < 400):
#         continue
#     if (index % 100 == 0):
#         print("finished:", index, "rows")
#     index += 1
#     # if(index > 1000):
#     #     break
#     preProAnamoly = preprossing(anamoly, smoothing=True, areaOfInterest=True , interestPeriod=2)
#     avgAbs = avgAbsRatio(anamoly,3)
#     peakCount = getNumberOfPeaks( preProAnamoly )
#     if(anamoly.anamolyType==0):
#         mAvgAbs.append(avgAbs)
#         mNumberOfPeaks.append(peakCount)
#     elif(anamoly.anamolyType<4):
#         gAvgAbs.append(avgAbs)
#         gNumberOfPeaks.append(peakCount)
#
#
# plt.subplot(221)
# plt.boxplot(mAvgAbs, showfliers=False)
# plt.title("Matab AVG ABS RAtio")
# plt.subplot(222)
# plt.boxplot(gAvgAbs,showfliers=False)
# plt.title("Ghalat AVG ABS RAtio")
#
#
#
# plt.subplot(223)
# plt.boxplot(mNumberOfPeaks, showfliers=False)
# plt.title("Matab number Of Peaks")
# plt.subplot(224)
# plt.boxplot(gNumberOfPeaks , showfliers=False)
# plt.title("Ghalat Number of peaks")
#
# plt.show()

# ploter.reviewMode = False
# fileName= 'AllJsonFiles.txt'
# rows = loadObjFromFile(fileName)
# plotingIndex = 90
# samplingRate = 50
# while plotingIndex<len(rows) and plotingIndex>=0:
#     anamoly = Anamoly(rows[plotingIndex]['value'])
#     convertToRelativeTime(anamoly)
#     anamolyArray = [ ]
#     if(ploter.isLookingFor(anamoly.anamolyType)):
#         anamolyArray.append((anamoly,'original'))
#         # augmentAuto(anamoly, 5 ,samplingRate, anamolyArray)
#         anamoly=preprossing(anamoly , smoothing=True , padding=True , shifting=True , areaOfInterest=True,normalizing=True)
#         anamolyArray.append((anamoly,'after preprocessing'))
#         ploter.plotMultipleAnamolies(anamolyArray , numberOfCols=1 , index=plotingIndex )
#
#     if(ploter.endPloting):
#         break
#     plotingIndex += ploter.indexDirection


# while plotingIndex<len(rows) and plotingIndex>=0:
#     anamoly = Anamoly(rows[plotingIndex]['value'])
#     convertToRelativeTime(anamoly)
#     if(ploter.isLookingFor(anamoly.anamolyType)):
#         max0 = np.max(anamoly.accelValues)
#         newAnamoly =augmentAnamoly(anamoly,2/max0 , 1)
#         anamolySampled=sample(newAnamoly,50)
#         paddedAnamoly = padAuto(anamolySampled ,10,50)
#         anamolyArray = [ (anamoly,'original'),(newAnamoly , 'augment area of interest'),
#                          (anamolySampled,'sample') , (paddedAnamoly, 'padding')]
#         ploter.plotMultipleAnamolies(anamolyArray , numberOfCols=2 , index=plotingIndex )
#     if(ploter.endPloting):
#         break
#     plotingIndex += ploter.indexDirection
#
#


#
# row = rows [3]['value'] #matab
# anamoly = Anamoly(JsonObj=row)
# counter = 0
# for i in range(10):
#     randomValue1 = 1
#     randomValue2 = 2
#     sampledAnamoly = sample(anamoly , 50)
#     newAnamoly = augmentAnamoly(sampledAnamoly , randomValue1 , randomValue2)
#     smoothedAnamoly = ApplySmoothingFilter(newAnamoly , 5)
#     augmentedTitle = 'augmented time:' + str(randomValue1) + ' accel: ' + str(randomValue2)
#     ploter.plotMultipleAnamolies([(anamoly,'original') , (sampledAnamoly ,'sampled ') ,
#                                   (smoothedAnamoly, 'smoothed'),(newAnamoly, augmentedTitle)])
#     if(ploter.endPloting):
#         break
# visualizeSpectrogram()