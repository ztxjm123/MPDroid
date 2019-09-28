import os

import scipy.io
import numpy as np

"""
切割和处理文件
"""

def exc_cmd():
    print(os.system("mysqldump -u root -p analyzedata permissions permissionlog  > %1.sql"))


def operation_file(TOPIC_NUM, limit, target):
    """
    切割文件
    :param TOPIC_NUM:
    :param limit:
    :param target:
    :return:
    """


    inPath = '数据源/' + target + '目标/' + str(TOPIC_NUM) + '话题数据输出'
    print(inPath)

    # 1、计算对比数量数据条数
    size = target.split('-')
    print(size[0], size[1])
    # 524 * (size / 100); 16343 / size
    m_less = int(524 * (int(size[0]) / 100))
    m_more = 524 - m_less
    print(m_less, m_more)

    s_less = int(16343 * (int(size[0]) / 100))
    s_more = 16343 - s_less
    print(s_less, s_more)

    # 读取文档
    m_permission_path = '数据源/恶性权限文件.txt'
    s_permission_path = '数据源/良性权限文件.txt'

    m_topic_path = inPath + '/topic_test0201_malicious.txt'
    s_topic_path = inPath + '/topic_test0201_suspend.txt'

    m_less_write_permission = inPath + '/恶性权限五分之一.txt'
    m_more_write_permission = inPath + '/恶性权限五分之四.txt'
    read_write_file(m_permission_path, m_less, m_less_write_permission, m_more_write_permission, '')

    m_less_write_topic = inPath + '/恶性话题五分之一.txt'
    m_more_write_topic = inPath + '/恶性话题五分之四.txt'
    read_write_file(m_topic_path, m_less, m_less_write_topic, m_more_write_topic, '')


    s_less_write_permission = inPath + '/良性权限五分之一.txt'
    s_more_write_permission = inPath + '/良性权限五分之四.txt'
    index_file = inPath + '/良性' + target + 'App.txt'
    read_write_file(s_permission_path, s_less, s_less_write_permission, s_more_write_permission, index_file)
    
    s_less_write_topic = inPath + '/良性话题五分之一.txt'
    s_more_write_topic = inPath + '/良性话题五分之四.txt'
    read_write_file(s_topic_path, s_less, s_less_write_topic, s_more_write_topic, '')

    print("源文件创建分割完成")


def read_write_file(file_path,less_size, less_file_path, more_file_path, index_file):
    """
    读取文件，分成两份
    :param file_path: 文件源
    :param less_size:较少的一份的行数
    :param less_file_path:较少的一份的文件路径
    :param more_file_path:较多的一份的文件路径
    :param index_file:良心较多的一份的appId集合，为空代表不需要
    :return:
    """
    m_less_data = []
    m_more_data = []
    index_data = []
    with open(file_path, encoding='utf-8', ) as txtfile:
        line = txtfile.readlines()
        for i, row in enumerate(line):
            if (i < less_size):
                m_less_data.append(row)
            else:
                m_more_data.append(row)
                if (index_file):
                    index_data.append(row[0:6])
    txtfile.close()
    print("读取完毕")

    print("写入测试部分数据")
    with open(less_file_path, 'w',) as f:
        for i in range(len(m_less_data)) :
            f.writelines(m_less_data[i])
    f.close()

    print("写入训练部分数据")
    with open(more_file_path, 'w',) as f:
        for i in range(len(m_more_data)) :
            f.writelines(m_more_data[i])
    f.close()

    if (index_file):
        print("写入良性训练部分数据的Appid")
        with open(index_file, 'w',) as f:
            for i in range(len(index_data)) :
                f.writelines(index_data[i] + '\r')
        f.close()

    print("写入完毕")

def getCtrm() :


    readlines1 = open('数据源/15-85目标/85话题数据输出/无标题.txt', 'r', encoding='utf-8').readlines()
    readlines2 = open('数据源/15-85目标/85话题数据输出/良性权限五分之四.txt', 'r').readlines()
    # print(permission_mat1)

    for item in range(len(readlines1)):
        readlines1[item] = readlines1[item].replace('\t', '  ')

    for i in range(len(readlines1)):
        if (readlines1[i] != readlines2[i]):
            print(readlines1[i][0:6])


    print("____________________________")
    # print(permission_mat2)
    print()



if __name__ == '__main__':
    # # exc_cmd()
    # # TOPIC_NUM = 65
    # TOPIC_NUM = 75
    # # TOPIC_NUM = 85
    # # TOPIC_NUM = 95
    # limit = 0.01
    # # limit = 0.5
    # # limit = 0.6
    # target = '15-85'
    # operation_file(TOPIC_NUM, limit, target)
    getCtrm()