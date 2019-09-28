import datetime
import pymysql as sql
import json
import os
import TopicMat as topic
import PermissionMat as permission
import getSimilarity as similar
import get_recommend as recommend
import filter
import operationfile
import common as config

PERMISSIONS = os.path.join('数据源', 'permissions.json')
PERMISSIONS_LIST = json.load(open(PERMISSIONS, 'r'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)  # the number of permissions set

"""
输出第二步迭代筛选
"""


def main(TOPIC_NUM, limit, target):
    """
    主函数
    :return:
    """
    # 读取文件是在循环里面

    # 文件输入路径，权限文件需要改动，不动的文件直接复制到新的文件中，需要改动的文件先读取原来的路径，然后再复制到新的路径中输出
    path = '迭代数据输出/' + target + '目标'
    common.mkdir(path)
    path = path + '/' + str(TOPIC_NUM) + '话题' + str(limit) + '过滤'
    common.mkdir(path)

    inPath = '数据源/' + target + '目标/' + str(TOPIC_NUM) + '话题数据输出'

    # # 处理切割文件
    operationfile.operation_file(TOPIC_NUM, limit, target)

    # 执行SQL清空表用于存储数据
    drop_permissions = "DROP TABLE IF EXISTS permissions; "
    common.sqlExecute(drop_permissions)

    drop_permissionslog = "DROP TABLE IF EXISTS permissionlog;"
    common.sqlExecute(drop_permissionslog)

    create_permissions = "create table permissions ( id int auto_increment primary key, times int null comment '迭代次数', appid int null comment 'Appid', appname varchar(255)  null comment 'app名称', permission varchar(5000) null comment '权限字符串', mal int(4) null comment '恶性App');"
    common.sqlExecute(create_permissions)

    create_permissionslog = "create table permissionlog ( id int auto_increment primary key, appid int null comment 'appid', times int null comment '循环次数', old varchar(5000) null comment '原始权限', new varchar(5000) null comment '新的权限', remove varchar(255) null comment '移除的权限', `create` datetime null comment '时间');"
    common.sqlExecute(create_permissionslog)

    insert_data = "insert into permissions SELECT null , 0 , id , docid , lessper , maliciousFlag from normal_test_permission;"
    common.sqlExecute(insert_data)

    for 迭代次数 in range(1):
        # 创建迭代输出路径
        outPath = path + '/' + str(迭代次数)
        common.mkdir(outPath)

        for index in range(5):
            newPath = outPath + '/' + str(index)
            common.mkdir(newPath)

            # 将话题文件和权限文件分成5份，循环取其中一份，权限文件使用数据库，方便修改
            # 获取到Appid
            良性测试话题, 良性训练话题, 良性测试appid, 良性训练appid = getTrainAndTarget(inPath + '/良性话题五分之四.txt', index)
            良性测试权限 = getTrainAndTargetPermissionFromDb(良性测试appid)
            良性训练权限 = getTrainAndTargetPermissionFromDb(良性训练appid)

            恶性训练话题 = open(inPath + '/恶性话题五分之四.txt', 'r').readlines()
            恶性训练权限 = open(inPath + '/恶性权限五分之四.txt', 'r').readlines()

            #
            # 良性测试话题, 良性训练话题 = getTrainAndTarget(inPath + '/良性话题文件.txt', index)
            # 良性测试权限, 良性训练权限 = getTrainAndTarget(inPath + '/良性权限文件.txt.txt', index)
            #
            # 恶性训练话题 = open(inPath + '/恶性话题文件.txt', 'r').readlines()
            # 恶性训练权限 = open(inPath + '/恶性权限文件.txt', 'r').readlines()

            ## 获取话题、权限矩阵
            ### 良性测试集
            topic.mainInList(良性测试话题, newPath + '/测试良性话题矩阵.mat', TOPIC_NUM)
            permission.mainInList(良性测试权限, newPath + '/测试良性权限矩阵.mat', 1)
            ### 良性训练集
            topic.mainInList(良性训练话题, newPath + '/训练良性话题矩阵.mat', TOPIC_NUM)
            permission.mainInList(良性训练权限, newPath + '/训练良性权限矩阵.mat', 1)
            ### 恶性训练集
            topic.mainInList(恶性训练话题, newPath + '/训练恶性话题矩阵.mat', TOPIC_NUM)
            permission.mainInList(恶性训练权限, newPath + '/训练恶性权限矩阵.mat', False)

            ## 获取相似度矩阵两个
            ### 良性训练集与测试集的相似度
            similar.main(newPath + '/训练良性话题矩阵.mat'
                         , newPath + '/测试良性话题矩阵.mat'
                         , newPath + '/相似度-训练良性.txt'
                         , TOPIC_NUM)
            ### 恶性训练集与测试集的相似度
            similar.main(newPath + '/训练恶性话题矩阵.mat'
                         , newPath + '/测试良性话题矩阵.mat'
                         , newPath + '/相似度-训练恶性.txt'
                         , TOPIC_NUM)

            ## 获取推荐文件
            ### 良性训练权限，良性训练集与测试集的相似度矩阵输入
            recommend.main(newPath + '/训练良性权限矩阵.mat'
                           , newPath + '/相似度-训练良性.txt'
                           , newPath + '/推荐-训练良性.txt'
                           , 0.6)
            ### 恶性训练权限，恶性训练集与测试集的相似度矩阵输入
            recommend.main(newPath + '/训练恶性权限矩阵.mat'
                           , newPath + '/相似度-训练恶性.txt'
                           , newPath + '/推荐-训练恶性.txt'
                           , 0.4)

            print("输出完成，进行差集过滤")
            reSetPermission(newPath + '/推荐-训练良性.txt', newPath + '/推荐-训练恶性.txt', 迭代次数)

    print('完成，开始过滤')
    filter.main(limit)
    print('过滤完成，请备份数据库数据')

    # 数据库备份
    batlogs = "mysqldump -u root -p%s analyzedata permissions permissionlog  > %s/%s话题%s过滤含日志.sql" % (config.sql_password, path, str(TOPIC_NUM), str(limit))
    os.system(batlogs)

    sqlPath = path + "/" + str(TOPIC_NUM) + '话题' + str(limit) + '过滤' + ".sql"
    bat = "mysqldump -u root -p%s analyzedata permissions > %s" % (config.sql_password, sqlPath)
    os.system(bat)
    print('过滤完成，数据库数据备份完成')
    return sqlPath


def reSetPermission(良性推荐文件, 恶性推荐文件, times):
    """
    推荐权限求差集，并修改数据库
    :param 良性推荐文件:
    :param 恶性推荐文件:
    :return:
    """
    良性推荐 = open(良性推荐文件, encoding='utf-8').readlines()
    恶性推荐 = open(恶性推荐文件, encoding='utf-8').readlines()

    for (lineSus, lineMal) in zip(良性推荐, 恶性推荐):
        app良性推荐 = json.loads(lineSus)
        app恶性推荐 = json.loads(lineMal)

        appId = app良性推荐['id']
        app良性推荐权限 = listToDict(app良性推荐['permissions'])
        app恶性推荐权限 = listToDict(app恶性推荐['permissions'])

        if (app恶性推荐权限 and app良性推荐权限):
            ## 不为空
            for perm in app恶性推荐权限:
                contains = perm in app良性推荐权限.keys()
                if (not contains):
                    ### 获取到原始的权限
                    selectSql = "SELECT permission from permissions where appid={0}".format(appId)
                    execute = common.sqlExecute(selectSql)
                    rawPermission = execute[0][0].lower()
                    if not (rawPermission == 'null'):
                        ### 是否存在于原始的文件中
                        permRemove = PERMISSIONS_LIST[perm]
                        isInNeedRemove = permRemove in rawPermission
                        if (isInNeedRemove):
                            rawPermission_arr = rawPermission.split(';')
                            for index, item in enumerate(rawPermission_arr):
                                if (item.lower() == permRemove.lower()):
                                    rawPermission_arr.remove(item)
                                    rawPermission = ';'.join(rawPermission_arr)

                            # removePerm = rawPermission.replace(permRemove, '')
                            remove = rawPermission
                            ## 写入数据库中，方便修改
                            insertLog = "insert into permissionlog values (null, {0}, {1}, '{2}', '{3}', '{4}', '{5}')".format(
                                appId
                                , times
                                , rawPermission
                                , remove
                                , permRemove
                                , datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
                            # print(insertLog)
                            common.sqlExecute(insertLog)
                            updateSql = "UPDATE permissions set permission='{0}',times={2} where appid={1} ".format(
                                remove, appId, times + 1)
                            common.sqlExecute(updateSql)
                    ## 获取数据去除？

                # app良性推荐权限


def listToDict(param):
    dic = {}
    if (param):
        for perm in param:
            item = dict(zip(perm[0::1], perm[1::2]))
            dic.update(item)
    return dic


def filelistToDict(param):
    """
    将权限文件转为dict
    :param param:
    :return:
    """
    dic = {}
    for item in param:
        split = item.split()
        if (split[2]):
            itemDic = {split[0]: split[2]}
            dic.update(itemDic)

    return dic


def getTrainAndTarget(filePath, popIndex):
    """
    将一个文件切割成5个文件
    :param list:
    :return:
    """

    # 读取文件
    file = open(filePath, 'r')
    list = file.readlines()

    整体数据 = []
    if (list):
        length = len(list)
        ends = length % 5
        size = int((length - ends) / 5)
        for i in range(5):
            start = size * i
            end = 0
            if (i == 4):
                end = length
            else:
                end = size * (i + 1)
            part = list[start:end]
            整体数据.append(part)

    五分之一 = 整体数据[popIndex]
    五分之四 = []
    for i in range(len(整体数据)):
        if (i != popIndex):
            五分之四 = 五分之四 + 整体数据[i]

    # 返回下标
    五分之一下标 = []
    五分之四下标 = []
    for item in 五分之四:
        五分之四下标.append(int(item[0:6]))
    for item in 五分之一:
        五分之一下标.append(int(item[0:6]))

    return (五分之一, 五分之四, 五分之一下标, 五分之四下标)


def getTrainAndTargetPermissionFromDb(appid):
    """
    从数据库中获取到权限数据，需要注意的是，有更新的数据是哪些
    :param 良性测试appid:
    :return:
    """
    replace = str(appid).replace('[', '').replace(']', '')
    selectSql = "SELECT appid, permission from permissions where appid in ({0})".format(replace)
    getPermission = common.sqlExecute(selectSql)
    return getPermission


class common(object):

    ## 日志打印
    def log(msg):
        ## 获取系统时间
        now_time = datetime.datetime.now().strftime('%Y-%m-%d')
        print('== {} == {} =='.format(now_time, msg))
        return

    def mkdir(path):
        '''
        创建文件夹
        :return:
        '''
        # 引入模块
        import os

        # 去除首位空格
        path = path.strip()
        # 去除尾部 \ 符号
        path = path.rstrip("\\")

        # 判断路径是否存在
        # 存在     True
        # 不存在   False
        isExists = os.path.exists(path)

        # 判断结果
        if not isExists:
            # 如果不存在则创建目录
            # 创建目录操作函数
            os.makedirs(path)

            print(path + ' 创建成功')
            return True
        else:
            # 如果目录存在则不创建，并提示目录已存在
            print(path + ' 目录已存在')
            return False

    def sqlExecute(sqlstr):
        '''
        执行SQL语句
        :param sqlstr:
        :return:
        '''
        db = sql.connect(host="localhost", user="root", password=config.sql_password, db="analyzedata")
        cursor = db.cursor()

        # 使用execute()方法执行SQL语句
        cursor.execute(sqlstr)

        # 使用fetall()获取全部数据
        data = cursor.fetchall()

        # 关闭游标和数据库的连接
        db.commit()
        cursor.close()
        db.close()

        return data


if __name__ == '__main__':
    # 话题数量， 100 80 60
    # TOPIC_NUM = 100
    # limit = 0.1

    # 1、话题		65、75、85、95
    # 2、测试集比例 	15-85 / 25-75 / 35-65 / 40-60
    # 3、阈值		0.01/0.5/0.6

    # TOPIC_NUM = 65
    # TOPIC_NUM = 75
    TOPIC_NUM = 85
    # TOPIC_NUM = 95
    limit = 0.01
    # limit = 0.5
    # limit = 0.6
    target = '15-85'
    # target = '25-75'
    # target = '35-65'
    # target = '40-60'

    main(TOPIC_NUM, limit, target)
    # reSetPermission()
