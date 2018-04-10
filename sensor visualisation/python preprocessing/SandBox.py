import FileHandler as FH

from preprecessing import *
import Ploter
import Server
import json
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

def checkSampling():
    rows = FH.loadObjFromFile('AllJsonFiles.txt')
    Lengthes =[]

    for row in rows:
        anamoly = Anamoly(row['value'])
        # print(len(anamoly.accelTime))


        samplingRate = 2
        samplingTime = 1/samplingRate

        convertToRelativeTime(anamoly)
        anamoly=sample(anamoly , samplingRate)

        maxTime = max(anamoly.accelTime)
        maxTime+= samplingTime
        times = []
        values = []
        while maxTime <= 15:
            times.append(maxTime)
            values.append(0)
            maxTime+=samplingTime

        # print(times)
        anamoly.accelTime= np.append(anamoly.accelTime , times)
        anamoly.accelValues=np.append(anamoly.accelValues, values)

        Lengthes.append(len(anamoly.accelTime))
       # print(len(anamoly.accelTime))
       #  ploter.plotAnamoly(anamoly)

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



rows = FH.loadObjFromFile('AllJsonFiles.txt')
print(len(rows))
rows.remove(rows[1490])
print(len(rows))
