import datetime
import json
import os
import sys

import common


PERMISSIONS_LIST = json.load(open('permission.json'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)

#  设置第三步到第二步的过滤 0.1 0.2


def main(limit):
    """
    用于第三步过滤第二步的权限数据
    :return:
    """
    ## 1、读取两个文件，转 dict
    良性App过滤权限 = toDict(open('数据源/合并过滤权限支持度/合并权限-良性App.txt', 'r').readlines())
    恶性App过滤权限 = toDict(open('数据源/合并过滤权限支持度/合并权限-恶性App.txt', 'r').readlines())
    良性训练App = open('数据源/合并过滤权限支持度/十分之九良性App.txt', 'r').readlines()

    for app in 良性训练App:
        permission = 良性App过滤权限[int(app)]
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
                                # 移除
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
                                print('\r', '当前操作AppID：' + app, end="")
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