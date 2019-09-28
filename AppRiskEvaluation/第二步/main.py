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
输出sf和minp的推荐文件
"""


def getSf(TOPIC_NUM, isSF, limit, size):
    """
    sf数据
    :return:
    """
    end = ''

    if (isSF):
        end = str(limit) + '过滤sf输出'
    else:
        end = str(limit) + '过滤minps输出'

    # 话题和权限文件路径
    sourcePath = '数据源/' + size + '目标/'

    # appId文件路径
    appIdPath = sourcePath + str(TOPIC_NUM) + '话题数据输出/' + '良性' + size + 'App.txt'

    # 输出路径
    outPrint = '数据输出/' + size + '目标/' + str(TOPIC_NUM) + '话题数据输出' + end + '/'

    config.mkdir(outPrint)
    getTopic(sourcePath + str(TOPIC_NUM) + '话题数据输出/', outPrint, TOPIC_NUM)
    getPermission(sourcePath + str(TOPIC_NUM) + '话题数据输出/', outPrint, isSF, size, appIdPath)
    getSimister(outPrint, TOPIC_NUM)
    getRecommend(outPrint)


def getTopic(resourcePath, targetPath, TOPIC_NUM):
    """
    得到话题矩阵
    :return:
    """
    # print("获取话题矩阵")
    topic.main(resourcePath + '良性话题五分之四.txt', targetPath + '良性话题.mat', TOPIC_NUM)
    topic.main(resourcePath + '恶性话题五分之四.txt', targetPath + '恶性话题.mat', TOPIC_NUM)
    topic.main(resourcePath + '良性话题五分之一.txt', targetPath + '良性目标话题.mat', TOPIC_NUM)
    topic.main(resourcePath + '恶性话题五分之一.txt', targetPath + '恶性目标话题.mat', TOPIC_NUM)
    # print('话题矩阵获取完毕')


def getPermission(resourcePath, targetPath, isSF, size, appidPath):
    """
    获取权限矩阵，权限从数据库中获取？
    :return:
    """
    # print('获取权限矩阵')

    if (isSF):
        # sf
        permission.main(resourcePath + '良性权限五分之四.txt', targetPath + '良性权限矩阵.mat')

    else:
        # 不是sf的实验
        # if (size == '五分之一'):
        #     良性训练App = open('数据源/合并过滤权限支持度/五分之四良性App.txt', 'r').readlines()
        # else:
        #     良性训练App = open('数据源/合并过滤权限支持度/十分之九良性App.txt', 'r').readlines()
        良性训练App = open(appidPath, 'r').readlines()
        # 恢复数据库
        db = getTrainAndTargetPermissionFromDb(良性训练App)
        permission.mainInList(db, targetPath + '良性权限矩阵.mat', 1)
    permission.main(resourcePath + '恶性权限五分之四.txt', targetPath + '恶性权限矩阵.mat')
    permission.main(resourcePath + '良性权限五分之一.txt', targetPath + '良性目标权限矩阵.mat')
    permission.main(resourcePath + '恶性权限五分之一.txt', targetPath + '恶性目标权限矩阵.mat')
    # print('获取权限矩阵完毕')


def getSimister(outPath, TOPIC_NUM):
    """
    获取相似度文件
    :param outPath:
    :param TOPIC_NUM: 话题数
    :return:
    """
    # print('获取相似度文件')
    similar.main(outPath + '良性话题.mat', outPath + '良性目标话题.mat', outPath + '良性良性相似度.txt', TOPIC_NUM)
    similar.main(outPath + '恶性话题.mat', outPath + '良性目标话题.mat', outPath + '恶性良性相似度.txt', TOPIC_NUM)

    similar.main(outPath + '良性话题.mat', outPath + '恶性目标话题.mat', outPath + '良性恶性相似度.txt', TOPIC_NUM)
    similar.main(outPath + '恶性话题.mat', outPath + '恶性目标话题.mat', outPath + '恶性恶性相似度.txt', TOPIC_NUM)
    # print('获取相似度文件完毕')


def getRecommend(outPath):
    """
    获取推荐文件
    :return:
    """

    recommend.main(outPath + '良性权限矩阵.mat', outPath + '良性良性相似度.txt', outPath + '良性目标良性推荐.txt', 0.6)
    recommend.main(outPath + '恶性权限矩阵.mat', outPath + '恶性良性相似度.txt', outPath + '良性目标恶性推荐.txt', 0.4)
    recommend.main(outPath + '良性权限矩阵.mat', outPath + '良性恶性相似度.txt', outPath + '恶性目标良性推荐.txt', 0.6)
    recommend.main(outPath + '恶性权限矩阵.mat', outPath + '恶性恶性相似度.txt', outPath + '恶性目标恶性推荐.txt', 0.4)


def getTrainAndTargetPermissionFromDb(appids):
    """
    从数据库中获取到权限数据，需要注意的是，有更新的数据是哪些
    :param 良性测试appid:
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
    获取sf
    :return:
    """
    global end
    getSf(TOPIC_NUM, 1, limit, target)
    end = str(limit) + '过滤sf输出'
    print(str(TOPIC_NUM) + '话题' + end + '完成')


def minpMethod(TOPIC_NUM, limit, target):
    """
    获取minp
    :return:
    """
    global end
    # batMethod()
    end = str(limit) + '过滤minps输出'
    getSf(TOPIC_NUM, False, limit, target)
    print(str(TOPIC_NUM) + '话题' + end + '完成')


def batMethod():
    """
    恢复数据库
    :return:
    """
    bat = "mysql -uroot -p analyzedata < "  # + abspath
    os.system(bat)


### 创建多线程
# threads = []
# t1 = threading.Thread(target=sfMethod)
# threads.append(t1)
# t2 = threading.Thread(target=minpMethod)
# threads.append(t2)


def create_result(limit, target, topic_NUM):
    """
    创建文件到得出结果四个步骤
    :param limit:
    :param target:
    :param topic_NUM:
    :return:
    """
    localtime = time.asctime(time.localtime(time.time()))
    print(localtime, target, limit, topic_NUM, '输出开始')
    print('=====================================================================================')
    topicPath = '数据源/%s目标/%s话题数据输出/topic_test0201_suspend.txt' % (target, topic_NUM)
    if not (os.path.exists(topicPath)):
        # 创建话题文件
        # 数据源/' + target + '目标/' + str(TOPIC_NUM) + '话题数据输出'
        print(topicPath, "需要创建文件")
        cmd = "java -Dfile.encoding=utf-8 -jar topicNumGet.jar %s ./data ./数据源/%s目标" % (topic_NUM, target)
        print(cmd)
        os.system(cmd)
    sqlPath = ReData.main(topic_NUM, limit, target)
    sfMethod(topic_NUM, limit, target)
    minpMethod(topic_NUM, limit, target)
    # 计算写入数据库
    # cmd = "java -jar mappro.jar %s %s %s %s %s" % (topic_NUM, target, limit, './数据输出', './permission.json')
    cmd = "java -Dfile.encoding=utf-8 -jar mappro2.0.jar %s %s %s %s %s %s" % (topic_NUM, target, limit, './数据输出', './permission.json', config.sql_password)
    print(cmd)
    os.system(cmd)
    print('=====================================================================================')
    print(time.asctime(time.localtime(time.time())), target, limit, topic_NUM, '输出完成\r\n')


def getData():
    # topic_num_list = [60, 65, 70, 75, 80, 85, 90, 95, 100]
    # topic_num_list = [100 ]
    # target_list = ['20-80']
    # limit_list = [0.1]
    # for target in target_list:
    #    for topic_NUM in topic_num_list:
    #        for limit in limit_list:
    #            create_result(limit, target, topic_NUM)
    # matplot.getResult(topic_num_list, 'Topic Number', topic_num_list, target_list, limit_list,
    #                  'C:/Users/wzp12/Desktop/data', 'topic_num', topic_num_list)

    # topic_num_list = [100]
    # target_list = ['20-80']
    # limit_list = [0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6]
    # for target in target_list:
    #     for topic_NUM in topic_num_list:
    #         for limit in limit_list:
    #             create_result(limit, target, topic_NUM)
    # matplot.getResult(limit_list, 'Support Degree Threshold', topic_num_list, target_list, limit_list,
    #                   'C:/Users/wzp12/Desktop/data', 'threshold', limit_list)

    topic_num_list = [100]
    target_list = ['10-90', '15-85', '20-80', '25-75']  # ,'30-60','35-65', '40-60'
    limit_list = [0.1]
    for target in target_list:
        for topic_NUM in topic_num_list:
            for limit in limit_list:
                create_result(limit, target, topic_NUM)
    ta = [re.sub(r"-.{2}", '', x) for x in target_list]
    matplot.getResult(ta, 'Test Set Ratio', topic_num_list, target_list, limit_list,
                      'AppRiskEvaluation\第二步\数据输出\Data', 'test_proportion', target_list)


    # topic_num_list = [100]
    # target_list = ['20-80']
    # limit_list = [0.1]
    # for target in target_list:
    #     for topic_NUM in topic_num_list:
    #         for limit in limit_list:
    #             create_result(limit, target, topic_NUM)
    # matplot.getResult(topic_num_list, 'Topic Number', topic_num_list, target_list, limit_list,
    #                   'C:/Users/wzp12/Desktop/data', 'topic_num', topic_num_list)

if __name__ == '__main__':
    getData()

    print('输出完成')
