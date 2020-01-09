#!/usr/bin/env python
# -*- coding: utf-8 -*-

import numpy
import scipy.io
import sys
import os

from scipy.sparse import csr_matrix

# Global variable
# TOPIC_NUM = 100

def getTopicCoords(line):
    ''' Parse the lines in topic model, and convert them to coordinates
    '''
    topic_info = line.strip().split()
    row_num = int(topic_info[0])
    elements = topic_info[2:]

    rowids = []; colids = []; data = []
    for (idx, value) in dict(zip(elements[0::2], elements[1::2])).items():
        rowids.append(row_num)
        colids.append(int(idx))
        data.append(round(float(value), 2))

    return rowids, colids, data

def mainInList(all_topics, prefix, TOPIC_NUM):
    '''
    '''
    row_ids = []; col_ids = []; data = []
    for line in all_topics:
        rows, cols, dataclips = getTopicCoords(line)
        row_ids.extend(rows)
        col_ids.extend(cols)
        data.extend(dataclips)
    mat_shape = (max(row_ids) + 1, TOPIC_NUM)
    sparse_mat = csr_matrix((data, (row_ids, col_ids)), shape=mat_shape, dtype=numpy.float16)

    scipy.io.savemat(os.path.join(prefix), {'matrix': sparse_mat})
    print('Topic matrix acquisition complete')

def main(topic_file, prefix, TOPIC_NUM):
    '''
    '''
    all_topics = open(topic_file, 'r')
    row_ids = []; col_ids = []; data = []
    for line in all_topics:
        rows, cols, dataclips = getTopicCoords(line)
        row_ids.extend(rows)
        col_ids.extend(cols)
        data.extend(dataclips)
    mat_shape = (max(row_ids) + 1, TOPIC_NUM)
    sparse_mat = csr_matrix((data, (row_ids, col_ids)), shape=mat_shape, dtype=numpy.float16)

    scipy.io.savemat(os.path.join(prefix), {'matrix': sparse_mat})
    print('Complete')


if __name__ == '__main__':
    # if len(sys.argv) < 3:
    #     print 'USAGE:', sys.argv[0], '[INPUT_FILE] [PREFIX]'
    #     print 'PREFIX: train, test'
    # else:
    #     main(sys.argv[1], sys.argv[2])


    main('.txt', 'b')
    main('.txt', 'm')
    main('.txt', 'b')
    main('.txt', 'm')
