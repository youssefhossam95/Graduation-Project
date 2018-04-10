import FileHandler as FH
import numpy as np


def loadData():
    rows = FH.loadObjFromFile('AllJsonFiles.txt')
    m=len(rows)
    T_x = len(rows[0]['value']['accelValues'])
    X = np.zeros((m,T_x))
    Y = np.zeros((m,1))
    return X ,Y