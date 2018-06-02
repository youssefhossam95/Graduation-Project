# from FileHandler import  *
# from  preprecessing import *
# import numpy as np
# import Server
# import Ploter
# ploter = Ploter.Ploter()
#
# rows = loadObjFromFile("AllJsonFilesLatest.txt")
#
# anamoly = Anamoly(rows[419]['value'])
#
#
# def ApplySmoothingFilter2 ( array , fsize ) :
#     maxBefore = np.sum(np.abs(array))
#     print("max before" , maxBefore)
#     Filter = []
#     for i in range(0, fsize):
#         Filter.append(1 / fsize)
#
#     print(len(Filter))
#     newValues = np.convolve(array, Filter , 'same')
#     maxAfter = np.sum(np.abs(newValues)) ;
#     print("max after", maxAfter)
#     return newValues * maxBefore / maxAfter;
# def zeroCrossings1D(x):
#   XCrossings=0
#   for j in range(1,len(x)):
#     if (x[j]*x[j-1])<0:
#       XCrossings+=1
#   return XCrossings
# anamoly = sample(anamoly,50)
# print(anamoly.accelValues)
# anamoly = ApplySmoothingFilter(anamoly , 5);
# print(anamoly.accelValues)
# interestAnamoly,start,end = getAreaOfInterest(anamoly , 2 )
# print(start , end )
# print('interest speed' , getInterestSpeed(anamoly,2,50))
# print('number of peaks ' , getNumberOfPeaks(interestAnamoly));
# print("zero corssings" , zeroCrossings1D(interestAnamoly.accelValues))
# print('avgAbs: ' , avgAbsRatio(anamoly ,2))
#
# ploter.plotAnamoly(anamoly)

# #
# print('speed' , anamoly.speedValues)
# print('time' , anamoly.speedTime)

from scipy import  signal


f = signal.gaussian(5 , 1)
print(f/sum(f))
print (f)