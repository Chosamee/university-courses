# -*- coding: utf-8 -*-

import argparse
import logging
from pathlib import Path

import csv
import numpy as np

# from 이재민_2017313260_model import MLP
from 이재민_2017313260_model import Preprocessor


def main(args):
    ### EDIT HERE ###
    id = args.test_article_id - 1
    preprocessor = Preprocessor(args.data_dir, 'BBC_News_Data')
    x_train, y_train, x_test, y_test = preprocessor.preprocess()
    label_list = ['business', 'politics', 'sport', 'tech', 'entertainment']

    token_num = len(x_train[0])
    article_num = len(x_train)
    label_num = 5


    # initialize
    h = 15000  # hidden layer
    x = np.array(x_train)
    w1 = 0.01 * np.random.randn(token_num, h)
    b1 = np.zeros((1, h))
    w2 = 0.01 * np.random.randn(h, label_num)
    b2 = np.zeros((1, label_num))
    learning_rate = 0.001
    label_real = []

    for i in range(article_num):
        label = y_train[i]
        label_real.append(label_list.index(label))

    for _ in range(20000):
        hidden = np.maximum(0, np.dot(x, w1) + b1)  # ReLU
        label_val = np.dot(hidden, w2) + b2  # 행렬 곱 결과

        # softmax
        label_exp = np.exp(label_val)
        probs = label_exp / np.sum(label_exp, axis=1, keepdims=True)

        # 각 label 확률에서 정답부분 1 뺌 [0.xx, 0.xx, 0.xx, 0.xx, 0.xx] - [0, 0, 1, 0, 0]
        error = np.array(probs)
        for j in range(article_num):
            error[j][label_real[j]] -= 1

        # back prpagation
        hidden_inv = np.dot(error, w2.T)
        hidden_inv[hidden <= 0] = 0  #ReLU

        dw2 = np.dot(hidden.T, error)
        db2 = np.sum(error, axis=0, keepdims=True)

        dw1 = np.dot(x.T, hidden_inv)
        db1 = np.sum(hidden_inv, axis=0, keepdims=True)

        w2 -= learning_rate * dw2
        w1 -= learning_rate * dw1
        b2 -= learning_rate * db2
        b1 -= learning_rate * db1

        if (_%5 == 0):
            print(_)

    # accuracy, precision, recall, f1 calculate
    n = 0
    metric = np.zeros((5, 5))

    for i in range(len(x_test)):
        x = np.array(x_test[i])
        y = label_list.index(y_test[i])

        predict = np.argmax(np.dot(np.dot(x, w1) + b1, w2) + b2)
        n += 1
        metric[predict][y] += 1

    TP, precision, recall = 0, 0, 0
    for i in range(5):
        TP += metric[i][i]

    for i in range(5):
        val = 0
        for j in range(5):
            val += metric[i][j]
        if val != 0:
            precision += metric[i][i] / val
    precision = precision / 5

    for i in range(5):
        val = 0
        for j in range(5):
            val += metric[j][i]
        if val != 0:
            recall += metric[i][i] / val
    recall = recall / 5

    f1 = 2 * (precision * recall) / (precision + recall)

    # file write
    with open(args.data_dir / '{}_{}.txt'.format('이재민', 15000), 'w', encoding='utf-8') as f:

        f.write("Accuracy: {}\n".format(round(TP/n*100, 2)))
        f.write("Macro averaging f1-score: {}\n\n".format(round(f1*100, 2)))
        f.write("ArticleID: {}\n".format(id))
        f.write("Predicted label: {}\n".format(label_list[int(np.argmax(np.dot(np.dot(x_test[id], w1) + b1, w2) + b2))]))
        f.write("Real label: {}\n".format(y_test[id]))

    return
    ### END ###


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--test_article_id',type=int, default=1, dest="test_article_id")
    parser.add_argument('--data_dir', type=Path, default='./')
    args = parser.parse_args()
    main(args)
