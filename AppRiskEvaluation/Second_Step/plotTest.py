import matplotlib.pyplot as plot

import common


def main(xlist, sf, minp):
    """
    绘图
    :param xlist:x
    :param sf:y1sf
    :param minp:y2minp
    :return:
    """
    # xlist = [60, 80, 100]
    #
    # minp = [0.336, 0.328, 0.341]
    # sf = [0.312, 0.321, 0.318]

    plot.figure()

    '''
    color：curve color, blue, green, red, etc
    label：legend, this parameter content is customized, note that if you write this parameter, plt.legend() must be added, then plt.show() will be useful!
    linestyle：curvilinear style, '-', '-', ':', etc
    linewidth：curve width, custom
    marker：well, it's like, 'o', 'x', which means that these symbols are going to mark specific 'points' on the curve, so that you can see where the support points are on the curve
    markersize：mark the size of the point, custom can be
    markerfacecolor:'blue'
    '''

    plot.plot(xlist, minp, "b-", marker='o', label='aupr_minp')
    plot.plot(xlist, sf, "g-", marker='^', label='aupr_sf')

    for a, b in zip(xlist, minp):
        plot.text(a, b, b, ha='center', va='bottom', fontsize=14)

    for a, b in zip(xlist, sf):
        plot.text(a, b, b, ha='center', va='bottom', fontsize=14)

    plot.legend()



    plot.xlabel('topic number')
    plot.ylabel('num')
    plot.title('20% test set ratio -0.1 support filtering threshold')

    plot.yscale('linear')
    plot.savefig('D:')

    plot.show()



def plotMat(xlist,xtype, topic_num_list, target_list, limit_list):
    """

    :param topic_num:
    :param target:
    :param limit:
    :return:
    """

    result_type = ['aupr']
    # result_type = ['aupr', 'rar', 'arisk', 'map']
    target_type = ['良']
    # target_type = ['良', '恶']
    type = [' SF ', ' minp ']

    for target in target_type:
        sql = "select distinct aupr, rar, arisk, map from riskgener_log where topic_num in  (%s) AND test_proportion in (%s) AND threshold in (%s) AND type in (%s) AND target_type in (%s)" % (
            str(topic_num_list).replace('[', '').replace(']', '')
            , str(target_list).replace('[', '').replace(']', '')
            , str(limit_list).replace('[', '').replace(']', '')
            , str([type[0]]).replace('[', '').replace(']', '')
            , str([target]).replace('[', '').replace(']', '')
        )
        sf_execute = common.sqlExecute(sql)

        sql = "select distinct aupr, rar, arisk, map from riskgener_log where topic_num in  (%s) AND test_proportion in (%s) AND threshold in (%s) AND type in (%s) AND target_type in (%s)" % (
            str(topic_num_list).replace('[', '').replace(']', '')
            , str(target_list).replace('[', '').replace(']', '')
            , str(limit_list).replace('[', '').replace(']', '')
            , str([type[1]]).replace('[', '').replace(']', '')
            , str([target]).replace('[', '').replace(']', '')
        )
        minp_execute = common.sqlExecute(sql)

        for index, result in enumerate(result_type) :
            sf_result = [float(x[index]) for x in sf_execute]
            minp_result = [float(x[index]) for x in minp_execute]
            main(xlist, sf_result, minp_result)
            print('%s对%s结果影响(%s性app作为测试集)'% (xtype, result, target))


if __name__ == '__main__':
    # main()

    topic_num_list = [65, 70, 75, 80, 85, 90, 95, 100]
    target_list = ['20-80']
    limit_list = [0.1]
    plotMat(topic_num_list, '话题', topic_num_list, target_list, limit_list)

    # topic_num_list = [100]
    # target_list = ['20-80']
    # limit_list = [0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6]
    #
    # topic_num_list = [100]
    # target_list = ['10-90', '15-85', '20-80', '25-75', '30-60', '35-65', '40-60']
    # limit_list = [0.1]
