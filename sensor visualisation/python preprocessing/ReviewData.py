import FileHandler as FH
from preprecessing import Anamoly
import Ploter
import Server
import json

ploter= Ploter.Ploter()
ploter.reviewMode = False
anamolyArray=[]
rows = FH.loadObjFromFile('AllJsonFiles.txt')
plotingIndex = 0

#change the type of the anamoly if it was miss labled otherwise it will mark it as Human Reviewed correctly
#this function will be executed on the object indexed at 'plotingIndex' which is  a global variable
#input: correct boolean whether or not the existing label of the anamoly is correct
#output: ----
def reviewData ( isCorrectLabel ):
    global plotingIndex
    global rows # all json rows
    rows[plotingIndex]['value']['Reviewed'] = True
    if(not isCorrectLabel):
        rows[plotingIndex]['value']['anamolyType'] = rows[plotingIndex]['value']['anamolyType'] + 5
    elif(rows[plotingIndex]['value']['anamolyType']>4): #if it was reviewed as miss labeled before but now we want it to be correct
        rows[plotingIndex]['value']['anamolyType'] = rows[plotingIndex]['value']['anamolyType'] - 5

    r= Server.updateValueInServer(rows[plotingIndex]['value'])
    rows[plotingIndex]['value']['_rev'] = json.loads(r.text)['rev']
    FH.writeObjToFile('AllJsonFiles.txt' , rows)


#return true if we are interested in reviewing the current anamoly
#input reviewLabledData boolean true if we want to review data that is already labled
#       row the current anamoly as json obj
# output the boolean representing our interest in reviewing the current anamoly
def wantToReview(reviewLabledData , row):
    reviewedBefore = 'Reviewed' in row
    return (reviewLabledData and reviewedBefore) or (reviewUnlabledData and not reviewedBefore)

reviewLabledData = False
reviewUnlabledData = True
while plotingIndex<len(rows) and plotingIndex>=0:
    anamoly = Anamoly(rows[plotingIndex]['value'])
    if(ploter.isLookingFor(anamoly.anamolyType) and wantToReview(reviewLabledData,rows[plotingIndex]['value'] )):
        anamolyArray = [(anamoly,'hi')]
        ploter.plotMultipleAnamolies(anamolyArray , numberOfCols=1 , index=plotingIndex )
        if (ploter.reviewButtonPressed):
            reviewData(ploter.lastReview)
            ploter.reviewButtonPressed=False

    # elif(plotingIndex == len(rows)-1 or plotingIndex == 0):
    #     break
    if(ploter.endPloting):
        break
    plotingIndex += ploter.indexDirection
