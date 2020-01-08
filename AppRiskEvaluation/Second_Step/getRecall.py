
import json
import os
import re
import numpy
import sys


intpath = 'Data_Origin/Benign-permission-file.txt'
onpath = 'Data_Origin/Malicious-permission-file.txt'
scc = 'Data_Output/20-80Target/100topic_data_output0.1Filtering minps Output/Benign-benign_Recommend.txt'
sec = 'Data_Output/20-80Target/100topic_data_output0.1Filtering sf Output/Benign-benign_Recommend.txt'
erc = 'Data_Output/20-80Target/100topic_data_output0.1Filtering minps Output/Malicious-benign_Recommend.txt'
enc = 'Data_Output/20-80Target/100topic_data_output0.1Filtering sf Output/Malicious-benign_Recommend.txt'


def topission():
    PERMISSIONS = os.path.join('Data_Origin', 'permissions.json')
    PERMISSIONS_LIST = json.load(open(PERMISSIONS, 'r'))
    NUM_PERMISSIONS = len(PERMISSIONS_LIST)  # the number of permissions set
    # print(NUM_PERMISSIONS)
    return PERMISSIONS_LIST


def getline(path,n):

    list = []
    f = open(path, 'r')
    line = f.readline()
    for i in range(0,n):
        list.append(line)
        line = f.readline()
    f.close()
    return (list)

def getlxtxt(path,n):

    lxlist=[]
    List1 = getline(path,n)
    for i in List1:
        ix = re.findall(r"permission.(.+?);",i)
        lxlist.append(ix)
    return lxlist


def gettjtxt(sc,n):

    List2 = getline(sc,n)
    tj_list = []
    #print(List2)
    for i in List2:
        p_list = topission()
        tjlist = []
        #print(i)
        zfc = re.findall(r": \[(.+?)\]}",i)
        #print(zfc)
        for j in zfc:
            prission = re.findall(r"\[(.+?)\,",j)
            #print(prission)
            priss = re.findall(r",(.+?)],", j)
            for p in prission:
                x = p_list[eval(p)]  # 匹配com.开头的permission
                # print(x)
                ix = re.findall(r"permission.(.+)", x)
                # print(ix)
                tjlist.append(ix[0].upper())
            #print(tjlist)
            #print(priss)
            for maxp in range(len(priss)):
                for r in range(maxp+1,len(priss)):
                    if eval(priss[r]) >= eval(priss[maxp]):
                        priss[maxp], priss[maxp + (r-maxp)] = priss[maxp + (r-maxp)], priss[maxp]
                        tjlist[maxp], tjlist[maxp + (r-maxp)] = tjlist[maxp + (r-maxp)], tjlist[maxp]
        tj_list.append(tjlist)
            #print(tjlist)
            #print(priss)
            #print('\n')
    #print(tjlist)
    return tj_list

def getnr(path,sc,n):

    NR = 0
    num = 0
    list1 = getlxtxt(path,n)
    list2 = gettjtxt(sc,n)
    hebi = []
    for i in range(len(list1)):
        try:
            lxlist = list1[i]
            # print(len(lxlist))
            tjlist = list2[i]
            newtjlist = []
            for j in range(len(lxlist)):
                newtjlist.append(tjlist[j])
            # print(newtjlist)
            c = [x for x in newtjlist if x in lxlist]
            #hebi.append(c)
            nr = len(c) / len(newtjlist)
            NR += nr
            num += 1
        except:
            num += 0
            pass

    #print('\r', "processed: %d / %d" % (num , len(list1)))
    print('NR=', end="")
    print(NR / num)
    #print(hebi)

def gettrr(path,sc,n):

    TRR = 0
    num = 0
    list1 = getlxtxt(path,n)
    list2 = gettjtxt(sc,n)
    for i in range(len(list1)):
        try:
            lxlist = list1[i]
            #print(lxlist)
            tjlist = list2[i]
            #print(tjlist)
            if tjlist:
                c = [x for x in lxlist if x in tjlist]
                # print(c)
                if len(c) == len(lxlist):
                    for j in range(len(tjlist)):
                        if tjlist[j] == c[-1]:
                            leng = j + 1
                    trr = leng / len(lxlist)
                    TRR += trr
                    num += 1
                else:
                    trr = len(tjlist) / len(lxlist)
                    TRR += trr
                    num += 1
        except:
            num += 0
            pass
    #print('\r', "processed: %d / %d" % (num, len(list1)))
    print('TRR=', end="")
    print(TRR / num)

def getP(k,lxlist,tjlist):
    newtjlist = []
    for j in range(k):
        newtjlist.append(tjlist[j])
    # print(newtjlist)
    c = [x for x in lxlist if x in newtjlist]
    # hebi.append(c)
    Pk = len(c) / k
    return Pk

def getRel(k,lxlist,tjlist):
    if tjlist[k-1] in lxlist:
        return 1
    else:
        return 0

def getmap(path,sc,n):
    MAP = 0
    num = 0
    list1 = getlxtxt(path, n)
    list2 = gettjtxt(sc, n)
    for i in range(len(list1)):
        try:
            lxlist = list1[i]
            #print(lxlist)
            tjlist = list2[i]
            #print(tjlist)
            if tjlist:
                AvgP = 0
                for k in range(len(tjlist)):
                    Pk = getP(k + 1, lxlist, tjlist)
                    #print(Pk)
                    Relk = getRel(k + 1, lxlist, tjlist)
                    #print(Relk)
                    Avg = Pk * Relk
                    #print(Avg)
                    AvgP +=Avg / len(lxlist)
                #print(AvgP)
                MAP += AvgP
                #print(MAP)
                num += 1
        except:
            num += 0
            pass
    #print('\r', "processed: %d / %d" % (num, len(list1)))
    print('MAP=', end="")
    print(MAP / num)


def main():
    method_list = ['---MPDrold---','---SF---']
    type_list = ['Benign:','Malicious:']
    my_path = [intpath, onpath]
    my_min= [scc, erc]
    my_sf = [sec,enc]
    num = [3268,104]
    for j in range(len(method_list)):
        if j==0:
            print(method_list[j])
            for i in range(len(type_list)):
                print(type_list[i])
                getnr(my_path[i], my_min[i], num[i])
                gettrr(my_path[i], my_min[i], num[i])
                getmap(my_path[i], my_min[i], num[i])
        else:
            print(method_list[j])
            for i in range(len(type_list)):
                print(type_list[i])
                getnr(my_path[i], my_sf[i], num[i])
                gettrr(my_path[i], my_sf[i], num[i])
                getmap(my_path[i], my_sf[i], num[i])

if __name__ == '__main__':
    main()
