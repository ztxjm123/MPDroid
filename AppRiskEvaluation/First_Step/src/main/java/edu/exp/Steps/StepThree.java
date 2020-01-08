package edu.exp.Steps;

import edu.exp.Utils.ReadFromFile;

import java.sql.*;
import java.util.List;

public class StepThree {

    public static final String url = "jdbc:mysql://localhost:3306/analyzedata?characterEncoding=utf8&serverTimezone=GMT%2B8";
    public static final String name = "com.mysql.cj.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "123456";

    public static void callProcOne()  throws SQLException {
//        String url = "jdbc:mysql://localhost:3306/app_permission1";
//        String name = "com.mysql.jdbc.Driver";
//        String user = "root";
//        String password = "ztxjm";

        try{
            Connection conn = DriverManager.getConnection(url, user, password);//???????
            String sql = "{CALL Proc_2_permission()}";
            CallableStatement cst = conn.prepareCall(sql);
            cst.execute();
            cst.close();

            conn.close();
            System.out.println("Done!");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static void callProcTwo()  throws SQLException {
        List<String> list = ReadFromFile.readFileByLines("topic/topic_test0201_suspend.txt");
        String url = "jdbc:mysql://localhost:3306/app_permission1";
        String name = "com.mysql.jdbc.Driver";
        String user = "root";
        String password = "ztxjm";

        try{
            Connection conn = DriverManager.getConnection(url, user, password);//获取连接

            PreparedStatement pst = conn.prepareStatement("delete from lda_topic");//准备执行语句
            pst.execute();
            pst.close();

            pst = conn.prepareStatement("delete from permission");
            pst.execute();
            pst.close();

            for (String item:list){
                String[] ss = item.split(" ");
                String sql="insert into permission(id,docid) values(?,?)";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.valueOf(ss[0]).intValue());
                pst.setString(2, ss[1]);
                pst.execute();
                pst.close();

                int len = ss.length;
                int layer = (len-2)/2;
                for(int i=0;i<layer;i++){
                    sql="insert into lda_topic(id,topicid,probability) values(?,?,?)";
                    pst = conn.prepareStatement(sql);
                    pst.setInt(1, Integer.valueOf(ss[0]).intValue());
                    pst.setInt(2, Integer.valueOf(ss[2+i*2]).intValue());
                    pst.setDouble(3, Double.valueOf(ss[2+2*i+1]).doubleValue());
                    pst.execute();
                    pst.close();
                }
            }

            String sql = "{CALL Proc_app_permission_list()}";
            CallableStatement cst = conn.prepareCall(sql);
            cst.execute();
            cst.close();

            conn.close();
            System.out.println("Done!");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

}
