import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * @ClassName APPPrivilegeProbablity
 * @Description TODO
 * @Author miaoxu
 * @Date 2018/12/28 20:54
 * @Version 1.0
 **/
public class APPPrivilegeProbablity {
    public static void main(String[] args) throws IOException, JSONException {
        getApp();
        String daimaPath = "数据源/正式数据源/测试输出/代码权限-良性App.txt";
        String shengmingPath = "数据源/正式数据源/测试输出/声明权限-良性App.txt";

        HashMap<Integer, App> together = getTogether(daimaPath, shengmingPath);

        new APPPrivilegeProbablity().writeFile("数据源/正式数据源/测试输出/合并权限-良性App.txt", together);

        System.out.println("输出完成");

        return;
    }


    //<editor-fold desc="计算">

    private static void getApp() throws IOException {
        APPPrivilegeProbablity appPrivilegeProbablity = new APPPrivilegeProbablity();
        Repository repository = new Repository();
        //读取文件，生成app对象
        //读取权限名和权限的id


        //<editor-fold desc="输入">

//        // 1、285权限数据输入
//        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/perm.txt", 1);
//        // 2、App权限文件
//        appPrivilegeProbablity.readFile(repository, "数据源/测试数据源/test.txt", 2);
//        // 3、App话题文件
//        appPrivilegeProbablity.readFile(repository, "数据源/测试数据源/norml_train.txt", 3);


        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/perm.txt", 1);
        //生成app对象，设定它所拥有的权限
//        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/代码权限.txt", 2);
        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/声明权限.txt", 2);
        // 读取Topic,设定它所对应的app
        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/话题文件.txt", 3);

        //</editor-fold>


        //计算每个权限对每个Topic支持度
        HashMap<Integer, Topic> topicid_topic = repository.getTopicid_topic();
        HashMap<Integer, App> appid_app = repository.getAppid_app();
        appPrivilegeProbablity.calTopicSupportPriPro(topicid_topic, appid_app);


        //<editor-fold desc="测试目标">

        //读取测试集
        //生成测试集的app的对象，包含appid和权限列表
//
//        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/良性权限.txt", 4);
//        //
//        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/良性话题概率.txt", 5);

        if (true) {
            System.out.println("dasd");
        }

        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/恶性权限.txt", 4);
        //
        appPrivilegeProbablity.readFile(repository, "数据源/正式数据源/恶性话题概率.txt", 5);

        //</editor-fold>

        HashMap<Integer, App> appid_app_test = repository.getTest_appid_app();

        //基于上述p-topic模型，为新来的app推荐权限
        appPrivilegeProbablity.appSupportPrivilege(appid_app_test);
        appPrivilegeProbablity.writeFile("数据源/正式数据源/测试输出/声明权限-恶性App.txt", appid_app_test);
//        appPrivilegeProbablity.writeFile("数据源/正式数据源/测试输出/代码权限-恶性App.txt", appid_app_test);
    }


    //<editor-fold desc="文件操作">

