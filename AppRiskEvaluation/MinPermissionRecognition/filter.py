import datetime
import json
import os
import sys

import common


PERMISSIONS_LIST = json.load(open('permission.json'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)



def main(limit):
    """
    Used to filter the permission data of step 2 in step 3
    :return:
    """
    Benign_App_filtering_permission = toDict(open('Data_Origin/Merge-filter-permission-support/Merge_permission-benignApp.txt', 'r').readlines())
    Malicious_App_filtering_permission = toDict(open('Data_Origin/Merge-filter-permission-support/Merge_permission-maliciousApp.txt', 'r').readlines())
    Benign_training_App = open('Data_Origin/Merge-filter-permission-support/Nine_over_ten-benignApp.txt', 'r').readlines()

    for app in Benign_training_App:
        permission = Benign_App_filtering_permission[int(app)]
        selectSql = " select permission from permissions WHERE appid = {0}".format(int(app))
        execute = common.sqlExecute(selectSql)
        if (execute):
            rawPermission = execute[0][0]
            if not (rawPermission == 'null'):
                raw_permission_split = rawPermission.split(';')[:-1]
                for per in raw_permission_split:
                    if (per.strip()):
                        index = PERMISSIONS_LIST.index(per.lower())
                        if (index in permission.keys()) :
                            possibility = permission[index]
                            if possibility < limit:
                                rawPermission_arr = rawPermission.split(';')
                                for index, item in enumerate(rawPermission_arr):
                                    if (item.lower() == per.lower()):
                                        rawPermission_arr.remove(item)
                                        rawPermission = ';'.join(rawPermission_arr)
                                replace = rawPermission
                                if (replace == ';' or replace == '' ):
                                    replace = 'null'
                                insertLog = "insert into permissionlog values (null, {0}, {1}, '{2}', '{3}', '{4}', '{5}')".format(
                                    int(app)
                                    , -1
                                    , rawPermission
                                    , replace
                                    , per.lower() + " " + str(possibility)
                                    , datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
                                print('\r', 'Current operation AppID：' + app, end="")
                                sys.stdout.flush()
                                common.sqlExecute(insertLog)
                                updateSql = "UPDATE permissions set permission='{0}',times={2} where appid={1} ".format(replace, int(app), -1)
                                common.sqlExecute(updateSql)

    print('\r')
    print('load')

def toDict(file):
    dic = {}
    if (file):
        for app in file:
            loads = json.loads(app)
            list = {}
            for item in loads['permissions']:
                list[item[0]] = item[1]
            dic[loads['id']] = list
    return dic

if __name__ == '__main__':

    limit = 0.1
    main(limit)