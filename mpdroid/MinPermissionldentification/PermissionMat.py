#!/usr/bin/env python
# -*- coding: utf-8 -*-

import json
import numpy
from scipy.sparse import csr_matrix
import scipy.io
import sys
import os

PERMISSIONS = os.path.join('Data_Origin', 'permissions.json')
PERMISSIONS_LIST = json.load(open(PERMISSIONS, 'r'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)  # the number of permissions set


# Data processing functions
def getPermissionCoords(line, fromDb):
    ''' Parse the lines in permission file, and convert it to coordinates
    '''

    row_num = 0
    permissions = []
    if (fromDb):
        row_num = int(line[0])
        permissions = line[1].split(';')[:-1]  # delete the last blank string
        permissions = [it.lower() for it in permissions]
    else:
        app_info = line.strip().split()
        row_num = int(app_info[0])
        permissions = app_info[2].split(';')[:-1]  # delete the last blank string
        permissions = [it.lower() for it in permissions]

    # Get row id and column id
    rowids = [];
    colids = []
    for item in permissions:
        if item in PERMISSIONS_LIST:
            rowids.append(row_num)
            index = PERMISSIONS_LIST.index(item)
            colids.append(index)
        else:
            print('Error')

    return rowids, colids


def main(permission_file, prefix):
    ''' Convert App permissions to sparse matrix
    '''
    all_apps = open(permission_file, 'r')
    row_ids = [];
    col_ids = [];
    data = []
    for line in all_apps:
        rows, cols = getPermissionCoords(line, False)
        row_ids.extend(rows)
        col_ids.extend(cols)
    data = [1] * len(row_ids)
    sparse_mat = csr_matrix((data, (row_ids, col_ids)), shape=(max(row_ids) + 1, NUM_PERMISSIONS), dtype=numpy.int8)

    scipy.io.savemat(os.path.join(prefix), {'matrix': sparse_mat})
    print('Complete')


def mainInList(all_apps, prefix, fromDb):
    ''' Convert App permissions to sparse matrix
    '''
    row_ids = [];
    col_ids = [];
    data = []
    for line in all_apps:
        rows, cols = getPermissionCoords(line, fromDb)
        row_ids.extend(rows)
        col_ids.extend(cols)
    data = [1] * len(row_ids)
    sparse_mat = csr_matrix((data, (row_ids, col_ids)), shape=(max(row_ids) + 1, NUM_PERMISSIONS), dtype=numpy.int8)

    scipy.io.savemat(os.path.join(prefix), {'matrix': sparse_mat})
    print('Permission matrix acquisition complete')


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
