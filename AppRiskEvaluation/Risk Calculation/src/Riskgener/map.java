package Riskgener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tool.JDBCUtilSingle;
import tool.Util;

public class map {

    //对测试集file的结果判定
    static double threshold = (float) 0;
    static String s = "";
    static double t = 0;
    static ArrayList<String> perlist = null;

    public static void main(String[] args) throws JSONException, SQLException, IOException {

        /** 设置测试集的比例,默认五分之一 */
//        String size = "";
//        String size = "十分之一";
//        String size = "十分之三";


        String size = "15-85";

        /** 设置话题的数量 */
//        String topic = "60";
//        String topic = "80";
//        String topic = "100";
//        String topic = "75";
        String topic = "85";

        /** 设置过滤阈值 */
        String limit = "0.01";
//        String limit = "0.05";
//        String limit = "0.1";
//        String limit = "0.2";
//        String limit = "0.3";
//        String limit = "0.4";

        /** 良性测试集或者是恶性测试集 */
//        String target = "恶";
        String target = "良";

        /** 是否是SF输出 */
//        boolean isSf = true;
        boolean isSf = false;

        new map().test(0.05, topic, limit, isSf, target, size);
    }


    public void test(double shold, String topic, String limit, boolean isSf, String target, String size) throws IOException, JSONException, SQLException {
        // TODO Auto-generated method stub
        perlist = getPERlist(); // 权限集合list
        threshold = shold;
        Connection conn = JDBCUtilSingle.getInitJDBCUtil().getConnection();
        BufferedReader br1 = null;
        BufferedReader br2;
        int id;
        String context1;
        String str = "2";
        s = "test";
        t = 0.03;

        String testfile1 = MessageFormat.format("数据源/{0}话题{4}输出/{0}话题数据输出{1}过滤{2}输出/{3}性目标恶性推荐.txt",
                topic,
                limit,
                isSf ? "sf" : "minps",
                target,
                size);
        String testfile2 = MessageFormat.format("数据源/{0}话题{4}输出/{0}话题数据输出{1}过滤{2}输出/{3}性目标良性推荐.txt",
                topic,
                limit,
                isSf ? "sf" : "minps",
                target,
                size);


//		String testfile1 = "数据源/g号输出/100话题/过滤之前/恶性目标恶性推荐.txt";
//		String testfile2 = "数据源/g号输出/100话题/过滤之前/恶性目标良性推荐.txt";
//		String testfile1 = "数据源/g号输出/100话题/过滤之前/良性目标恶性推荐.txt";
//		String testfile2 = "数据源/g号输出/100话题/过滤之前/良性目标良性推荐.txt";

//		String testfile1 = "数据源/g号输出/100话题/过滤之后/恶性目标恶性推荐.txt";
//		String testfile2 = "数据源/g号输出/100话题/过滤之后/恶性目标良性推荐.txt";
//        String testfile1 = "数据源/g号输出/100话题/过滤之后/良性目标恶性推荐.txt";
//        String testfile2 = "数据源/g号输出/100话题/过滤之后/良性目标良性推荐.txt";


        //m和g的差集
        ArrayList<Integer> subtraction = new ArrayList<>();
        //风险app中m_g的恶意差集和real的交集
        ArrayList<Integer> interaction = new ArrayList<>();
        //不被期待的权限
        ArrayList<Integer> unexpectList;
        ArrayList<Integer> real_List;
        JSONObject jsonObject1;
        JSONArray m_permissions;
        JSONObject jsonObject2;
        JSONArray g_permissions;
        int riskapp = 0;//风险app
        int chaapp = 0;//恶意app
        int zeroapp = 0;
        int sum = 0;
        int unexpectper = 0;
        int subper = 0;
        int riskval = 0;
        int appscore = 0;
        //实际权限数
        int realp = 0;
        //推荐权限数
        int recom = 0;
        //MAP
        float map = 0;
        //去掉risk
        //float map2 = 0;
        //去掉unexpected
        //float map3 = 0;
        String per_name = "";
        try {
            br1 = new BufferedReader(new InputStreamReader(new FileInputStream(testfile1), "UTF-8"));
            br2 = new BufferedReader(new InputStreamReader(new FileInputStream(testfile2), "UTF-8"));
            String context2;
            while ((context1 = br1.readLine()) != null) {
                appscore = 0;
                subtraction.clear();
                interaction.clear();
                jsonObject1 = new JSONObject(context1);
                m_permissions = jsonObject1.getJSONArray("permissions");

                id = jsonObject1.getInt("id");
                real_List = getPers(id, conn);

                context2 = br2.readLine();
                jsonObject2 = new JSONObject(context2);
                g_permissions = jsonObject2.getJSONArray("permissions");
                //得到m和g的差集，作为判定指标依据
                subtraction = sub(get2(m_permissions), get2(g_permissions));
                //没有权限是无风险的,直接跳过
                if (real_List.size() != 0) {
                    realp = realp + real_List.size();

                    ArrayList<Integer> n_rList = get2(g_permissions);
                    map = map + getAP(n_rList, real_List);

                    recom = recom + get2(g_permissions).size();
                    //识别风险app
                    //判断是否有风险
                    unexpectList = assess(g_permissions, real_List);

                    //map3 = map3 +getAP(get2(g_permissions),sub(real_List,unexpectList));

                    //有风险的app计数，并判断差集击中
                    if (unexpectList.size() > 0) {
                        unexpectper = unexpectper + unexpectList.size();
                        riskapp++;//风险
                        //识别恶意app
                        //真实权限和差集有交，和，不期待权限和差集有交，等价的
                        if (subtraction.size() > 0) {
                            zeroapp++;
                            interaction = inter(subtraction, unexpectList);
                            if (interaction.size() > 0) {
                                for (int j = 0; j < interaction.size(); j++) {
                                    per_name = perlist.get(interaction.get(j));
                                    appscore = appscore + getProtectLevel(per_name, conn);
                                }
                                riskval = riskval + appscore;
                                subper = subper + interaction.size();
                                chaapp++;//恶意
                            }
                        }
                    }
                    //map2 = map2 +getAP(get2(g_permissions),sub(real_List,interaction));
                    //有权限app的总数
                    sum++;
                }
            }
            String unexpectedPermissionApp = quotient((float) riskapp, (float) sum);
            String riskPermissionApp = quotient((float) chaapp, (float) sum);
            String riskValue = quotient((float) riskval, (float) sum);
            String mapValue = quotient(map, (float) sum);
            System.out.println(unexpectedPermissionApp + "\t" + riskPermissionApp + "\t" + riskValue + "\t" + mapValue + "\t" + quotient((float) realp, (float) sum) + "\t" + quotient((float) recom, (float) sum) + "\t" + quotient((float) unexpectper, (float) sum));
            System.out.println("unexpected permission app 比例: " + unexpectedPermissionApp);
            System.out.println("Risk Permission app 比例: " + riskPermissionApp);
            System.out.println("Risk Value: " + riskValue);
            System.out.println("MAP: " + mapValue);
            //System.out.println("去掉riskMAP2: "+quotient(map2,(float)sum));
            //System.out.println("去掉unexpectedMAP3: "+quotient(map3,(float)sum));
            System.out.println("实际权限数: " + quotient((float) realp, (float) sum));
            System.out.println("推荐权限数: " + quotient((float) recom, (float) sum));
            System.out.println("unexpected permission: " + quotient((float) unexpectper, (float) sum));
            System.out.println();
            System.out.println("Risk Permission: " + quotient((float) subper, (float) sum));
            System.out.println("APR " + unexpectedPermissionApp);
            System.out.println("HPR: " + quotient((float) chaapp, (float) zeroapp));

            System.out.println();

            String rate = "";

            switch (size) {
                default:
                case "":rate = "20%";break;
                case "十分之一" :rate= "10%";break;
                case "十分之三" :rate= "30%";break;

            }

            System.out.println(MessageFormat.format("{0}话题{1}支持度过滤的 {2}性{4}目标{3}结果输出",
                    topic,
                    limit,
                    target,
                    isSf?" SF " : " minp ",
                    rate));

            System.out.println(" aupr     rar      arisk      map");
            System.out.println(MessageFormat.format("{0}    {1}     {2}     {3}", unexpectedPermissionApp, riskPermissionApp, riskValue, mapValue));
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

    private static float getAP(ArrayList<Integer> n_rList,
                               ArrayList<Integer> real_List) {
        // TODO Auto-generated method stub
        ArrayList<Integer> recommend_cha = new ArrayList<Integer>();
        ArrayList<Integer> real_cha = new ArrayList<Integer>();
        int hit = 0;
        int p = 0;
        int r = 0;
        float AP = 0;
        float precision = 0;
        float recall = 0;
        float fmeasure = 0;
        float result = 0;
        for (int i = 0; i < n_rList.size(); i++) {
            if (real_List.indexOf(n_rList.get(i)) == -1) {
                hit = 0;
                recommend_cha.add(n_rList.get(i));
            } else {
                hit = 1;
                p = p + 1;
            }
            AP = AP + hit * Float.parseFloat(quotient(p, i + 1));
        }

        r = p;
        precision = Float.parseFloat(quotient(r, n_rList.size()));
        recall = Float.parseFloat(quotient(r, real_List.size()));
        fmeasure = Float.parseFloat(quotient(2 * precision * recall, precision + recall));
        AP = Float.parseFloat(quotient(AP, r));

        for (int j = 0; j < real_List.size(); j++) {
            if (n_rList.indexOf(real_List.get(j)) == -1) {
                real_cha.add(real_List.get(j));
            }
        }
        String recommend = "";
        String real = "";
        for (int m = 0; m < recommend_cha.size(); m++) {
            recommend = recommend + recommend_cha.get(m) + ",";
        }
        for (int m = 0; m < real_cha.size(); m++) {
            real = real + real_cha.get(m) + ",";
        }
        //result = precision+"\t"+recall+"\t"+fmeasure+"\t"+AP+"\t"+recommend+"\t"+real+"\r\n";
        result = AP;
        return result;
    }

    private static ArrayList<String> getPERlist() throws IOException {
        // TODO Auto-generated method stub
        ArrayList<String> perlist = new ArrayList<String>();

        Util util = new Util();
        String JsonContext = util.ReadFile("0517/permissions.json");
        JsonContext = JsonContext.replace("[", "");
        JsonContext = JsonContext.replace("]", "");
        JsonContext = JsonContext.replace("\"", "");
        String[] instancestr = JsonContext.split(", ");
        for (int i = 0; i < instancestr.length; i++) {
            perlist.add(instancestr[i].toLowerCase().trim());
            //if(i==140){
            //	System.out.println(perlist.get(i));
            //}
        }
        return perlist;
    }

    static int getProtectLevel(String per_name, Connection conn) throws SQLException {
        // TODO Auto-generated method stub
        String protectLevel = "";
        int perscore = 0;
        String sql = "select protectionLevel from perm where per_name = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, per_name.trim());
        ResultSet rss = pstmt.executeQuery();
        if (rss.next()) {
            protectLevel = rss.getString(1);
            if (protectLevel.equals("normal")) {
                perscore = 1;
            } else if (protectLevel.equals("dangerous")) {
                perscore = 2;
            } else if (protectLevel.equals("signature")) {
                perscore = 3;
            } else if (protectLevel.equals("signatureOrSystem")) {
                perscore = 4;
            }
        }
        return perscore;
    }

    private static ArrayList<Integer> assess(JSONArray permissions, ArrayList<Integer> real_List) throws JSONException {
        // TODO Auto-generated method stub
        ArrayList<Integer> unexpectList = new ArrayList<Integer>();
        ArrayList<Integer> n_rList = new ArrayList<Integer>(); //满足阈值要求的权限
        if (permissions.toString().equals("[]")) {
            return unexpectList;
        } else {
            n_rList = get2(permissions);
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
        String sql = "select realper from " + s + "_real_pers where id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rss = pstmt.executeQuery();
        if (rss.next()) {
            if (rss.getString(1).equals("[]")) {
                return real_List;
            }
            String[] str = rss.getString(1).replace("[", "").replace("]", "").split(",");
            System.err.println("当前app：" + id);
            for (int i = 0; i < str.length; i++) {
                int e = Integer.parseInt(str[i].trim());
//                real_List.add(e - 1);
                real_List.add(e);
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

    private static ArrayList<Integer> sub(ArrayList<Integer> m_rList,
                                          ArrayList<Integer> g_rList) {
        // TODO Auto-generated method stub
        ArrayList<Integer> recommend_cha = new ArrayList<Integer>();
        for (int j = 0; j < m_rList.size(); j++) {
            if (g_rList.indexOf(m_rList.get(j)) == -1) {
                recommend_cha.add(m_rList.get(j));
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
	/*
	private static ArrayList<Integer> get(JSONArray permissions) throws JSONException {
		// TODO Auto-generated method stub
		ArrayList<Integer> perList = new ArrayList<Integer>();
		String item = "";
		if(!permissions.toString().equals("[]"))
		{
			String[] str =  new String[2];
			for(int i=0;i<permissions.length();i++){						
				item = permissions.getJSONArray(i).toString().replace("[", "").replace("]", "");				
				str = item.split(",");
				if(Double.parseDouble(str[1])>threshold){
					perList.add(Integer.parseInt(str[0]));
				}
			}
		}
		return perList;
	}
	*/
    private static ArrayList<Integer> get2(JSONArray permissions) throws JSONException {
        // TODO Auto-generated method stub
        ArrayList<String> per_num = new ArrayList<String>();
        ArrayList<Integer> perList = new ArrayList<Integer>();
        String item = "";
        if (!permissions.toString().equals("[]")) {
            for (int i = 0; i < permissions.length(); i++) {
                item = permissions.getJSONArray(i).toString().replace("[", "").replace("]", "");
                per_num.add(item);
            }
            // 偏度计算推荐权限
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
            //由于接近0的值较多，需要把小于0.1的先去掉


            if (per_value.get(i) > t) {
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
