import json
import os


from ReData import common

PERMISSIONS = os.path.join('Data_Origin', 'permissions.json')
PERMISSIONS_LIST = json.load(open(PERMISSIONS, 'r'))
NUM_PERMISSIONS = len(PERMISSIONS_LIST)  # the number of permissions set


def main():
    """
    Convert the actual permission to the corresponding number
    :return:
    """

    selectSql = "SELECT id, lessper FROM `normal_test_permission`"
    execute = common.sqlExecute(selectSql)
    for item in execute:
        permissions = item[1].split(';')[:-1]  # delete the last blank string
        permissions = [it.lower() for it in permissions]

        indexPer = []
        for per in permissions:
            if (per in PERMISSIONS_LIST):
                index = PERMISSIONS_LIST.index(per)
                indexPer.append(index)
        insertSql = "insert into test_real_pers values({0}, '{1}')"\
            .format(item[0], str(indexPer))
        common.sqlExecute(insertSql)
        print(insertSql)

    print('ok')


def shell():
    print(os.system("tree"))


if __name__ == '__main__':
    # main();
    shell()
