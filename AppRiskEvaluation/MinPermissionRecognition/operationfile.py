import os

import scipy.io
import numpy as np

"""
Cutting processing file
"""

def exc_cmd():
    print(os.system("mysqldump -u root -p analyzedata permissions permissionlog  > %1.sql"))


def operation_file(TOPIC_NUM, limit, target):
    """
    Cutting file
    :param TOPIC_NUM:
    :param limit:
    :param target:
    :return:
    """


    inPath = 'Data_Origin/' + target + 'Target/' + str(TOPIC_NUM) + 'topic_data_output'
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
    m_permission_path = 'Data_Origin/Malicious-permission-file.txt'
    s_permission_path = 'Data_Origin/Benign-permission-file.txt'

    m_topic_path = inPath + '/topic_test0201_malicious.txt'
    s_topic_path = inPath + '/topic_test0201_suspend.txt'

    m_less_write_permission = inPath + '/Malicious permission one-fifths.txt'
    m_more_write_permission = inPath + '/Malicious permission four-fifths.txt'
    read_write_file(m_permission_path, m_less, m_less_write_permission, m_more_write_permission, '')

    m_less_write_topic = inPath + '/Malicious topic one-fifths.txt'
    m_more_write_topic = inPath + '/Malicious topic four-fifths.txt'
    read_write_file(m_topic_path, m_less, m_less_write_topic, m_more_write_topic, '')


    s_less_write_permission = inPath + '/Benign permission one-fifths.txt'
    s_more_write_permission = inPath + '/Benign permission four-fifths.txt'
    index_file = inPath + '/Benign' + target + 'App.txt'
    read_write_file(s_permission_path, s_less, s_less_write_permission, s_more_write_permission, index_file)
    
    s_less_write_topic = inPath + '/Benign topic one-fifths.txt'
    s_more_write_topic = inPath + '/Benign topic four-fifths.txt'
    read_write_file(s_topic_path, s_less, s_less_write_topic, s_more_write_topic, '')

    print("Source file creation split completed")


def read_write_file(file_path,less_size, less_file_path, more_file_path, index_file):
    """
    Read the file and divide it into two parts
    :param file_path: Data_Origin
    :param less_size: the number of rows for the smaller order
    :param less_file_path:a smaller file path
    :param more_file_path:more than one file path
    :param index_file:conscience more a set of appids, empty represents not needed
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
    print("Read complete")

    print("Write part of the test data")
    with open(less_file_path, 'w',) as f:
        for i in range(len(m_less_data)) :
            f.writelines(m_less_data[i])
    f.close()

    print("Write part of the training data")
    with open(more_file_path, 'w',) as f:
        for i in range(len(m_more_data)) :
            f.writelines(m_more_data[i])
    f.close()

    if (index_file):
        print("Write the Appid to the benign training part of the data")
        with open(index_file, 'w',) as f:
            for i in range(len(index_data)) :
                f.writelines(index_data[i] + '\r')
        f.close()

    print("Write complete")

def getCtrm() :


    readlines1 = open('Data_Origin/%sTarget/%stopic_data_output/none.txt', 'r', encoding='utf-8').readlines()
    readlines2 = open('Data_Origin/%sTarget/%stopic_data_output/Benign permission four-fifths.txt', 'r').readlines()
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