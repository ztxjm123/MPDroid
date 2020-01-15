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

PERMISSIONS = os.path.join('Data_Origin', 'permissions.json')
PERMISSIONS_LIST = json.load(open(PERMISSIONS, 'r'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)  # the number of permissions set

"""
Output step 2 iterative filtering
"""


def main(TOPIC_NUM, limit, target):
    """
    main
    :return:
    """
    # The read file is inside the loop

    # File input path, permission file needs to be changed, the unchanged file directly copied to the new file, the need to change the file first read the original path, and then copied to the new path output
    path = 'Iterativedata_Output/' + target + 'Target'
    common.mkdir(path)
    path = path + '/' + str(TOPIC_NUM) + 'Topics' + str(limit) + 'Filtering'
    common.mkdir(path)

    inPath = 'Data_Origin/' + target + 'Target/' + str(TOPIC_NUM) + 'topic_data_output'

    # # Cutting file
    operationfile.operation_file(TOPIC_NUM, limit, target)

    # use SQL
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

    for iterations in range(1):

        outPath = path + '/' + str(iterations)
        common.mkdir(outPath)

        for index in range(5):
            newPath = outPath + '/' + str(index)
            common.mkdir(newPath)

            # Divide the topic file and the permission file into 5 parts and take one of them in a loop. The permission file USES the database for easy modification
            # Appid
            Benign_testing_topic, Benign_training_topic, Benign_testing_appid, Benign_training_appid = getTrainAndTarget(inPath + '/Benign topic four-fifths.txt', index)
            Benign_testing_permission = getTrainAndTargetPermissionFromDb(Benign_testing_appid)
            Benign_training_permission = getTrainAndTargetPermissionFromDb(Benign_training_appid)

            Malicious_training_topic = open(inPath + '/Malicious topic four-fifths.txt', 'r').readlines()
            Malicious_training_permission = open(inPath + '/Malicious permission four-fifths.txt', 'r').readlines()



            topic.mainInList(Benign_testing_topic, newPath + '/Testing_benign_topic_matrix.mat', TOPIC_NUM)
            permission.mainInList(Benign_testing_permission, newPath + '/Testing_benign_permission_matrix.mat', 1)

            topic.mainInList(Benign_training_topic, newPath + '/Training_benign_topic_matrix.mat', TOPIC_NUM)
            permission.mainInList(Benign_training_permission, newPath + '/Training_benign_permission_matrix.mat', 1)

            topic.mainInList(Malicious_training_topic, newPath + '/Training_malicious_topic_matrix.mat', TOPIC_NUM)
            permission.mainInList(Malicious_training_permission, newPath + '/Training_malicious_permission_matrix.mat', False)


            similar.main(newPath + '/Training_benign_topic_matrix.mat'
                         , newPath + '/Testing_benign_topic_matrix.mat'
                         , newPath + '/Similarity-training_benign.txt'
                         , TOPIC_NUM)

            similar.main(newPath + '/Training_malicious_topic_matrix.mat'
                         , newPath + '/Testing_benign_topic_matrix.mat'
                         , newPath + '/Similarity-training_malicious.txt'
                         , TOPIC_NUM)


            recommend.main(newPath + '/Training_benign_permission_matrix.mat'
                           , newPath + '/Similarity-training_benign.txt'
                           , newPath + '/Recommend-training_benign.txt'
                           , 0.6)

            recommend.main(newPath + '/Training_malicious_permission_matrix.mat'
                           , newPath + '/Similarity-training_malicious.txt'
                           , newPath + '/Recommend-training_malicious.txt'
                           , 0.4)

            print("The output is complete, and the difference filtering is performed.")
            reSetPermission(newPath + '/Recommend-training_benign.txt', newPath + '/Recommend-training_malicious.txt', iterations)

    print('Done,start filtering')
    filter.main(limit)
    print('Filtering is complete,please back up the database data')

    batlogs = "mysqldump -u root -p%s analyzedata permissions permissionlog  > %s/%sTopics%sFiltering-including_log.sql" % (config.sql_password, path, str(TOPIC_NUM), str(limit))
    os.system(batlogs)

    sqlPath = path + "/" + str(TOPIC_NUM) + 'Topics' + str(limit) + 'Filtering' + ".sql"
    bat = "mysqldump -u root -p%s analyzedata permissions > %s" % (config.sql_password, sqlPath)
    os.system(bat)
    print('Filtering completed,database data backup complete')
    return sqlPath


def reSetPermission(BenignRecommend_file, MaliciousRecommend_file, times):
    """
    Recommend permission to find the difference set, and modify the database
    :param BenignRecommend_file:
    :param MaliciousRecommend_file:
    :return:
    """
    BenignRecommend = open(BenignRecommend_file, encoding='utf-8').readlines()
    MaliciousRecommend = open(MaliciousRecommend_file, encoding='utf-8').readlines()

    for (lineSus, lineMal) in zip(BenignRecommend, MaliciousRecommend):
        appBenignRecommend = json.loads(lineSus)
        appMaliciousRecommend = json.loads(lineMal)

        appId = appBenignRecommend['id']
        appBenignRecommendPer = listToDict(appBenignRecommend['permissions'])
        appMaliciousRecommendPer = listToDict(appMaliciousRecommend['permissions'])

        if (appMaliciousRecommendPer and appBenignRecommendPer):


            for perm in appMaliciousRecommendPer:
                contains = perm in appBenignRecommendPer.keys()
                if (not contains):

                    selectSql = "SELECT permission from permissions where appid={0}".format(appId)
                    execute = common.sqlExecute(selectSql)
                    rawPermission = execute[0][0].lower()
                    if not (rawPermission == 'null'):

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



def listToDict(param):
    dic = {}
    if (param):
        for perm in param:
            item = dict(zip(perm[0::1], perm[1::2]))
            dic.update(item)
    return dic


def filelistToDict(param):
    """
    Turn the permission file to dict
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
    Cut a file into five files
    :param list:
    :return:
    """


    file = open(filePath, 'r')
    list = file.readlines()

    Overall_data= []
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
            Overall_data.append(part)

    one_fifths = Overall_data[popIndex]
    four_fifths = []
    for i in range(len(Overall_data)):
        if (i != popIndex):
            four_fifths = four_fifths + Overall_data[i]

    one_fifthsxb = []
    four_fifthsxb = []
    for item in four_fifths:
        four_fifthsxb.append(int(item[0:6]))
    for item in one_fifths:
        one_fifthsxb.append(int(item[0:6]))

    return (one_fifths, four_fifths, one_fifthsxb, four_fifthsxb)


def getTrainAndTargetPermissionFromDb(appid):
    """
    When you get permission data from the database, it's important to note what data has been updatedWhen you get permission data from the database, it's important to note what data has been updated
    :param Benign testing appid:
    :return:
    """
    replace = str(appid).replace('[', '').replace(']', '')
    selectSql = "SELECT appid, permission from permissions where appid in ({0})".format(replace)
    getPermission = common.sqlExecute(selectSql)
    return getPermission


class common(object):


    def log(msg):

        now_time = datetime.datetime.now().strftime('%Y-%m-%d')
        print('== {} == {} =='.format(now_time, msg))
        return

    def mkdir(path):
        '''
        create
        :return:
        '''

        import os

        path = path.strip()

        path = path.rstrip("\\")

        isExists = os.path.exists(path)
        if not isExists:

            os.makedirs(path)

            print(path + ' Created successfully')
            return True
        else:

            print(path + ' Directory already exists')
            return False

    def sqlExecute(sqlstr):
        '''
        use SQL
        :param sqlstr:
        :return:
        '''
        db = sql.connect(host="localhost", user="root", password=config.sql_password, db="analyzedata")
        cursor = db.cursor()


        cursor.execute(sqlstr)

        data = cursor.fetchall()

        db.commit()
        cursor.close()
        db.close()

        return data


if __name__ == '__main__':
    # ， 100 80 60
    # TOPIC_NUM = 100
    # limit = 0.1

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
