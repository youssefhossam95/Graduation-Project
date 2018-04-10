import FileHandler as FH

from preprecessing import *
import Ploter
import Server
import json
ploter= Ploter.Ploter()
ploter.reviewMode = True
anamolyArray=[]
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

    # elif(plotingIndex == len(rows)-1 or plotingIndex == 0):
    #     break
