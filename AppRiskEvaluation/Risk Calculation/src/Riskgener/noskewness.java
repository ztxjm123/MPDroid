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
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tool.JDBCUtilSingle;
import tool.test;

public class noskewness {
	static double shold = 0;
	//对测试集file的结果判定  
		static String g_pref = "0517/";
		public void start(double threshold) throws IOException, JSONException, SQLException {
			// TODO Auto-generated method stub
			shold = threshold;
			Connection conn = JDBCUtilSingle.getInitJDBCUtil().getConnection();
			BufferedReader br2=null;
			int id;
			String context2 = "";
			String testfile2 = g_pref+"pure_onestar_re0.4";
			//不被期待的权限
			ArrayList<Integer> unexpectList;
			ArrayList<Integer> real_List;
			JSONObject jsonObject2;
			JSONArray g_permissions	;
			
			int per0 = 0;//没有权限的app
			int riskapp = 0;
			int sum = 0;
			try {
				br2 = new BufferedReader(new InputStreamReader(new FileInputStream(testfile2+".txt"),"UTF-8"));
				//
				//int count=1;
				int zero_recommend=0;
				while ((context2 = br2.readLine()) != null) {
					/*
					if(count!=35){
					count++;
					continue;
					}
					*/
					jsonObject2 = new JSONObject(context2);
					g_permissions = jsonObject2.getJSONArray("permissions");
					if(g_permissions.length()==0){
						zero_recommend++;
					}
					id = jsonObject2.getInt("id");
					real_List = getPers(id,conn);
					//没有权限是无风险的,直接跳过
					if(real_List.size()!=0) {	
						//判断是否有风险
						unexpectList = assess(g_permissions,real_List);
						//有风险的app计数，并判断差集击中
						if(unexpectList.size()>0) {
							riskapp++;			
						}	
						sum++;
					}else{
						per0++;
					}
					//
					//break;
				}
				//System.out.println(quotient((float)riskapp,(float)sum)+"\t"+"per0:"+per0);
				System.out.println(quotient((float)riskapp,(float)sum)+"\t"+riskapp+"\t"+zero_recommend+"\t"+sum);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(br2!=null){
					br2.close();
				}
			}
		}
		private static ArrayList<Integer> assess(JSONArray permissions, ArrayList<Integer> real_List) throws JSONException {
			// TODO Auto-generated method stub
				ArrayList<Integer> unexpectList = new ArrayList<Integer>();
				ArrayList<Integer> n_rList = new ArrayList<Integer>(); //满足阈值要求的权限
				if(permissions.toString().equals("[]"))
				{
					//					
					return unexpectList;
					//return real_List;
				}else{
					//大于阈值的推荐permissions
					n_rList = get(permissions);
					for(int i = 0;i<real_List.size();i++){
						//真实权限的index是否在推荐权限里有出现;若没有出现，则score+1
						if(n_rList.indexOf(real_List.get(i)) == -1){
							unexpectList.add(real_List.get(i));
						}			
					}		
				}
				return unexpectList;
			}
		static ArrayList<Integer> getPers(int id,Connection conn) throws SQLException {
			// TODO Auto-generated method stub
			ArrayList<Integer> real_List = new ArrayList<Integer>();
			String sql = "select realper from normal_train_real_pers where id = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			ResultSet rss = pstmt.executeQuery();
			if(rss.next()){
				if(rss.getString(1).equals("[]")){
					return real_List;
				}
				String[] str = rss.getString(1).replace("[", "").replace("]", "").split(", ");
				for(int i = 0;i<str.length;i++){
					real_List.add(Integer.parseInt(str[i]));
				}
			}
			return real_List;
		}
		static String quotient(float a, float f) {
			// TODO Auto-generated method stub
			if(f == 0.0f){
				return "0.000";
			}
			float result1 = a/f;
	        String result = String.format("%.3f",result1);
			return result;
		}		
		//根据JSONArray获得ArrayList类型的推荐权限列表
		private static ArrayList<Integer> get(JSONArray permissions) throws JSONException {
			// TODO Auto-generated method stub
			ArrayList<Integer> reasonable_id = new ArrayList<Integer>();
			
			
			
			
			reasonable_id.add(290);
			
			
			
			String item = "";
			if(!permissions.toString().equals("[]"))
			{
				String[] str =  new String[2];
				for(int i=0;i<permissions.length();i++){						
					item = permissions.getJSONArray(i).toString().replace("[", "").replace("]", "");				
					str = item.split(",");
					if(Double.parseDouble(str[1])>shold){
						reasonable_id.add(Integer.parseInt(str[0]));
					}
				}
			}
			return reasonable_id;
		}
}
