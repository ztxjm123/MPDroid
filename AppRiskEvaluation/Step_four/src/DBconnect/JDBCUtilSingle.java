package DBconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * jdbc操作类
 * @author zhangkai
 *
 */
public final class JDBCUtilSingle {
	//public static final String url = "jdbc:mysql://localhost:3306/car_data?zeroDateTimeBehavior=convertToNull";
	public static final String url = "jdbc:mysql://localhost:3306/apprisk";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "root";
	//阿里云服务器mysql密码
//	public static final String password = "lhgIX0dXrp";
	
	public  Connection conn = null;
	private static JDBCUtilSingle jdbcUtilSingle = null;

	public static JDBCUtilSingle getInitJDBCUtil() {
		if (jdbcUtilSingle == null) {
			synchronized (JDBCUtilSingle.class) {
				if (jdbcUtilSingle == null) {
					jdbcUtilSingle = new JDBCUtilSingle();
				}
			}
		}
		return jdbcUtilSingle;
	}

	private JDBCUtilSingle() {
	}

	static {
		try {
			Class.forName(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		try {
			conn = DriverManager.getConnection(url,user,password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;

	}

	public void closeConnection(ResultSet rs, Statement statement, Connection con) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (con != null) {
						con.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
