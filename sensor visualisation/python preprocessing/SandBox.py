import FileHandler as FH
from preprecessing import *
import Ploter
from scipy import signal
import matplotlib.pyplot as plt
ploter= Ploter.Ploter()
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
        f, t, Sxx = signal.spectrogram(xTrain[i][:], samplingRate, nperseg=10)
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

def padAuto(anamoly , endOfTime , samplingRate):
    _,startIndex, endIndex = getAreaOfInterest(anamoly , 3)
    accelArray = np.append(anamoly.accelValues[:startIndex] , anamoly.accelValues[endIndex:])
    if(len(accelArray > 2)):
        mean = np.mean(accelArray)
        var = np.var(accelArray)
    else:
        mean=0
        var =0.5
    print(mean , var)
    return paddding(anamoly,endOfTime,samplingRate,mean , var)

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



ploter.reviewMode = False
fileName= 'AllJsonFiles.txt'
rows = FH.loadObjFromFile(fileName)
plotingIndex = 0
samplingRate = 50
matabVars = []
galatVars = []
for row in rows:
    anamoly= Anamoly(JsonObj=row)



# ploter.reviewMode = False
# fileName= 'AllJsonFiles.txt'
# rows = FH.loadObjFromFile(fileName)
# plotingIndex = 10
# samplingRate = 50
#
#
# while plotingIndex<len(rows) and plotingIndex>=0:
#     anamoly = Anamoly(rows[plotingIndex]['value'])
#     convertToRelativeTime(anamoly)
#     anamolyArray = [ ]
#     if(ploter.isLookingFor(anamoly.anamolyType)):
#         # augmentAuto(anamoly, 5 ,samplingRate, anamolyArray)
#         anamoly= sample(anamoly , samplingRate)
#         SmoothedAnamoly=ApplySmoothingFilter(anamoly , 5)
#         speedMean= np.mean(anamoly.speedValues)
#         speedVar = np.var(anamoly.speedValues)
#         anamolyArray.append((SmoothedAnamoly , 'Avg Speed: '+ str(speedMean) + " Var: "+ str(speedVar)))
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
