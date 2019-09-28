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

    //�Բ��Լ�file�Ľ���ж�
    static double threshold = (float) 0;
    static String s = "";
    static double t = 0;
    static ArrayList<String> perlist = null;

    public static void main(String[] args) throws JSONException, SQLException, IOException {

        /** ���ò��Լ��ı���,Ĭ�����֮һ */
//        String size = "";
//        String size = "ʮ��֮һ";
//        String size = "ʮ��֮��";


        String size = "15-85";

        /** ���û�������� */
//        String topic = "60";
//        String topic = "80";
//        String topic = "100";
//        String topic = "75";
        String topic = "85";

        /** ���ù�����ֵ */
        String limit = "0.01";
//        String limit = "0.05";
//        String limit = "0.1";
//        String limit = "0.2";
//        String limit = "0.3";
//        String limit = "0.4";

        /** ���Բ��Լ������Ƕ��Բ��Լ� */
//        String target = "��";
        String target = "��";

        /** �Ƿ���SF��� */
//        boolean isSf = true;
        boolean isSf = false;

        new map().test(0.05, topic, limit, isSf, target, size);
    }


    public void test(double shold, String topic, String limit, boolean isSf, String target, String size) throws IOException, JSONException, SQLException {
        // TODO Auto-generated method stub
        perlist = getPERlist(); // Ȩ�޼���list
        threshold = shold;
        Connection conn = JDBCUtilSingle.getInitJDBCUtil().getConnection();
        BufferedReader br1 = null;
        BufferedReader br2;
        int id;
        String context1;
        String str = "2";
        s = "test";
        t = 0.03;

        String testfile1 = MessageFormat.format("����Դ/{0}����{4}���/{0}�����������{1}����{2}���/{3}��Ŀ������Ƽ�.txt",
                topic,
                limit,
                isSf ? "sf" : "minps",
                target,
                size);
        String testfile2 = MessageFormat.format("����Դ/{0}����{4}���/{0}�����������{1}����{2}���/{3}��Ŀ�������Ƽ�.txt",
                topic,
                limit,
                isSf ? "sf" : "minps",
                target,
                size);


//		String testfile1 = "����Դ/g�����/100����/����֮ǰ/����Ŀ������Ƽ�.txt";
//		String testfile2 = "����Դ/g�����/100����/����֮ǰ/����Ŀ�������Ƽ�.txt";
//		String testfile1 = "����Դ/g�����/100����/����֮ǰ/����Ŀ������Ƽ�.txt";
//		String testfile2 = "����Դ/g�����/100����/����֮ǰ/����Ŀ�������Ƽ�.txt";

//		String testfile1 = "����Դ/g�����/100����/����֮��/����Ŀ������Ƽ�.txt";
//		String testfile2 = "����Դ/g�����/100����/����֮��/����Ŀ�������Ƽ�.txt";
//        String testfile1 = "����Դ/g�����/100����/����֮��/����Ŀ������Ƽ�.txt";
//        String testfile2 = "����Դ/g�����/100����/����֮��/����Ŀ�������Ƽ�.txt";


        //m��g�Ĳ
        ArrayList<Integer> subtraction = new ArrayList<>();
        //����app��m_g�Ķ�����real�Ľ���
        ArrayList<Integer> interaction = new ArrayList<>();
        //�����ڴ���Ȩ��
        ArrayList<Integer> unexpectList;
        ArrayList<Integer> real_List;
        JSONObject jsonObject1;
        JSONArray m_permissions;
        JSONObject jsonObject2;
        JSONArray g_permissions;
        int riskapp = 0;//����app
        int chaapp = 0;//����app
        int zeroapp = 0;
        int sum = 0;
        int unexpectper = 0;
        int subper = 0;
        int riskval = 0;
        int appscore = 0;
        //ʵ��Ȩ����
        int realp = 0;
        //�Ƽ�Ȩ����
        int recom = 0;
        //MAP
        float map = 0;
        //ȥ��risk
        //float map2 = 0;
        //ȥ��unexpected
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
                //�õ�m��g�Ĳ����Ϊ�ж�ָ������
                subtraction = sub(get2(m_permissions), get2(g_permissions));
                //û��Ȩ�����޷��յ�,ֱ������
                if (real_List.size() != 0) {
                    realp = realp + real_List.size();

                    ArrayList<Integer> n_rList = get2(g_permissions);
                    map = map + getAP(n_rList, real_List);

                    recom = recom + get2(g_permissions).size();
                    //ʶ�����app
                    //�ж��Ƿ��з���
                    unexpectList = assess(g_permissions, real_List);

                    //map3 = map3 +getAP(get2(g_permissions),sub(real_List,unexpectList));

                    //�з��յ�app���������жϲ����
                    if (unexpectList.size() > 0) {
                        unexpectper = unexpectper + unexpectList.size();
                        riskapp++;//����
                        //ʶ�����app
                        //��ʵȨ�޺Ͳ�н����ͣ����ڴ�Ȩ�޺Ͳ�н����ȼ۵�
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
                                chaapp++;//����
                            }
                        }
                    }
                    //map2 = map2 +getAP(get2(g_permissions),sub(real_List,interaction));
                    //��Ȩ��app������
                    sum++;
                }
            }
            String unexpectedPermissionApp = quotient((float) riskapp, (float) sum);
            String riskPermissionApp = quotient((float) chaapp, (float) sum);
            String riskValue = quotient((float) riskval, (float) sum);
            String mapValue = quotient(map, (float) sum);
            System.out.println(unexpectedPermissionApp + "\t" + riskPermissionApp + "\t" + riskValue + "\t" + mapValue + "\t" + quotient((float) realp, (float) sum) + "\t" + quotient((float) recom, (float) sum) + "\t" + quotient((float) unexpectper, (float) sum));
            System.out.println("unexpected permission app ����: " + unexpectedPermissionApp);
            System.out.println("Risk Permission app ����: " + riskPermissionApp);
            System.out.println("Risk Value: " + riskValue);
            System.out.println("MAP: " + mapValue);
            //System.out.println("ȥ��riskMAP2: "+quotient(map2,(float)sum));
            //System.out.println("ȥ��unexpectedMAP3: "+quotient(map3,(float)sum));
            System.out.println("ʵ��Ȩ����: " + quotient((float) realp, (float) sum));
            System.out.println("�Ƽ�Ȩ����: " + quotient((float) recom, (float) sum));
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
                case "ʮ��֮һ" :rate= "10%";break;
                case "ʮ��֮��" :rate= "30%";break;

            }

            System.out.println(MessageFormat.format("{0}����{1}֧�ֶȹ��˵� {2}��{4}Ŀ��{3}������",
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
        ArrayList<Integer> n_rList = new ArrayList<Integer>(); //������ֵҪ���Ȩ��
        if (permissions.toString().equals("[]")) {
            return unexpectList;
        } else {
            n_rList = get2(permissions);
            for (int i = 0; i < real_List.size(); i++) {
                //��ʵȨ�޵�index�Ƿ����Ƽ�Ȩ�����г���;��û�г��֣���score+1
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
            System.err.println("��ǰapp��" + id);
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

    //����JSONArray���ArrayList���͵��Ƽ�Ȩ���б�
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
            // ƫ�ȼ����Ƽ�Ȩ��
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
            //��֤id��value��Ӧ
            per_id.add(Integer.parseInt(str[0]));
            per_value.add(Double.parseDouble(str[1]));
        }
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (int i = 0; i < per_value.size(); i++) {
            //���ڽӽ�0��ֵ�϶࣬��Ҫ��С��0.1����ȥ��


            if (per_value.get(i) > t) {
                ds.addValue(per_value.get(i));
            }
        }
        double[] sortvalues = ds.getSortedValues();
        ds.clear();
        //��������
        for (int i = 0; i < sortvalues.length; i++) {
            ds.addValue(sortvalues[i]);
            //System.out.print(sortvalues[i]+"\t");
        }
        //System.out.println();
        while (ds.getSkewness() > 0) {
            //���ֵȡ����,��Ϊ�쳣��ֵ�����Ƽ�Ȩ��
            if (per_value.indexOf(ds.getMax()) != -1) {
                reasonable_id.add(per_id.get(per_value.indexOf(ds.getMax())));
            }
            //��ԭ�������ֵ�滻Ϊƽ��ֵ
            ds.removeMostRecentValue();
            ds.addValue(ds.getMean());
            //��������
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
