import numpy as np


def getXY(data):
    x_data = []
    y_data = []
    for index in range(len(data)):
        x_data.append(data[index][0])
        y_data.append(data[index][1])
    return x_data, y_data


def f1_matrix(x_data):
    # f1 = [1, cos(pi*x)]
    fmatrix = []
    for x in x_data:
        fmatrix.append([1, np.cos(np.pi*x)])
    return fmatrix


def f2_matrix(x_data):
    # f2 = [1, x, x**2]
    fmatrix = []
    for x in x_data:
        fmatrix.append([1, x, x**2])
    return fmatrix


def calcCoef(hMatrix, y_data):
    matrix = np.array(hMatrix)
    y = np.array(y_data)
    hTh = np.dot(matrix.transpose(), matrix)
    inverse_hTh = np.linalg.inv(hTh)
    hTy = np.dot(matrix.transpose(), y)
    w = np.dot(inverse_hTh, hTy)
    return w


if __name__ == '__main__':
    sample_data = [[-1, 1], [0, 1], [1, 1], [1, 0]]

    xData, yData = getXY(sample_data)

    w1 = calcCoef(f1_matrix(xData), yData)
    w2 = calcCoef(f2_matrix(xData), yData)

    print('f1')
    for i in range(len(w1)):
        print('w{} = {:.10f}'.format(i, w1[i]))

    print('\nf2')
    for i in range(len(w2)):
        print('w{} = {:.10f}'.format(i, w2[i]))