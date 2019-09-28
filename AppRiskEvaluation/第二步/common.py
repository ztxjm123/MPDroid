import datetime

import pymysql as sql

sql_password = ''

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
    db = sql.connect(host="localhost", user="root", password=sql_password, db="analyzedata")
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
    pass