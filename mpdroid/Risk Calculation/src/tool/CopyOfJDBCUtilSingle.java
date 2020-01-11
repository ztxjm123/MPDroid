package tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * jdbc²Ù×÷Àà
 * @author zhangkai
 *
 */
public final class CopyOfJDBCUtilSingle {
	//public static final String url = "jdbc:mysql://localhost:3306/car_data?zeroDateTimeBehavior=convertToNull";
	//public static final String url = "jdbc:mysql://localhost:3306/cfshiyan";
	public static final String url = "jdbc:mysql://localhost:3306/permission";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "root";
	
	public  Connection conn = null;
	private static CopyOfJDBCUtilSingle jdbcUtilSingle = null;

	public static CopyOfJDBCUtilSingle getInitJDBCUtil() {
		if (jdbcUtilSingle == null) {
			synchronized (CopyOfJDBCUtilSingle.class) {
				if (jdbcUtilSingle == null) {
					jdbcUtilSingle = new CopyOfJDBCUtilSingle();
				}
			}
		}
		return jdbcUtilSingle;
	}

	private CopyOfJDBCUtilSingle() {
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
