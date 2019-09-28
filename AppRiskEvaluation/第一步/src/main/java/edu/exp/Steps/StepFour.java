package edu.exp.Steps;

import edu.exp.DB.JDBCUtilSingle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StepFour {
    static double shold = 0;
    //对测试集file的结果判定

    public static void runResult(String file, String s_threshold) throws IOException, JSONException, SQLException {
        // TODO Auto-generated method stub
        Connection conn = JDBCUtilSingle.getInitJDBCUtil().getConnection();
        BufferedReader br1 = null;
        BufferedReader br2 = null;
        BufferedReader br3 = null;
        int id;
        String context1 = "";
        String context2 = "";
        String context3 = "";
        //String testfile1 = m_pref+"0.4"+"/"+"test_malicous"+"0.4"+"_noskew_m";
        //String testfile2 = g_pref+"0.6"+"/"+"test_malicous"+"0.6"+"_noskew";
        //String testfile1 = "MC_"+"malicious"+"_recommend_m";
        //String testfile2 = "MC_"+"goodapp"+"_recommend_m";
        //String testfile3 = "MC_"+"goodapp"+"_recommend_m";

        //String testfile1 = g_pref+s_threshold+"/"+"test_"+file+s_threshold+"_noskew";
        //String testfile2 = g_pref+s_threshold+"/"+"test_"+file+s_threshold+"_noskew";
        //String testfile3 = g_pref+s_threshold+"/"+"test_"+file+s_threshold+"_noskew";


        String testfile1 = "source-file/realper";
        String testfile2 = testfile1;//"diff/skew/" + "test_" + file + s_threshold + "_skew";
        String testfile3 = testfile1;//"diff/skew/" + "test_" + file + s_threshold + "_skew";
        //m和g的差集
        ArrayList<Integer> subtraction = new ArrayList<Integer>();
        //m_g的恶意差集和real的交集
        ArrayList<Integer> interaction;
        //风险app中m_g的恶意差集和real的交集
        ArrayList<Integer> inter2 = new ArrayList<Integer>();
        //不被期待的权限
        ArrayList<Integer> unexpectList;
        ArrayList<Integer> real_List;
        JSONObject jsonObject1;
        List<String> m_permissions;
        JSONObject jsonObject2 = null;
        List<String> g_permissions = null;
        JSONObject jsonObject3 = null;
        List<String> cb_permissions;

        int riskapp = 0;//风险app
        int chaapp = 0;//恶意app
        int zeroapp = 0;
        int sum = 0;
        try {
            br1 = new BufferedReader(new InputStreamReader(new FileInputStream(testfile1 + ".txt"), "UTF-8"));
            br2 = new BufferedReader(new InputStreamReader(new FileInputStream(testfile2 + ".txt"), "UTF-8"));
            br3 = new BufferedReader(new InputStreamReader(new FileInputStream(testfile3 + ".txt"), "UTF-8"));
            while ((context1 = br1.readLine()) != null) {
                subtraction.clear();
                jsonObject1 = new JSONObject(context1);

                Object realper = jsonObject1.get("realper");

                m_permissions = Arrays.asList(realper.toString().replace("[", "").replace("]","").split(","));//jsonObject1.getJSONArray("realper");

                id = jsonObject1.getInt("id");
                real_List = getPers(id, conn);

                context2 = br2.readLine();
                jsonObject2 = new JSONObject(context2);
                g_permissions = m_permissions;

                context3 = br3.readLine();
                jsonObject3 = new JSONObject(context3);
                cb_permissions = m_permissions;

                //得到m和g的差集，作为判定指标依据
                subtraction = sub(m_permissions, cb_permissions);

                //没有权限是无风险的,直接跳过
                if (real_List.size() != 0) {
                    //识别风险app
                    //判断是否有风险
                    unexpectList = assess(cb_permissions, real_List);
                    //有风险的app计数，并判断差集击中
                    if (unexpectList.size() > 0) {
                        riskapp++;//风险
                        //识别恶意app
                        //真实权限和差集有交，和，不期待权限和差集有交，等价的
                        if (subtraction.size() > 0) {
                            zeroapp++;
                            interaction = inter(subtraction, unexpectList);
                            if (interaction.size() > 0) {
                                chaapp++;//恶意
                            }
                        }
                    }
                    //有权限app的总数
                    sum++;
                }
                //System.out.println(id+"\t"+riskapp+"\t"+chaapp+"\t"+unexpectList.size()+"\t"+intersection.size());
            }
            //System.out.println(riskapp+"\t"+sum+"\t"+chaapp);
            //风险app百分比，恶意app百分比，风险app中恶意app的覆盖率
            System.out.println(quotient((float) riskapp, (float) sum) + "\t" + quotient((float) chaapp, (float) zeroapp));
            //System.out.println(quotient((float)chaapp,(float)zeroapp));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br1 != null) {
                br1.close();
            }
        }
    }

    private static ArrayList<Integer> assess(List<String> permissions, ArrayList<Integer> real_List) throws JSONException {
        // TODO Auto-generated method stub
        ArrayList<Integer> unexpectList = new ArrayList<Integer>();
        List<String> n_rList = new ArrayList<String>(); //满足阈值要求的权限
        if (permissions.toString().equals("[]")) {
            return unexpectList;
        } else {
            //大于阈值的推荐permissions
            n_rList = permissions;
            for (int i = 0; i < real_List.size(); i++) {
                //真实权限的index是否在推荐权限里有出现;若没有出现，则score+1
                if (n_rList.indexOf(real_List.get(i)) == -1) {
                    unexpectList.add(real_List.get(i));
                }
            }
        }
        return unexpectList;
    }

    static ArrayList<Integer> getPers(int id, Connection conn) throws SQLException {
        // TODO Auto-generated method stub
        ArrayList<Integer> real_List = new ArrayList<Integer>();

        // normal_train_real_pers 是什么表？
        String sql = "select realper from normal_train_real_pers where id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rss = pstmt.executeQuery();
        if (rss.next()) {
            if (rss.getString(1).equals("[]")) {
                return real_List;
            }
            String[] str = rss.getString(1).replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < str.length; i++) {
                real_List.add(Integer.parseInt(str[i]));
            }
        }
        return real_List;
    }

    static String quotient(float a, float f) {
        // TODO Auto-generated method stub
        if (f == 0.0f) {
            return "0.000";
        }
        float result1 = a / f;
        String result = String.format("%.3f", result1);
        return result;
    }

    private static ArrayList<Integer> sub(List<String> m_rList,
                                          List<String> g_rList) {
        // TODO Auto-generated method stub
        ArrayList<Integer> recommend_cha = new ArrayList<Integer>();
        for (int j = 0; j < m_rList.size(); j++) {
            if (g_rList.indexOf(m_rList.get(j)) == -1) {
                recommend_cha.add(Integer.parseInt(m_rList.get(j)));
            }
        }
        return recommend_cha;
    }

    private static ArrayList<Integer> inter(ArrayList<Integer> m_rList,
                                            ArrayList<Integer> g_rList) {
        // TODO Auto-generated method stub
        ArrayList<Integer> interaction = new ArrayList<Integer>();
        for (int j = 0; j < m_rList.size(); j++) {
            if (g_rList.indexOf(m_rList.get(j)) != -1) {
                interaction.add(m_rList.get(j));
            }
        }
        return interaction;
    }

    //根据JSONArray获得ArrayList类型的推荐权限列表
    private static ArrayList<Integer> get(JSONArray permissions) throws JSONException {
        // TODO Auto-generated method stub
        ArrayList<String> per_num = new ArrayList<String>();
        ArrayList<Integer> perList = new ArrayList<Integer>();
        String item = "";
        if (!permissions.toString().equals("[]")) {
            for (int i = 0; i < permissions.length(); i++) {
                item = permissions.getJSONArray(i).toString().replace("[", "").replace("]", "");
                per_num.add(item);
            }
            perList = test(per_num);
        }
        return perList;
    }

    private static ArrayList<Integer> test(ArrayList<String> data) {
        // TODO Auto-generated method stub
        String[] str = new String[2];
        ArrayList<Integer> per_id = new ArrayList<Integer>();
        ArrayList<Double> per_value = new ArrayList<Double>();

        ArrayList<Integer> reasonable_id = new ArrayList<Integer>();

        for (int i = 0; i < data.size(); i++) {
            str = data.get(i).split(",");
            //保证id和value对应
            per_id.add(Integer.parseInt(str[0]));
            per_value.add(Double.parseDouble(str[1]));
        }
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (int i = 0; i < per_value.size(); i++) {
            //由于接近0的值较多，需要把小于0.05的先去掉
            if (per_value.get(i) > 0.1) {
                ds.addValue(per_value.get(i));
            }
        }
        double[] sortvalues = ds.getSortedValues();
        ds.clear();
        //升序载入
        for (int i = 0; i < sortvalues.length; i++) {
            ds.addValue(sortvalues[i]);
            //System.out.print(sortvalues[i]+"\t");
        }
        //System.out.println();
        while (ds.getSkewness() > 0) {
            //最大值取出来,作为异常大值，即推荐权限
            if (per_value.indexOf(ds.getMax()) != -1) {
                reasonable_id.add(per_id.get(per_value.indexOf(ds.getMax())));
            }
            //将原来的最大值替换为平均值
            ds.removeMostRecentValue();
            ds.addValue(ds.getMean());
            //升序排列
            double[] sort = ds.getSortedValues();
            ds.clear();
            for (int i = 0; i < sort.length; i++) {
                ds.addValue(sort[i]);
                //System.out.print(sort[i]+"\t");
            }
            //System.out.println();
        }
        return reasonable_id;
    }
}
