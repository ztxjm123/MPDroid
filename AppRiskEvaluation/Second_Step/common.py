import datetime

import pymysql as sql

sql_password = ''


def log(msg):

    now_time = datetime.datetime.now().strftime('%Y-%m-%d')
    print('== {} == {} =='.format(now_time, msg))
    return

def mkdir(path):
    '''
    Create a folder
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
    Execute SQL statement
    :param sqlstr:
    :return:
    '''
    db = sql.connect(host="localhost", user="root", password=sql_password, db="analyzedata")
    cursor = db.cursor()

    cursor.execute(sqlstr)

    data = cursor.fetchall()

    db.commit()
    cursor.close()
    db.close()

    return data


if __name__ == '__main__':
    pass