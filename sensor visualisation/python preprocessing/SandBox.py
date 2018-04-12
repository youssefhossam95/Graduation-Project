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

def getRandomValue():
    # return 2 / 3 * random.uniform(0.3, 0.8) + 1 / 3 * random.uniform(1.4, 2.4)
    # return  np.random.normal(2 , 0.5)
    return 0.5
def augmentAnamoly (anamoly , timeFactor, accelFactor):
    newAnamoly=Anamoly(anamoly=anamoly)
    for i in range(len(newAnamoly.accelTime)):
        newAnamoly.accelValues[i] *= accelFactor
        newAnamoly.accelTime[i] /=timeFactor
    return newAnamoly


ploter.reviewMode = False
fileName= 'AllJsonFiles.txt'
rows = FH.loadObjFromFile(fileName)
plotingIndex = 0


while plotingIndex<len(rows) and plotingIndex>=0:
    anamoly = Anamoly(rows[plotingIndex]['value'])
    if(ploter.isLookingFor(anamoly.anamolyType)):
        
        anamolyArray = [(anamoly,'original')]
        ploter.plotMultipleAnamolies(anamolyArray , numberOfCols=1 , index=plotingIndex )
    if(ploter.endPloting):
        break
    plotingIndex += ploter.indexDirection




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
