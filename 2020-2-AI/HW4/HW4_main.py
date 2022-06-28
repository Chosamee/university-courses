#   *** Do not import any library except already imported libraries ***
import tqdm
import math
from tqdm import tqdm,trange
from HW4_util import AI_util
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

        label_list = ['business', 'politics', 'sport', 'tech', 'entertainment']

        # 개수 셌음
        vocab_cnt = [[1 for _ in range(len(vocab))] for _ in range(5)]

        for i in range(len(vocab)):
            for j in range(len(train_data)):
                tf = train_data[j][2]

                if train_labels[j] == 'business':
                    vocab_cnt[0][i] += tf[i]

                elif train_labels[j] == 'politics':
                    vocab_cnt[1][i] += tf[i]

                elif train_labels[j] == 'sport':
                    vocab_cnt[2][i] += tf[i]

                elif train_labels[j] == 'tech':
                    vocab_cnt[3][i] += tf[i]

                elif train_labels[j] == 'entertainment':
                    vocab_cnt[4][i] += tf[i]

        # 확률로 바꾸자
        for i in range(5):
            total_cnt = 0
            for cnt in vocab_cnt[i]:
                total_cnt += cnt
            for j in range(len(vocab)):
                vocab_cnt[i][j] /= total_cnt
        # 카테고리 갯수셈
        cat_num = [0, 0, 0, 0, 0]
        for cat in train_labels:
            if cat == 'business':
                cat_num[0] += 1
            elif cat == 'politics':
                cat_num[1] += 1
            elif cat == 'sport':
                cat_num[2] += 1
            elif cat == 'tech':
                cat_num[3] += 1
            elif cat == 'entertainment':
                cat_num[4] += 1
        # 확률로 바꾸자
        total_cnt = 0
        for i in range(5):
            total_cnt += cat_num[i]
        for i in range(5):
            cat_num[i] /= total_cnt

        # Prediction?
        for i in range(len(test_data)):
            tuple = test_data[i]
            tf = tuple[2]
            imsival = 0.0
            prob_set = [0, 0, 0, 0, 0]

            # 각 label에 대해 확률
            for j in range(5):
                logprob = 0
                for k in range(len(vocab)):
                    logprob += ((tf[k]) * math.log2(vocab_cnt[j][k]))
                prob_set[j] = cat_num[j] * logprob
                imsival += prob_set[j]
            imsi = prob_set.copy()
            for k in range(5):
                imsi[k] = round(imsi[k] / imsival * 100, 2)

            probmax = max(prob_set)
            predict = prob_set.index(probmax)
            article = tuple[0]
            true_label = test_labels[i]
            data1.append((article, imsi, label_list[predict], true_label))
            data2.append(label_list[predict])

        return data1, data2



if __name__ == "__main__":
    #   *** Do not modify the code below ***
    naive_bayes = Naive_Bayes()
    news_data = naive_bayes.Load_Pickle_Data(input_pickle_file_path="HW4_BBC_Data.pickle")
    vocab = naive_bayes.Load_Pickle_Data(input_pickle_file_path="HW4_Vocab.pickle")
    train_data, test_data, train_labels, test_labels = naive_bayes.Split_Train_Test(news_data=news_data)

    result, predictions = naive_bayes.Train_and_Prediction(train_data=train_data, test_data=test_data, train_labels=train_labels, test_labels=test_labels, vocab=vocab)
    naive_bayes.Save_Result(result=result, predictions=predictions, test_label=test_labels, std_name="StudentName", std_id ="StudentID")
    #   *** Do not modify the code above ***
