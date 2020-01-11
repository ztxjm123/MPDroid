package Riskgener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import tool.JDBCUtilSingle;

/**
 *
 */
public class CBFre {
	static int appsum = 1;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = JDBCUtilSingle.getInitJDBCUtil().getConnection();
		String fretablename = "malicious2_3_cb_recommend_frequency";

		// 所有的类别
		ArrayList<String> category_list = new ArrayList<>();

		// 查询类别？？？
		String sql = "select distinct(category) from malicious_cb_recommend_frequency";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			category_list.add(rs.getString(1));
		}
    	ArrayList<Integer> per_num;
		for(int i=0;i<category_list.size();i++){
			per_num = getAppFrequency(category_list.get(i),conn);
			insert(per_num,fretablename,category_list.get(i),conn,i);
		}			
	}

	/**
	 * 插入
	 * @param per_num 权限编号
	 * @param fretablename
	 * @param category 类别
	 * @param conn
	 * @param j
	 * @throws SQLException
	 */
	private static void insert(ArrayList<Integer> per_num, String fretablename,
			String category, Connection conn, int j) throws SQLException {
		// TODO Auto-generated method stub
		String sql = "";
		String rato = "";
		System.out.print(category+"\t");
		for(int i = 0;i < per_num.size();i++){
			if(appsum==0){
				rato = "0";
			}else{
				rato = quotient((float)per_num.get(i),(float)appsum);
			}
			sql = "insert into "+fretablename+"(category,per_id,frequency,ratio,category_id)values(?,?,?,?,?)";
			System.out.print("("+i+"\t"+per_num.get(i)+"\t"+rato+")");
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, category);
			pstmt.setInt(2, i);
			pstmt.setInt(3, per_num.get(i));
			pstmt.setString(4, rato);
			pstmt.setInt(5, j);
			pstmt.executeUpdate();
		}
		System.out.println();
	}
	static String quotient(float a, float f) {
		// TODO Auto-generated method stub
		float result1 = a/f;
        String result = String.format("%.3f",result1);
		return result;
	}
	private static ArrayList<Integer> getAppFrequency(String category,
			Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		
		ArrayList<Integer> per_num = new ArrayList<Integer>();
		for(int i = 0;i<285;i++){
			per_num.add(0);
		}
		
		ArrayList<Integer> id_List = new ArrayList<Integer>();
		//String sql = "select appid from recommend_sc_cluster where appid > 428032 and clusterid = ?";
		String sql = "select id from normal_test_permission where id < 428138 and category = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, category);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			id_List.add(rs.getInt(1));
		}
		
		sql = "select id from normal_test_permission where id > 428242 and id < 428557 and category = ?";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, category);
		ResultSet rs2 = pstmt.executeQuery();
		while(rs2.next()){
			id_List.add(rs2.getInt(1));
		}
		appsum = id_List.size();
		rs = null;
		for(int i = 0;i < id_List.size();i++){
			sql = "select realper from normal_test_real_pers where id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id_List.get(i));
			rs = pstmt.executeQuery();
			if(rs.next()){
				if(rs.getString(1).equals("[]")){
					continue;
				}
				String[] str = rs.getString(1).replace("[", "").replace("]", "").split(", ");
				for(int j = 0;j<str.length;j++){						
					per_num.set(Integer.parseInt(str[j]),per_num.get(Integer.parseInt(str[j]))+1);
				}
			}
		}
		return per_num;
	}
}
