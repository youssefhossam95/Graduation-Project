import preprecessing as PP
import matplotlib.pyplot as plt
import math
import sys

class Ploter:
    indexDirection = 1
    endPloting = False
    lookingFor = 'a'
    reviewMode = False
    reviewButtonPressed = False
    lastReview = True
    def press(self , event):
        print('press', event.key)
        sys.stdout.flush()
        if event.key == '1':
            self.indexDirection = -1  # change the direction to be negative i.e. move backwards
            plt.close()
        elif event.key == '2':
            self.indexDirection = 1  # change the direction to be positive i.e. move forward
            plt.close()
        elif event.key == 'q':
            self.endPloting = True
        elif (self.reviewMode):  # if Ploter was called in the review mode
            if (event.key == 't'):
                self.lastReview = True
                self.reviewButtonPressed = True
                plt.close()
            elif (event.key == 'f'):
                self.lastReview = False
                self.reviewButtonPressed = True
                plt.close()
            else:
                self.lookingFor = event.key
        else:
            self.lookingFor = event.key

    #this should be called everytime you create a new plot
    # 1 to move to the previous anamoly
    # 2 to move to the next anamoly
    # t review this anamoly as correctly labeled
    # f review this anamoly as miss labeled
    # q to exit the
    def setKeyPressHandler(self):
        fig, ax = plt.subplots()
        fig.canvas.mpl_connect('key_press_event', self.press)

    # converts anamoly type which is an INT into a string representing this INT
    #input: INT Type
    #output: string Type name
    def getTypeName(self , type):
        names =["MATAB" , 'HOFRA' ,'GALAT', 'TAKSER' ,'UNKNWON']
        miss = ""
        if(type >4 ):
            miss ='miss Labled '
            type -= 5
        return miss+names[type]


    #plot multiple anamolies in vertical sub plots  represented by anamolyArray
    #input: Anamoly Array // array of tubles of anamolies to be printed and thier titles
    #output: -----
    def plotMultipleAnamolies(self, anamolyArray , numberOfCols =2 , index=""  ):
        if(len(anamolyArray)==0):
            print('the array is empty')
            return
        self.setKeyPressHandler() # to allow control through keyboard
        numberOfPlots = len(anamolyArray)
        numberOfRows = math.ceil(numberOfPlots/numberOfCols)
        plotNumber = 1
        for i in range (0 , numberOfPlots):
            subPlotNumber= numberOfRows*100 + numberOfCols * 10 + plotNumber
            plotNumber =  plotNumber + numberOfCols
            plotNumberMod = plotNumber%(numberOfCols*numberOfRows+1)
            if (plotNumber != plotNumberMod ):
                plotNumber = plotNumberMod + 2
            else :
                plotNumber = plotNumberMod

            anamoly , title = anamolyArray[i]
            PP.convertToRelativeTime(anamoly) #convert time to be in seconds
            plt.subplot(subPlotNumber)
            plt.plot(anamoly.accelTime , anamoly.accelValues)
            plt.title(title)
            plt.grid()

        figManager = plt.get_current_fig_manager()
        figManager.window.state('zoomed')
        plt.suptitle(self.getTypeName(anamoly.anamolyType)+" "+ str(index) + " " + anamoly.id)
        plt.show()


    #plot a single anamoly
    #input: Anamoly
    #output: -----
    def plotAnamoly (self, anamoly):
        plt.plot(anamoly.accelTime, anamoly.accelValues)
        plt.title(self.getTypeName(anamoly.anamolyType))
        plt.show()

    # check if the type of the anamoly is the type that the user wants
    # input: lookingFor: char representing what the user is looking for
    #       anamolyType: int the type of the current anamoly
    # output: boolean true if type of anamoly is what we are looking OR it is not in the list for else false
    def isLookingFor(self , type):
        #   names = ["MATAB", 'HOFRA', 'GALAT', 'TAKSER', 'UNKNWON','MISSLABLED']
        #   type will be as the index of the names array
        types = ['m', 'h', 'g', 't', 'u']
        if (self.lookingFor in types):
            return types.index(self.lookingFor) == type
        elif(self.lookingFor == 'i'): #miss labled anamoly
            return type > 4
        else:
            return True


    def movePlotingIndex(self, plotingIndex , maxIndex):
        print('indexDirectoin' , self.indexDirection)
        plotingIndex += self.indexDirection
        if(plotingIndex > maxIndex or plotingIndex < 0):  # if out of the bounds of the rows array
            plotingIndex-=self.indexDirection  # reverse the last step so nothing will happen in the algorithm
        return  plotingIndex




