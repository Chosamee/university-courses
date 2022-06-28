import numpy as np
import cv2
import random

class Matrix:
    def __init__(self):
        rad = np.deg2rad(5)
        self.dic = {'a': np.array([[1, 0, -5], [0, 1, 0], [0, 0, 1]]),
                    'd': np.array([[1, 0, 5], [0, 1, 0], [0, 0, 1]]),
                    's': np.array([[1, 0, 0], [0, 1, 5], [0, 0, 1]]),
                    'w': np.array([[1, 0, 0], [0, 1, -5], [0, 0, 1]]),
                    'R': np.array([[np.cos(rad), -np.sin(rad), 0], [np.sin(rad), np.cos(rad), 0], [0, 0, 1]]),
                    'r': np.array([[np.cos(-rad), -np.sin(-rad), 0], [np.sin(-rad), np.cos(-rad), 0], [0, 0, 1]]),
                    'f': np.array([[-1, 0, 0], [0, 1, 0], [0, 0, 1]]),
                    'F': np.array([[1, 0, 0], [0, -1, 0], [0, 0, 1]]),
                    'x': np.array([[0.95, 0, 0], [0, 1, 0], [0, 0, 1]]),
                    'X': np.array([[1.05, 0, 0], [0, 1, 0], [0, 0, 1]]),
                    'y': np.array([[1, 0, 0], [0, 0.95, 0], [0, 0, 1]]),
                    'Y': np.array([[1, 0, 0], [0, 1.05, 0], [0, 0, 1]])}




