#   *** Do not import any library except already imported libraries ***
import tqdm
import math
from tqdm import tqdm, trange
from HW4_util import AI_util
import numpy as np
from sklearn.naive_bayes import MultinomialNB

#   *** Do not import any library except already imported libraries ***

class Naive_Bayes(AI_util):
    def Train_and_Prediction(self, train_data=None, test_data=None, train_labels=None, test_labels=None, vocab=None):
        """
            *** You should implement this function with raw code ***
            *** When you code, you have to erase this comment ***

            (input) 'train_data/test_data' data type : ('list')
            (input) 'train_data/test_data' data format :  [(article id, tokens, tf_value)]
            (input) 'train_data/test_data' data example :  [(857, ['eviction', ... , 'night'], [2, 0, 0, ..., 0]), ..., (154, ['business', ..., 'rise'], [4, 0, 0, ..., 0]), ...]

            (input) 'train_labels/test_labels' data type : ('list')
            (input) 'train_labels/test_labels' data format :  [category]
            (input) 'train_labels/test_labels' data example :  ['business', 'business', 'tech', ..., 'entertainment']

            (input) 'vocab' data type : ('list')
            (input) 'vocab' data type : [tokens]
            (input) 'vocab' data type : ['info', '1970s', ..., 'travel']
            ========
            (output) 'return data1' type : ('list')
            (output) 'return data1' format : [(article id, Category probability distribution, prediction of model, true label)]
            (output) 'return data1' example : [(2160, [7.35, 35.11, 12.93, 26.61, 18], 'business', 'entertainment'), ...]

            (output) 'return data2' type : ('list')
            (output) 'return data2' format : [prediction of model]
            (output) 'return data2' example :  ['business', 'business', 'politics', ..., 'entertainment']
        """

        data1 = []
        data2 = []
        tf_set = []
        test_tf = []
        for tuple in train_data:
            tf_set.append(tuple[2])

        for tuple in test_data:
            test_tf.append(tuple[2])

        tf_set = np.array(tf_set)
        test_tf = np.array(test_tf)
        for i in range(len(train_labels)):
            if train_labels[i] == 'business':
                train_labels[i] = 0

            elif train_labels[i] == 'politics':
                train_labels[i] = 1

            elif train_labels[i] == 'sport':
                train_labels[i] = 2

            elif train_labels[i] == 'tech':
                train_labels[i] = 3

            elif train_labels[i] == 'entertainment':
                train_labels[i] = 4

        for i in range(len(test_labels)):
            if test_labels[i] == 'business':
                test_labels[i] = 0

            elif test_labels[i] == 'politics':
                test_labels[i] = 1

            elif test_labels[i] == 'sport':
                test_labels[i] = 2

            elif test_labels[i] == 'tech':
                test_labels[i] = 3

            elif test_labels[i] == 'entertainment':
                test_labels[i] = 4

        train_labels = np.array(train_labels)
        test_labels = np.array(test_labels)
        clf = MultinomialNB(alpha=1.0)
        clf.fit(tf_set, train_labels)
        predict = clf.predict(test_tf)
        print(predict)
        hit = 0
        for i in range(len(test_labels)):
            if predict[i] == test_labels[i]:
                hit += 1
        print(hit / len(test_labels))
        return data1, data2


if __name__ == "__main__":
    #   *** Do not modify the code below ***
    naive = Naive_Bayes()
    news_data = naive.Load_Pickle_Data(input_pickle_file_path="HW4_BBC_Data.pickle")
    vocab = naive.Load_Pickle_Data(input_pickle_file_path="HW4_Vocab.pickle")
    train_data, test_data, train_labels, test_labels = naive.Split_Train_Test(news_data=news_data)

    result, predictions = naive.Train_and_Prediction(train_data=train_data, test_data=test_data,
                                                           train_labels=train_labels, test_labels=test_labels,
                                                           vocab=vocab)
    #   *** Do not modify the code above ***
