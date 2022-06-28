import csv
import random
import math
import numpy as np

import nltk
from nltk.corpus import stopwords 
from nltk.tokenize import word_tokenize 
from tqdm import tqdm

nltk.download('punkt')                      
nltk.download('averaged_perceptron_tagger')
nltk.download('stopwords')


class Preprocessor:

    def __init__(self, data_dir, data_name):
        
        self.text = list()
        self.label = list()

        with open(data_dir/'{}.csv'.format(data_name), 'r', encoding='utf-8-sig') as f:
            rdr = csv.reader(f)

            for line in rdr:
                self.text.append(line[1])
                self.label.append(line[2])
        
        self.text = self.text[1:]
        self.label = self.label[1:]
    
    def preprocess(self):

        tokens = self.tokenize()
        tf_idf = self.cal_tf_idf(tokens)

        dataset = list()
        for v, l in zip(tf_idf, self.label):
            dataset.append([v, l])

        random.shuffle(dataset)
        train, test = self.split_data(dataset)

        x_train = list()
        y_train = list()
        for v, l in train:
            x_train.append(v)
            y_train.append(l)

        x_test = list()
        y_test = list()
        for v, l in test:
            x_test.append(v)
            y_test.append(l)
        
        return x_train, y_train, x_test, y_test

    def split_data(self, data):

        train = data[:int(len(data) * 0.8)]
        test = data[int(len(data) * 0.8):]

        return train, test

    def tokenize(self):
        
        tags = ['NN', 'NNS', 'NNP', 'NNPS', 'VB', 'VBD', 'VBG', 'VBN', 'VBP', 'VBZ']
        stop_words = set(stopwords.words('english'))
        tokenized_text = list()

        for txt in tqdm(self.text, desc="tokenizing..."):
            txt = txt.rstrip("\n")
            txt = txt.lower()

            pos_tagged = nltk.pos_tag(word_tokenize(txt))
            tokenized_text.append(
                [token[0] for token in pos_tagged \
                    if token[1] in tags \
                        if not token[0] in stop_words]
            )

        return tokenized_text
    
    def cal_tf_idf(self, data):
        """
        Function calculating tf-idf

        Parameter
            data: tokenized text list.

        Return
            tfidf vector of the data
        """
        tfidf = list()

        ### EDIT HERE ###
        # TF
        tf_data = []
        dic = {}
        for word_set in data:
            for word in word_set:
                if word in dic:
                    pass
                else:
                    dic[word] = 1

        for word_set in data:
            for word in word_set:
                dic[word] += 1
            tf_data.append(dic.copy())
            for key in dic.keys():
                dic[key] = 1

        # IDF
        N = 0
        for word_set in data:
            for word in word_set:
                if word in dic.keys():
                    dic[word] += 1
                else:
                    dic[word] = 1
            N += 1
        for key in dic.keys():
            dic[key] = math.log2(N / dic[key])
        idf_dic = dic.copy()

        # TF-IDF
        tfidf_dic = {}
        norm_tfidf_dic = {}

        for tf_dic in tf_data:
            tfidf_dic.clear()
            norm_tfidf_dic.clear()

            for word in tf_dic.keys():
                tfidf_dic[word] = tf_dic[word] * idf_dic[word]

            over = 0
            for key in tfidf_dic.keys():
                over += tfidf_dic[key] ** 2
            for word in tf_dic.keys():
                norm_tfidf_dic[word] = tfidf_dic[word]/math.sqrt(over)
            tfidf.append(list(norm_tfidf_dic.copy().values()))

        ### END ###

        return tfidf
        

class MLP:
    def __init__(self, input_size, hidden_size, output_size):
        ### EDIT HERE ###
        self.a = 0
        ### END ###