    //读取文件
    //flag为1的话，读取权限名和权限的id
    //flag为2的话，读取app对象，设定它所拥有的权限对象
    //flag为3的话，读取topic对象，设定它所对应的app
    public void readFile(Repository repository, String filename, int flag) throws IOException {
        //7.读取文件
        //import java.io.*;
        // 逐行读取数据
        FileReader fr = null;
        try {
            fr = new FileReader(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String str2 = null;
        while (true) {
            try {
                str2 = br.readLine();
                if (str2 == null) {
                    break;
                }
                String[] split = str2.split("\t");
                if (flag == 1) {
                    setPriName_Priid(split, repository);
                } else if (flag == 2) {
                    setAppPri(split, repository, 0);
                } else if (flag == 3) {
                    split = Arrays.stream(str2.split(" ")).filter(i -> !("".equals(i))).toArray(String[]::new);
                    setTopicAPP(split, repository, 0);
                } else if (flag == 4) {
                    setAppPri(split, repository, 1);
                } else if (flag == 5) {
                    split = Arrays.stream(str2.split(" ")).filter(i -> !("".equals(i))).toArray(String[]::new);
                    setTopicAPP(split, repository, 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        br.close();
        fr.close();
    }


    public void setPriName_Priid(String[] str, Repository repository) {
        if (str.length == 2) {
            repository.getPri_pridid().put(str[1], Integer.parseInt(str[0]));
        } else {
            System.out.println("str的长度不够" + "str的内容为" + str);
        }
    }

    /**
     * @return void
     * @Author miaoxu
     * @Description //生成app 对象，设置权限
     * @Date 19:59 2019/1/5
     * @Param [str, repository, test] str是app的权限集合的字符串，test = 0 代表训练集，test = 1代表测试集，生成的app放入repository.getTest_appid_app()中
     **/
    public void setAppPri(String[] str, Repository repository, int test) {
        if (str.length >= 3) {
            int appid = Integer.parseInt(str[0]);
            String[] prilist = str[2].split(";");
            App app = new App(appid);
            HashMap<String, Integer> pri_id = repository.getPri_pridid();
            List<Integer> privilegeList = app.getPrivilegeList();
            //将app对应的权限转换为id
            for (int i = 0; i < prilist.length; i++) {
                try {
                    String per = prilist[i].trim();
                    if (pri_id.keySet().contains(per)) {
                        int priid = pri_id.get(per);
                        privilegeList.add(priid);
                    } else {
                        System.err.println("权限：" + per + "无法对应");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            //训练的时候test = 0，放入repository.getAppid_app()
            if (test == 0) {
                repository.getAppid_app().put(appid, app);
            } else if (test == 1) {
                repository.getTest_appid_app().put(appid, app);
            }

        } else {
            System.out.println("str的长度不够" + "str的内容为" + str);
        }
    }

    //读取Topic,设定它所对应的app
    public void setTopicAPP(String[] str, Repository repository, int test) {
        if (str.length >= 4) {
            int appid = Integer.parseInt(str[0]);

            int topicNum = (str.length - 2);

            // 将概率加入
            for (int i = 2; i <= topicNum; ) {
                int topicid = Integer.parseInt(str[i]);
                double pro = Double.parseDouble(str[i + 1]);
                HashMap<Integer, Topic> topicid_topic = repository.getTopicid_topic();
                Topic t = topicid_topic.get(topicid);

                i += 2;

                if (test == 0) {
                    if (t != null) {
                        t.getAppidPro().put(appid, pro);
                        t.getAppidlist().add(appid);
                    } else {
                        t = new Topic(topicid);
                        t.getAppidlist().add(appid);
                        t.getAppidPro().put(appid, pro);
                        repository.addTopic(t);
                    }
                }
                //测试集中的app所对应的topic的概率
                else if (test == 1) {
                    App testapp = repository.getTest_appid_app().get(appid);
                    if (testapp != null) {
                        if ((t != null)) {
                            testapp.getTopicPro().put(t, pro);
                        } else {
                            System.out.println("当前topic在训练集中不存在： " + topicid);
                        }

                    } else {
                        System.out.println("输入文件的顺序错误" + appid + " ：没有初始化");
                    }

                }

            }


        }
    }

    public void writeFile(String filename, HashMap<Integer, App> testapp) throws IOException {
        /* 第三种：BufferedWriter */
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
        for (Map.Entry<Integer, App> e : testapp.entrySet()) {
            App a = e.getValue();

            if (a.priid_pro.size() > 0) {
                bw.write(a.toString() + "\r\n");
            } else {
                String str = "{\"id\":"+e.getKey()+",\"permissions\":[]}";
                bw.write(str + "\r\n");
            }
            System.err.println("输出中。。。");
        }

        bw.flush();
        bw.close();
    }

    //</editor-fold>


    //<editor-fold desc="计算">

    //计算权限支持度
    public void calTopicSupportPriPro(HashMap<Integer, Topic> topicid_topic, HashMap<Integer, App> appid_app) {
        for (Map.Entry<Integer, Topic> entry : topicid_topic.entrySet()) {
            Topic topic = entry.getValue();
            List<Integer> appidlist = topic.getAppidlist();
            HashMap<Integer, Double> priid_pro = topic.getPriid_pro();
            //分母
            double sum = 0;
            for (Integer appid : appidlist) {
                App a = appid_app.get(appid);
                if (a != null) {
                    List<Integer> privilegeList = a.getPrivilegeList();
                    //app对应的topic的概率
//                double temp = a.getTopicPro().get(topic);
                    double temp = topic.getAppidPro().get(a.id);
                    sum += temp;
                    for (Integer priid : privilegeList) {
                        //计算pri的支持度
                        if (priid_pro.get(priid) != null) {
                            double pre = priid_pro.get(priid);
                            priid_pro.put(priid, pre + temp);
                        } else {
                            priid_pro.put(priid, temp);
                        }
                    }
                }
                //app的所有权限列表

            }
            //求每个权限的支持度
            for (Map.Entry<Integer, Double> e : priid_pro.entrySet()) {
                Integer priidkey = e.getKey();
                double supportvalue = e.getValue() / sum;
                priid_pro.put(priidkey, supportvalue);
            }

        }
    }

    public void appSupportPrivilege(HashMap<Integer, App> appid_app) {
        for (Map.Entry<Integer, App> entry : appid_app.entrySet()) {
            App a = entry.getValue();
            HashMap<Topic, Double> topicPro = a.getTopicPro();
            HashMap<Integer, Double> priid_proapp = a.getPriid_pro();
            for (Map.Entry<Topic, Double> e : topicPro.entrySet()) {
                Topic t = e.getKey();
                Double apptopicpro = e.getValue();
                //遍历topic下的所有权限
                HashMap<Integer, Double> priid_pro = t.getPriid_pro();
                for (Map.Entry<Integer, Double> e1 : priid_pro.entrySet()) {
                    int priid = e1.getKey();
                    double value = e1.getValue();
                    if (priid_proapp.get(priid) != null) {
                        double pre = priid_proapp.get(priid);
                        priid_proapp.put(priid, pre + apptopicpro * value);
                    } else {
                        priid_proapp.put(priid, apptopicpro * value);
                    }
                }
            }

        }
    }
    //</editor-fold>

    //</editor-fold>


    //<editor-fold desc="并集">

    /**
     * 求两个的并集
     */
    public static HashMap<Integer, App> getTogether(String daimaPath, String shengmingPath) throws IOException, JSONException {

        // 读取文件
        BufferedReader 代码权限 = new BufferedReader(new FileReader(daimaPath));
        BufferedReader 声明权限 = new BufferedReader(new FileReader(shengmingPath));

        // 逐行读取
        String daima = "";

        HashMap<Integer, App> testapp = new HashMap<>();
        while ((daima = 代码权限.readLine()) != null) {
            String shengming = 声明权限.readLine();
            // 代码权限读取
            JSONObject daimaJson = new JSONObject(daima);
            int id = daimaJson.getInt("id");
            App app = new App(id);
            JSONArray 代码权限json = daimaJson.getJSONArray("permissions");
            HashMap<Integer, Double> 代码权限jsonarray = jsonToArray(代码权限json);

            // 申请权限读取
            JSONObject shenqingJson = new JSONObject(shengming);
            JSONArray 声明权限json = shenqingJson.getJSONArray("permissions");
            HashMap<Integer, Double> 声明权限jsonarray = jsonToArray(声明权限json);
            int shengmingSize = 声明权限json.length();
            int daimaSize = 代码权限json.length();

            System.out.println(MessageFormat.format("app:{0},声明权限{1}个，代码权限{2}个",id, shengmingSize, daimaSize));

            if (shengmingSize >= daimaSize) {
                // 遍历声明权限
                声明权限jsonarray.forEach((perr, posi) -> {
                    if (代码权限jsonarray.containsKey(perr)) {
                        // 比较大小
                        Double daimPosi = 代码权限jsonarray.get(perr);
                        app.addPriPro(perr, posi > daimPosi ? posi : daimPosi);
                    } else {
                        // 直接加入
                        app.addPriPro(perr, posi);
                    }
                });
            } else {
                // 遍历代码权限
                代码权限jsonarray.forEach((perr, posi) -> {
                    if (声明权限jsonarray.containsKey(perr)) {
                        // 比较大小
                        Double shengMing = 声明权限jsonarray.get(perr);
                        app.addPriPro(perr, posi > shengMing ? posi : shengMing);
                    } else {
                        // 直接加入
                        app.addPriPro(perr, posi);
                    }
                });
            }
            // 并集
            testapp.put(id, app);
        }
        return testapp;
    }

    public static HashMap<Integer, Double> jsonToArray(JSONArray jsonArray) throws JSONException {
        HashMap<Integer, Double> map = new HashMap<>();
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonStr = jsonArray.get(i).toString();
                String replaceStr = jsonStr.replaceAll("\\[", "").replaceAll("]", "");
                String[] split = replaceStr.split(",");
                int permissionId = Integer.parseInt(split[0]);
                double possibility = Double.parseDouble(split[1]);
                map.put(permissionId, possibility);
            }
        }
        return map;
    }

    //</editor-fold>

}
