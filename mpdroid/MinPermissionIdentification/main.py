import datetime
import re
import time

import pymysql as sql
import os
import threading

import TopicMat as topic
import PermissionMat as permission
import getSimilarity as similar
import get_recommend as recommend

import matplot

import ReData

import common as config

"""
Output recommended files for sf and minps
"""


def getSf(TOPIC_NUM, isSF, limit, size):
    """
    sf data
    :return:
    """
    end = ''

    if (isSF):
        end = str(limit) + 'Filtering sf Output'
    else:
        end = str(limit) + 'Filtering minps Output'

    sourcePath = 'Data_Origin/' + size + 'Target/'


    appIdPath = sourcePath + str(TOPIC_NUM) + 'topic_data_output/' + 'Benign' + size + 'App.txt'

    outPrint = 'Data_Output/' + size + 'Target/' + str(TOPIC_NUM) + 'topic_data_output' + end + '/'

    config.mkdir(outPrint)
    getTopic(sourcePath + str(TOPIC_NUM) + 'topic_data_output/', outPrint, TOPIC_NUM)
    getPermission(sourcePath + str(TOPIC_NUM) + 'topic_data_output/', outPrint, isSF, size, appIdPath)
    getSimister(outPrint, TOPIC_NUM)
    getRecommend(outPrint)


def getTopic(resourcePath, targetPath, TOPIC_NUM):
    """
    Get the topic matrix
    :return:
    """
    topic.main(resourcePath + 'Benign topic four-fifths.txt', targetPath + 'Benign_topic.mat', TOPIC_NUM)
    topic.main(resourcePath + 'Malicious topic four-fifths.txt', targetPath + 'Malicious_topic.mat', TOPIC_NUM)
    topic.main(resourcePath + 'Benign topic one-fifths.txt', targetPath + 'Benign_target_topic.mat', TOPIC_NUM)
    topic.main(resourcePath + 'Malicious topic one-fifths.txt', targetPath + 'Malicious_target_topic.mat', TOPIC_NUM)



def getPermission(resourcePath, targetPath, isSF, size, appidPath):
    """
    Get the permission matrix, which is obtained from the database？
    :return:
    """

    if (isSF):
        # sf
        permission.main(resourcePath + 'Benign permission four-fifths.txt', targetPath + 'Benign-permission-matrix.mat')

    else:

        Benign_training_App = open(appidPath, 'r').readlines()

        db = getTrainAndTargetPermissionFromDb(Benign_training_App)
        permission.mainInList(db, targetPath + 'Benign-permission-matrix.mat', 1)
    permission.main(resourcePath + 'Malicious permission four-fifths.txt', targetPath + 'Malicious-permission-matrix.mat')
    permission.main(resourcePath + 'Benign permission one-fifths.txt', targetPath + 'Benign-target-permission-matrix.mat')
    permission.main(resourcePath + 'Malicious permission one-fifths.txt', targetPath + 'Malicious-target-permission-matrix.mat')



def getSimister(outPath, TOPIC_NUM):
    """
    Get similarity file
    :param outPath:
    :param TOPIC_NUM: topic number
    :return:
    """

    similar.main(outPath + 'Benign_topic.mat', outPath + 'Benign_target_topic.mat', outPath + 'Benign-benign_Similarity.txt', TOPIC_NUM)
    similar.main(outPath + 'Malicious_topic.mat', outPath + 'Benign_target_topic.mat', outPath + 'Malicious-benign_Similarity.txt', TOPIC_NUM)

    similar.main(outPath + 'Benign_topic.mat', outPath + 'Malicious_target_topic.mat', outPath + 'Benign-malicious_Similarity.txt', TOPIC_NUM)
    similar.main(outPath + 'Malicious_topic.mat', outPath + 'Malicious_target_topic.mat', outPath + 'Malicious-malicious_Similarity.txt', TOPIC_NUM)



def getRecommend(outPath):
    """
    Get recommend file
    :return:
    """

    recommend.main(outPath + 'Benign-permission-matrix.mat', outPath + 'Benign-benign_Similarity.txt', outPath + 'Benign-benign_Recommend.txt', 0.6)
    recommend.main(outPath + 'Malicious-permission-matrix.mat', outPath + 'Malicious-benign_Similarity.txt', outPath + 'Benign-malicious_Recommend.txt', 0.4)
    recommend.main(outPath + 'Benign-permission-matrix.mat', outPath + 'Benign-malicious_Similarity.txt', outPath + 'Malicious-benign_Recommend.txt', 0.6)
    recommend.main(outPath + 'Malicious-permission-matrix.mat', outPath + 'Malicious-malicious_Similarity.txt', outPath + 'Malicious-malicious_Recommend.txt', 0.4)


