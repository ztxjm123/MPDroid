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

import tool.JDBCUtilSingle;
import tool.test;

public class CB {

    static double topic_threshold = 0;

    //ÑµÁ·¼¯
    static String trainset = "goodapp";
    static String dbstr = "";

    // ¾ØÕó
    static Float[][] Matrix = new Float[42][285];

    /**
     * Ö÷º¯Êý
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        Connection conn = JDBCUtilSingle.getInitJDBCUtil().getConnection();
        int i = 0;
        while (i < 42) {
            int j = 0;
            while (j < 285) {
                Matrix[i][j] = Float.parseFloat(getRato(i, j, conn));
                j++;
            }
            i++;
        }
        String inpath = "0517/new/malicious0.2_3.txt";//²âÊÔ¼¯
        dbstr = "test";
        readTS(inpath, Matrix, conn);
    }

    private static void readTS(String inpath, Float[][] matrix, Connection conn) throws Exception {
        // TODO Auto-generated method stub
        BufferedReader br = null;
        String recommendPer = "";
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(inpath), "UTF-8"));
            String s = "";
            int id;
            int count = 1;
            while ((s = br.readLine()) != null) {
                String[] instancestr = s.split(" ");
                id = Integer.parseInt(instancestr[0]);
                recommendPer = getRecommend(id, conn);
                sb.append("{\"id\": " + id + ", \"permissions\": " + "[" + recommendPer + "]}" + "\r\n");
                if (count % 100 == 0) {
                    test.writeTxtFile(sb.toString(), "0517/new/0520result/0.2_3/CB_malicious0.2_" + trainset + ".txt");
                    sb.delete(0, sb.length());
                }
                System.out.println("{\"id\": " + id + ", \"permissions\": " + "[" + recommendPer + "]}");//{"id": 332380, "permissions": [[140, 0.8461], [276, 0.8461]]}
                count++;
            }
            test.writeTxtFile(sb.toString(), "0517/new/0520result/0.2_3/CB_malicious0.2_" + trainset + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    private static String getRecommend(int id, Connection conn) throws SQLException {
        // TODO Auto-generated method stub
        Float[] max = new Float[285];
        String recommendPer = "";
        String category = "";
        int category_id = -1;
        String sql = "select category from normal_" + dbstr + "_permission where id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rss = pstmt.executeQuery();
        if (rss.next()) {
            category = rss.getString(1);
        }
        sql = "select category_id from " + trainset + "_cb_recommend_frequency where category = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, category);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            category_id = rs.getInt(1);
        }
        for (int j = 0; j < max.length; j++) {
            max[j] = Matrix[category_id][j];
        }
        for (int j = 0; j < max.length; j++) {
            if (max[j] > 0) {
                recommendPer = recommendPer + "[" + j + ", " + max[j] + "]" + ", ";
            } //[118, 0.8123], [140, 0.8123], [276, 0.8123]
        }
        if (!recommendPer.equals("")) {
            recommendPer = recommendPer.substring(0, recommendPer.lastIndexOf(", "));
        }
        return recommendPer;
    }

    private static String getRato(int i, int j, Connection conn) throws SQLException {
        // TODO Auto-generated method stub
        String rato = "";
        String sql = "select ratio from " + trainset + "_cb_recommend_frequency where category_id = ? and per_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, i);
        pstmt.setInt(2, j);
        ResultSet rss = pstmt.executeQuery();
        if (rss.next()) {
            rato = rss.getString(1);
        } else {
            rato = "0.000";
        }
        return rato;
    }
}
