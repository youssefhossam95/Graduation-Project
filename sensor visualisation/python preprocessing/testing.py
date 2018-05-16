from FileHandler import  *
from  preprecessing import *
import numpy as np
import Server
import Ploter
ploter = Ploter.Ploter()

rows = loadObjFromFile("AllJsonFilesLatest.txt")

anamoly = Anamoly(rows[20]['value'])


def ApplySmoothingFilter2 ( array , fsize ) :
    maxBefore = np.sum(np.abs(array))
    print("max before" , maxBefore)
    Filter = []
    for i in range(0, fsize):
        Filter.append(1 / fsize)

    print(len(Filter))
    newValues = np.convolve(array, Filter , 'same')
    maxAfter = np.sum(np.abs(newValues)) ;
    print("max after", maxAfter)
    return newValues * maxBefore / maxAfter;
def zeroCrossings1D(x):
  XCrossings=0
  for j in range(1,len(x)):
    if (x[j]*x[j-1])<0:
      XCrossings+=1
  return XCrossings

print(anamoly.accelValues)
anamoly = sample(anamoly,50)
print(anamoly.accelValues)
#print(anamoly.accelTime)
interestAnamoly,start,end = getAreaOfInterest(anamoly , 3 )
print(start , end )
print('interest speed' , getInterestSpeed(anamoly,3,50))
print(getNumberOfPeaks(interestAnamoly));

ploter.plotAnamoly(anamoly)
print(avgAbsRatio(anamoly ,3))
print(zeroCrossings1D(interestAnamoly.accelValues))
smoothedAnamoly = ApplySmoothingFilter(anamoly , 3)

#
print('speed' , anamoly.speedValues)
print('time' , anamoly.speedTime)


