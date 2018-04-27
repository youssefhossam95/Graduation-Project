import FileHandler as FH
from preprecessing import *
import Ploter
ploter= Ploter.Ploter()

ploter.reviewMode = False
fileName= 'MatabsJsonFiles.txt'
rows = FH.loadObjFromFile(fileName)
plotingIndex = 10
samplingRate = 50


while plotingIndex<len(rows) and plotingIndex>=0:
    anamoly = Anamoly(rows[plotingIndex]['value'])
    convertToRelativeTime(anamoly)
    if(ploter.isLookingFor(anamoly.anamolyType)):
        # augmentAuto(anamoly, 5 ,samplingRate, anamolyArray)
        anamoly= sample(anamoly , samplingRate)
        anamolyArray = [(anamoly, 'original')]
        SmoothedAnamoly=ApplySmoothingFilter(anamoly , 5)
        anamolyArray.append((SmoothedAnamoly , 'smoothing'))
        _,startIndex, endIndex = getAreaOfInterest(SmoothedAnamoly,3)
        anamolyShifted =  shiftCurve(SmoothedAnamoly , startIndex , endIndex)
        anamolyArray.append((anamolyShifted, 'shifted Anamoly Smoothed'))
        anamolyDisplacement = getPureDisplacement(anamolyShifted , samplingRate , anamolyArray)
        anamolyArray.append((anamolyDisplacement , "Displacement"))
        ploter.plotMultipleAnamolies(anamolyArray , numberOfCols=2 , index=plotingIndex )

    if(ploter.endPloting):
        break
    plotingIndex += ploter.indexDirection

