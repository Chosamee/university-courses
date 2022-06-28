import numpy as np


def checkDistance(data, target):
    x1_diff = data[1] - target[0]
    x2_diff = data[2] - target[1]
    dis_square = x1_diff **2 + x2_diff**2
    distance = np.sqrt(dis_square)
    return distance


def calc_KNN(data_set, target, k):
    dis_set = []
    label0, label1 = 0, 0
    for data in data_set:
        data = np.array(data, dtype=np.float64)
        dis_set.append(checkDistance(data, target))
    dis_set_idx = np.array(dis_set).argsort()

    for i in range(k):
        label = data_set[dis_set_idx[i]][0]
        if label == '0':
            label0 += 1
        else:
            label1 += 1

    if label0 > label1:
        return '0'
    elif label0 < label1:
        return '1'
    else:
        return 'cannot found'


if __name__ == '__main__':
    data_set = np.genfromtxt("KNN_data.csv", delimiter=',', dtype=None, encoding='UTF-8')
    data_set = data_set[1:]
    i = 0

    target_set = [[1.4, 0.2], [1.4, 0.5], [0.9, 4.0], [-0.1, 3.0], [2.5, 0.1]]
    k_set = [3, 5]

    for k in k_set:
        print('Case {} : k = {}'.format(i, k))
        for target in target_set:
            print(target, ': Label = ', calc_KNN(data_set, target, k))
        print('\n')
        i += 1