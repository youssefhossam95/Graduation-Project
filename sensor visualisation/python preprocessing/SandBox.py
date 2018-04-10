import FileHandler as FH
import os
import math
from preprecessing import *
import Ploter
import Server
import json
import random
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


def loadDatasetFromFile(fileName):
    rows = FH.loadObjFromFile(fileName)
    m= len(rows)
    Tx = len(rows[0]['value']['accelValues'])
    X= np.zeros((m , Tx))
    Y = np.zeros((m , 1))
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