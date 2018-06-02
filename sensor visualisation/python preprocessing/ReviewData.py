import FileHandler as FH
from preprecessing import *
import Ploter
import Server
import json

ploter= Ploter.Ploter()
ploter.reviewMode = False
anamolyArray=[]
fileName= 'AllJsonFilesLatest.txt'
rows = FH.loadObjFromFile(fileName)
plotingIndex = 2000
#change the type of the anamoly if it was miss labled otherwise it will mark it as Human Reviewed correctly
#this function will be executed on the object indexed at 'plotingIndex' which is  a global variable
#input: correct boolean whether or not the existing label of the anamoly is correct
#output: boolean represents the sucess of the lableing operation
def reviewData ( isCorrectLabel , fileName ):
    global plotingIndex
    global rows # all json rows
    rows[plotingIndex]['value']['Reviewed'] = True
    if(not isCorrectLabel):
        rows[plotingIndex]['value']['anamolyType'] = rows[plotingIndex]['value']['anamolyType'] + 5
    elif(rows[plotingIndex]['value']['anamolyType']>4): #if it was reviewed as miss labeled before but now we want it to be correct
        rows[plotingIndex]['value']['anamolyType'] = rows[plotingIndex]['value']['anamolyType'] - 5

    r= Server.updateValueInServer(rows[plotingIndex]['value'])
    if(r==False):
        print('error in the server response')
        return False
    rows[plotingIndex]['value']['_rev'] = json.loads(r.text)['rev']
    FH.writeObjToFile(fileName , rows)
    return True

#return true if we are interested in reviewing the current anamoly
#input reviewLabledData boolean true if we want to review data that is already labled
#       row the current anamoly as json obj
# output the boolean representing our interest in reviewing the current anamoly
def wantToReview(reviewLabledData , row):
    reviewedBefore = 'Reviewed' in row
    return (reviewLabledData and reviewedBefore) or (reviewUnlabledData and not reviewedBefore)

reviewLabledData = True
reviewUnlabledData = True
while plotingIndex<len(rows) and plotingIndex>=0:
    anamoly = Anamoly(rows[plotingIndex]['value'])
    anamolyArray = []
    # anamolyArray.append((anamoly , 'Oiginal anomaly')) ;
    if(ploter.isLookingFor(anamoly.anamolyType) and wantToReview(reviewLabledData,rows[plotingIndex]['value'] )):
        print(anamoly.accelTime[2])
        convertToRelativeTime(anamoly)
        sampled = preprossing(anamoly , shifting=True , smoothing=True)
        area , _ , _ = getAreaOfInterest(sampled, 2)
        number = getNumberOfPeaks(area)
        avg = round(  avgAbsRatio(sampled,2) , 2)
        zero = zeroCrossings1D(area.accelValues)
        interestSpeed = round(getInterestSpeed(sampled , 2 , 50) , 2)
        avgSpeed = round( np.mean(anamoly.speedValues)  ,2 )
        anamolyArray.append(( sampled , #'Anomaly containing road bump \n' +
                              'number of Peaks :' + str  (number) +
                              "\nnumber of zero corssings: " + str(zero) +
                              "\nAverage Absolute Ratio: " + str(avg) +
                              "\nSpeed at area of interest: " + str(interestSpeed) +
                              "\nAverage Speed: "+ str(avgSpeed)        ))


        # filter3 = ApplySmoothingFilter(sampled , 3)
        # anamolyArray.append((filter3 , "Using filter of size 3"))
        # filter5 = ApplySmoothingFilter(sampled , 5)
        # anamolyArray.append((filter5 ,"Using filter of size 5"))
        # filter10 = ApplySmoothingFilter(sampled, 15)
        # anamolyArray.append((filter10 , 'Using filter of size 15'))

        # anamolyArray.append((interest , "Area of interest"))
        ################filters comparison################################
        # gauss = ApplyGaussianFilter( sampled , 10 )
        # anamolyArray.append((gauss , 'After applying gaussian filter'))
        # ourGauss = ApplyGaussianFilter2(sampled  , 5)
        # anamolyArray.append((ourGauss , 'gaussian with modification'))
        # avr = ApplyAveragingFilter(sampled , 5)
        # anamolyArray.append((avr , 'After applying arveraging filter'))
        # our1 = ApplySmoothingFilter(sampled , 5)
        # anamolyArray.append((our1 , "After applying the customized filter"))

        #####################end of filter comparison ###############################

        ploter.plotMultipleAnamolies(anamolyArray , numberOfCols=1 , index=plotingIndex )
        if (ploter.reviewButtonPressed):
            if(not reviewData(ploter.lastReview , fileName)):
                break;
            ploter.reviewButtonPressed=False

    # elif(plotingIndex == len(rows)-1 or plotingIndex == 0):
    #     break
    if(ploter.endPloting):
        break
    plotingIndex += ploter.indexDirection



