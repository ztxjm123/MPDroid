package DBconnect;


/**
 * jdbcÊý¾Ý¿â²Ù×÷
 *
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBPermission {

	static String DRIVER = "com.mysql.jdbc.Driver";
	static String URL = "jdbc:mysql://localhost:3306/permission";
	static String USER = "root";
	static String PWD = "root";

	static Connection conn = getConn();
	static PreparedStatement pstmt = null;
	static ResultSet rs = null;

	public static Connection getConn() {
		try {
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL,USER,PWD);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static PreparedStatement getPstmt(String sql, Object...objects) {
		try {
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < objects.length; i++) {
				pstmt.setObject(i + 1, objects[i]);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pstmt;
	}

	public static int updateDB(String sql, Object... objects) {
		pstmt = getPstmt(sql, objects);
		int rows = 0;
		try {
			rows = pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
//			closePstmt(pstmt);
//			closeConn(conn);
		}
		return rows;
	}

	public static ResultSet getRs(String sql, Object... objects) {
		
		pstmt = getPstmt(sql, objects);
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	public static void close(){
		closePstmt(pstmt);
		closeRs(rs);
		closeConn(conn);
	}
	public static void closeConn(Connection conn) {

		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

	}
	public static void closeRs(ResultSet rs) {

		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

	}
	public static void closePstmt(PreparedStatement pstmt) {

		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

	}


}
