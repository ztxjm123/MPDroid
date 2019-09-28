import matplotlib.pyplot as plot

import common


def printPlot(path, title, xlabel, ylabel, xlist, sf, minp, color):
    """
    绘图
    :param path: 图片存储路径
    :param title: 图表命名
    :param xlabel: x轴数据名
    :param ylabel: y轴数据名
    :param xlist: x轴数据
    :param sf: sf数据
    :param minp: minp数据
    :return:
    """

    plot.figure()
    '''
    color：曲线颜色，blue，green，red等等
    label：图例，这个参数内容就自定义啦，注意如果写这个参数一定要加上plt.legend()，之后再plt.show()才有有用！
    linestyle：曲线风格，’–’，’-.’，’:’等等
    linewidth：曲线宽度，自定义就可以
    marker：标记点样式，’o’,’x’，也就是说这些符号会标示出曲线上具体的“点”，这样一来就易于观察曲线上那些地方是支撑点
    markersize：标记点的大小，自定义就可以
    markerfacecolor:'blue'
    '''
    plot.plot(xlist, minp, color[0], marker='o', label='%s_MPDroid' % (ylabel))
    plot.plot(xlist, sf, color[1], marker='^', label='%s_SF' % (ylabel))
    # for a, b in zip(xlist, minp):
    #     plot.text(a, b, b, ha='center', va='bottom', fontsize=14)
    # for a, b in zip(xlist, sf):
    #     plot.text(a, b, b, ha='center', va='bottom', fontsize=14)
    plot.legend()  # 显示图例
    # plot.rcParams['font.sans-serif']=['SimHei'] #用来正常显示中文标签
    # plot.rcParams['axes.unicode_minus']=False #用来正常显示负号

    # plot.axis('normal')
    plot.xlim(xlist[0], xlist[len(xlist) - 1])

    plot.xlabel(xlabel)
    plot.ylabel('%s' % ylabel)
    plot.title(title)
    plot.yscale('linear')
    plot.grid(linestyle='--')
    plot.savefig(path)
    plot.show()
    # print('')


def getResult(xlist, xtype, topic_num_list, target_list, limit_list, save_path, columnfield, orderList):
    """

    :param xlist:
    :param xtype:
    :param topic_num_list:
    :param target_list:
    :param limit_list:
    :param save_path:
    :param columnfield:
    :param orderList:
    :return:
    """

    result_type = ['AUPR', 'RAR', 'ARISK', 'MAP']
    result_color = [['b-', 'r-'], ['g-', 'b-'], ['r-', 'm-'], ['m-', 'g-']]
    target_type = ['良', '恶']
    target_type_inen = ['Bengin', 'Malicious']
    type = [' SF ', ' minp ']

    for target in target_type:
        sql = "select distinct DISTINCT topic_num, test_proportion, threshold, type, target_type, aupr, rar, arisk, map from riskgener_log where topic_num in  (%s) AND test_proportion in (%s) AND threshold in (%s) AND type in (%s) AND target_type in (%s) ORDER BY FIELD(%s, %s)" % (
            str(topic_num_list).replace('[', '').replace(']', '')
            , str(target_list).replace('[', '').replace(']', '')
            , str(limit_list).replace('[', '').replace(']', '')
            , str([type[0]]).replace('[', '').replace(']', '')
            , str([target]).replace('[', '').replace(']', '')
            , columnfield
            , str(orderList).replace('[', '').replace(']', '')
        )
        common.log(sql)
        sf_execute = common.sqlExecute(sql)

        sql = "select distinct DISTINCT topic_num, test_proportion, threshold, type, target_type, aupr, rar, arisk, map from riskgener_log where topic_num in  (%s) AND test_proportion in (%s) AND threshold in (%s) AND type in (%s) AND target_type in (%s) ORDER BY FIELD(%s, %s)" % (
            str(topic_num_list).replace('[', '').replace(']', '')
            , str(target_list).replace('[', '').replace(']', '')
            , str(limit_list).replace('[', '').replace(']', '')
            , str([type[1]]).replace('[', '').replace(']', '')
            , str([target]).replace('[', '').replace(']', '')
            , columnfield
            , str(orderList).replace('[', '').replace(']', '')
        )
        common.log(sql)
        minp_execute = common.sqlExecute(sql)

        path_file = "%s/%s变量/%s性app作为测试集" % (save_path, xtype, target)
        common.mkdir(path_file)
        for index, result in enumerate(result_type):
            sf_result = [float(x[index + 5]) for x in sf_execute]
            minp_result = [float(x[index + 5]) for x in minp_execute]
            file_name = '%s/%s.png' % (path_file, result)

            printPlot(file_name, '%s App as Test Set' % (target_type_inen[target_type.index(target)]), xtype, result,
                      xlist, sf_result, minp_result, result_color[index])


if __name__ == '__main__':
    pass
