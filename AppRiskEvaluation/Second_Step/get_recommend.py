#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time
import json
import numpy
import sys
import scipy.io
import os

from scipy.sparse import csr_matrix

# Global variables
# Parameters
K = -1  # the top-k  if K == -1, use all apps in rank_list
THRESHOLD = 0.4
LEN_TOPIC = 100  # the number of topics in topic model
PERMISSIONS = os.path.join('Data_Origin', 'permissions.json')

PERMISSIONS_LIST = json.load(open(PERMISSIONS, 'r'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)  # the number of permissions set

LIMIT = 0.4  # 不使用 kNN 使用阈值


def getkNNs(rank_list, limit):
    ''' Get K-nearest neighbors from topic_file
    '''

    # upper = len(rank_list) if len(rank_list) < K else K
    # rank_list.append([])
    # list = []
    # ## 换成： 小于阈值就排除
    # # rank_list.remove()
    # if (len(rank_list) > 0) :
    #     for i in rank_list:
    #         if (i):
    #             i_ = i[1]
    #             if (i and i[1] > limit):
    #                 list.append(i)
    #
    # kNNs = {key: value for (key, value) in list}

    #
    upper = len(rank_list) if len(rank_list) < K else K
    rank_list.append([])
    kNNs = {key: value for (key, value) in rank_list[0:upper]}

    return kNNs


def recommendPermissions(kNNs, permission_mat, THRESHOLD):
    ''' Recommend permissions for test application
    '''
    # perm_vect = numpy.zeros(NUM_PERMISSIONS)
    perm_vect = csr_matrix((1, NUM_PERMISSIONS), dtype=numpy.float16)
    k_normalizing = 0.00001
    for idx in kNNs.keys():
        similarity = kNNs[idx]
        if K == -1 and similarity < THRESHOLD: continue
        k_normalizing += similarity
    for idx in kNNs.keys():
        similarity = kNNs[idx]
        # similarity >= threshold
        if K == -1 and similarity < THRESHOLD: continue
        perm_vect += permission_mat.getrow(idx) * similarity
    perm_vect = perm_vect / k_normalizing
    return list(zip(perm_vect.indices.tolist(), perm_vect.data.tolist()))


def main(permission_file, rank_file, out_file, limit):
    # input files
    permission_mat = scipy.io.loadmat(permission_file)['matrix']
    rankfp = open(rank_file, 'r')
    outfp = open(out_file, 'w')

    count = 0
    for line in rankfp:
        count += 1
        if count % 5 == 0:
            print('\r', "%s, processed: %d" % (rank_file, count), end="")
            sys.stdout.flush()
        # Find Top-K most similar Neighbors
        nn_list = json.loads(line.strip())  # nearst neighbors list
        appid = nn_list['id']
        rank_list = nn_list['rank_list']
        kNNs = getkNNs(rank_list, 0.4)

        # Recommanded permissions from k-nearest neighbors
        recommand_perms = recommendPermissions(kNNs, permission_mat, limit)
        outfp.write(json.dumps({'id': appid, 'permissions': recommand_perms}) + '\n')
    print('\r')
    outfp.close()


if __name__ == '__main__':
    # if len(sys.argv) != 4:
    #     print 'USAGE:', sys.argv[0], '[PERMISSIONS.MAT] [RANK_LIST] [OUT_FILE]'
    # else:
    #     start = time.clock()
    #     main(sys.argv[1], sys.argv[2], sys.argv[3])
    #     end = time.clock()
    #     print (end - start)

    ## 良性推荐过滤0.6， 恶性0.4
    main('data/良性权限矩阵.mat', 'data/良性良性相似度.txt', 'data/良性目标良性推荐.txt', 0.6)
    main('data/恶性权限矩阵.mat', 'data/恶性良性相似度.txt', 'data/良性目标恶性推荐.txt', 0.4)

    main('data/良性权限矩阵.mat', 'data/良性恶性相似度.txt', 'data/恶性目标良性推荐.txt', 0.6)
    main('data/恶性权限矩阵.mat', 'data/恶性恶性相似度.txt', 'data/恶性目标恶性推荐.txt', 0.4)