def getTrainAndTargetPermissionFromDb(appids):
    """
    To get permission data from the database, it is important to pay attention to what is the updated data.
    :param appid:
    :return:
    """

    appid = []
    for item in appids:
        appid.append(int(item))

    replace = str(appid).replace('[', '').replace(']', '')
    selectSql = "SELECT appid, permission from permissions where appid in ({0})".format(replace)
    getPermission = config.sqlExecute(selectSql)
    return getPermission


def sfMethod(TOPIC_NUM, limit, target):
    """
    Get sf
    :return:
    """
    global end
    getSf(TOPIC_NUM, 1, limit, target)
    end = str(limit) + 'Filtering sf Output'
    print(str(TOPIC_NUM) + 'Topics' + end + ' Complete')


def minpMethod(TOPIC_NUM, limit, target):
    """
    Get minp
    :return:
    """
    global end
    # batMethod()
    end = str(limit) + 'Filtering minps Output'
    getSf(TOPIC_NUM, False, limit, target)
    print(str(TOPIC_NUM) + 'Topics' + end + ' Complete')


def batMethod():
    """
    Restore database
    :return:
    """
    bat = "mysql -uroot -p analyzedata < "  # + abspath
    os.system(bat)


###
# threads = []
# t1 = threading.Thread(target=sfMethod)
# threads.append(t1)
# t2 = threading.Thread(target=minpMethod)
# threads.append(t2)


def create_result(limit, target, topic_NUM):
    """
    Four steps from creating a file to getting a result
    :param limit:
    :param target:
    :param topic_NUM:
    :return:
    """
    localtime = time.asctime(time.localtime(time.time()))
    print(localtime, target, limit, topic_NUM, 'Output start')
    print('=====================================================================================')
    topicPath = 'Data_Origin/%sTarget/%stopic_data_output/topic_test0201_suspend.txt' % (target, topic_NUM)
    if not (os.path.exists(topicPath)):
        # Create a topic file
        print(topicPath, "Need to create a file")
        cmd = "java -Dfile.encoding=utf-8 -jar Over_declared_Per_Identify.jar %s ./data ./Data_Origin/%sTarget" % (topic_NUM, target)
        print(cmd)
        os.system(cmd)
    sqlPath = ReData.main(topic_NUM, limit, target)
    sfMethod(topic_NUM, limit, target)
    minpMethod(topic_NUM, limit, target)
    # The calculation is written to the database
    cmd = "java -Dfile.encoding=utf-8 -jar RiskCalculation.jar %s %s %s %s %s %s" % (topic_NUM, target, limit, './Data_Output', './permission.json', config.sql_password)
    print(cmd)
    os.system(cmd)
    print('=====================================================================================')
    print(time.asctime(time.localtime(time.time())), target, limit, topic_NUM, 'Output complete\r\n')


def getData():
    # topic_num_list = [60, 65, 70, 75, 80, 85, 90, 95, 100]
    # # topic_num_list = [100 ]
    # target_list = ['20-80']
    # limit_list = [0.1]
    # for target in target_list:
    #    for topic_NUM in topic_num_list:
    #        for limit in limit_list:
    #            create_result(limit, target, topic_NUM)
    # matplot.getResult(topic_num_list, 'Topic Number', topic_num_list, target_list, limit_list,
    #                  'C:', 'topic_num', topic_num_list)

    # topic_num_list = [100]
    # target_list = ['20-80']
    # limit_list = [0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6]
    # for target in target_list:
    #     for topic_NUM in topic_num_list:
    #         for limit in limit_list:
    #             create_result(limit, target, topic_NUM)
    # matplot.getResult(limit_list, 'Support Degree Threshold', topic_num_list, target_list, limit_list,
    #                   'C:/Users/wzp12/Desktop/data', 'threshold', limit_list)

    # topic_num_list = [100]
    # target_list = ['10-90', '15-85', '20-80', '25-75']  # ,'30-60','35-65', '40-60'
    # limit_list = [0.1]
    # for target in target_list:
    #     for topic_NUM in topic_num_list:
    #        for limit in limit_list:
    #             create_result(limit, target, topic_NUM)
    # ta = [re.sub(r"-.{2}", '', x) for x in target_list]
    # matplot.getResult(ta, 'Test Set Ratio', topic_num_list, target_list, limit_list,
    #                  'D:', 'test_proportion', target_list)


    topic_num_list = [100]
    target_list = ['20-80']
    limit_list = [0.1]
    for target in target_list:
         for topic_NUM in topic_num_list:
             for limit in limit_list:
                 create_result(limit, target, topic_NUM)


if __name__ == '__main__':
    getData()

    print('Output complete')
