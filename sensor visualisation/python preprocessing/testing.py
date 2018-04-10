
import Server
import json
import FileHandler as FH
from Ploter import Ploter
ploter = Ploter()
rows = FH.loadObjFromFile('AllJsonFiles.txt')
#'3-f4ac1d40aed629e68959a4f1588a4445'
cou = 0
for row in rows:
    if(row['value']['anamolyType']==1):
        # row['value']['anamolyType'] = 4
        # row['value']['_rev'] ='10-4765663cbbac5e612bd0a6ab26ef6c81'
        # # r = Server.updateValueInServer(row['value'])
        # row['value']['_rev'] = json.loads(r.text)['rev']
        print(cou)
    cou+=1

# FH.writeObjToFile('AllJsonFiles.txt',rows)

