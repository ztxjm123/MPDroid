#!/usr/bin/env python
# -*- coding: utf-8 -*-

import datetime
import json
import numpy
import scipy.io
import sys
import os

from scipy.sparse import csr_matrix

# Global variables
# NUM_TOPIC = 100
THRESHOLD = 0.4
SIM_TYPE = 'Euclidean'


def groupingApps(matrix, TOPIC_NUM):
    ''' Group all applications according to the membership of topics
    '''
    group_list = []
    for x in range(TOPIC_NUM):
        (st, ed) = matrix.indptr[x: x + 2]
        idxs = numpy.where(matrix.data[st: ed] > THRESHOLD)[0]
        group_list.append(set(matrix.indices[st + idxs].tolist()))

    return group_list


def newGroupForTestApp(ndarray, group_list):
    ''' Construct the new set for test vector
    '''
    new_group = set()
    for x in numpy.where(ndarray > THRESHOLD)[1]:
        new_group.update(group_list[x])

    return new_group


def calcSimilarityOfVectors(vect0, vect1, distType='Euclidean'):
    ''' Calculate the similarity of two vectors
    '''
    similarity = 0.0
    if distType == 'Euclidean':
        similarity = euclidean_sim(vect0, vect1)
    #     elif distType == 'Pearson': similarity = pearson_sim(vect0, vect1)
    #     elif distType == 'Cosine': similarity = cosine_sim(vect0, vect1)
    #     elif distType == 'Tanimoto': similarity = tanimoto_sim(vect0, vect1)
    else:
        similarity = 0.0

    return round(similarity, 4)


def euclidean_sim(vect0, vect1):
    distance = ((vect0 - vect1).power(2).sum()) ** 0.5
    return 1 / (1 + distance)


def getSimRankList(vect0, search_domain, topic_mat):
    ''' Get the rank list of vect0
    '''
    rank_list = []
    for idx in search_domain:
        vect1 = topic_mat.getrow(idx)
        rank_list.append((calcSimilarityOfVectors(vect0, vect1, SIM_TYPE), idx))
    rank_list.sort()
    rank_list.reverse()
    return [(pair[1], pair[0]) for pair in rank_list]  # pair <similarity, id>


def getTopicCoords(line):
    ''' Parse the lines in topic model, and convert them to coordinates
    '''
    topic_info = line.strip().split()
    row_num = int(topic_info[0])
    elements = topic_info[2:]

    rowids = [];
    colids = [];
    data = []
    for (idx, value) in dict(zip(elements[0::2], elements[1::2])).iteritems():
        rowids.append(row_num)
        colids.append(int(idx))
        data.append(round(float(value), 2))

    return rowids, colids, data


def convertTopicToSpMat(topic_file, TOPIC_NUM):
    all_topics = open(topic_file, 'r')
    row_ids = [];
    col_ids = [];
    data = []
    for line in all_topics:
        rows, cols, dataclips = getTopicCoords(line)
        row_ids.extend(rows)
        col_ids.extend(cols)
        data.extend(dataclips)
    mat_shape = (max(row_ids) + 1, TOPIC_NUM)

    return csr_matrix((data, (row_ids, col_ids)), shape=(max(row_ids) + 1, TOPIC_NUM))


def main(train_path, test_path, output_path, TOPIC_NUM):
    train_spm = scipy.io.loadmat(train_path)['matrix']  # train sparse matrix
    test_spm = scipy.io.loadmat(test_path)['matrix']  # train sparse matrix
    # test_spm = convertTopicToSpMat(test_path)
    clusters = groupingApps(train_spm, TOPIC_NUM)

    alltest = set(test_spm.nonzero()[0].tolist())
    totalnum = len(alltest)
    count = 0
    outfile = open(output_path, 'w')

    for idx in alltest:
        count += 1
        if count % 5 == 0:
            print('\r', "%s, processed: %d / %d" % (test_path, count, totalnum), end="")
            sys.stdout.flush()
        vect0 = test_spm.getrow(idx)
        search_domain = newGroupForTestApp(vect0.toarray(), clusters)
        rank_list = getSimRankList(vect0, search_domain, train_spm)
        line = json.dumps({'id': idx, 'rank_list': rank_list})
        outfile.write(line + '\n')
    print('\r')
    outfile.close()


if __name__ == '__main__':
    # if len(sys.argv) < 4:
    #     print 'USAGE:', sys.argv[0], '[TRAIN_TOPIC.MAT] [TEST_TOPIC.FILE] [OUTPUT_FILE]'
    # else:
    #     main(sys.argv[1], sys.argv[2], sys.argv[3])
    #     print sys.argv[2], "Finished!"

    main('data/.mat', 'data/mat', 'data/.txt')
    main('data/.mat', 'data/mat', 'data/.txt')

    main('data/.mat', 'data/mat', 'data/.txt')
    main('data/.mat', 'data/.mat', 'data/.txt')
