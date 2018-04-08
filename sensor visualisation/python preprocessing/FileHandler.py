import pickle
import json

#write pyton object to file ... obj can be string , dictionary , array
#input:fileName name of the file to be written
#       obj the object to be written
def writeObjToFile (fileName , obj):
    fileObject = open(fileName, 'wb')
    pickle.dump(obj, fileObject)
    print('file was written named: ' +fileName)
    fileObject.close()

#write pyton object to file ... obj can be string , dictionary , array
#input:fileName name of the file containing the obj
#output:the object loaded from the file

def loadObjFromFile (fileName):
    fileObject = open(fileName, 'rb')
    # load the object from the file
    print('object loaded from file: ' + fileName)
    return  pickle.load(fileObject)


# writes some data to a file
#input: fileName: string of the name of the file
#       content: string containing the data to be written ;
#output: -----
def writeToFile( fileName , content):
    f = open(fileName, "w+", encoding='utf8')
    f.write(content)
    print("wrote to file" + fileName)
    f.close()



# gets the local stored json rows
#input: -----
#output: -----
def getStoredJsonRows():
    f = open("recievedJson.txt", "r+", encoding='utf8')
    x = f.read()
    print("file is read")
    obj=  json.loads(x)
    print("object is loaded")
    return obj["rows"]

def getDataForTest():
    f = open("testFile.txt", "r+", encoding='utf8')
    x = f.read()
    print("file is read")
    print ( x )
    obj = json.loads(x.replace("'" , "\""))
    print("object is loaded")
    return obj