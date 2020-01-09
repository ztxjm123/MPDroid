package edu.exp.Steps;

import edu.exp.DB.JDBCUtilSingle;
import edu.exp.entity.BianHao;
import edu.exp.entity.Permission;
import edu.exp.service.MysqlService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 求交集和并集
 */
public class StepTwo {


    public static void setBing() throws SQLException {
        Connection connection = JDBCUtilSingle.getInitJDBCUtil().getConnection();
        Statement statement = connection.createStatement();
        MysqlService mysqlService = new MysqlService();

        // permission并集
        List<Permission> list = mysqlService.selectFromPermission(statement);
        for (Permission perm : list) {
            String id = perm.getId();
            String docId = perm.getDocId();
            String permission = perm.getPermission();
            permission = permission.substring(0, permission.length() - 1);
            String[] permissions = permission.split(";");
            Set<String> permissionList = new HashSet<String>();
            for (String str : permissions) {
                permissionList.add(str);
            }
            Permission apiPerm = mysqlService.selectFromApi(statement, id);
            String apiPermersion = apiPerm.getPermission();
            apiPermersion = apiPermersion.substring(0, apiPermersion.length() - 1);
            String[] apiPermissions = apiPermersion.split(";");
            Set<String> apiPermissionList = new HashSet<String>();
            for (String str : apiPermissions) {
                apiPermissionList.add(str);
            }
            permissionList.addAll(apiPermissionList);
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : permissionList) {
                stringBuffer.append(str).append(";");
            }
            mysqlService.saveToBingji(statement, id, docId, stringBuffer.toString());
        }

        // bianhao 编号
        List<BianHao> bianHaoList = mysqlService.selectFromBianHao(statement);
        for (BianHao bian : bianHaoList) {
            String id = bian.getId();
            String bianhao = bian.getBianhao();
            bianhao = bianhao.substring(1, bianhao.length() - 1);
            String[] bianhaos = bianhao.split(", ");
            Set<String> bianhaoList = new HashSet<String>();
            for (String str : bianhaos) {
                bianhaoList.add(str);
            }
            BianHao bianHao2 = mysqlService.selectFromBianApi(statement, id);
            String apiBianhao = bianHao2.getBianhao();
            apiBianhao = apiBianhao.substring(1, apiBianhao.length() - 1);
            String[] apiBianhaos = apiBianhao.split(", ");
            Set<String> apiBianhaoList = new HashSet<String>();
            for (String str : apiBianhaos) {
                apiBianhaoList.add(str);
            }
            bianhaoList.addAll(apiBianhaoList);
            StringBuffer stringBuffer = new StringBuffer("[");
            for (String str : bianhaoList) {
                stringBuffer.append(str).append(", ");
            }
            String string = stringBuffer.toString();
            mysqlService.saveToBingjiBian(statement, id, string.substring(0, string.length() - 1) + "]");
        }
    }

    public static void setJiao() throws SQLException {
        Connection connection = JDBCUtilSingle.getInitJDBCUtil().getConnection();
        Statement statement = connection.createStatement();
        MysqlService mysqlService = new MysqlService();

        // permission交集
        List<Permission> list = mysqlService.selectFromPermission(statement);
        for (Permission perm : list) {
            String id = perm.getId();
            String docId = perm.getDocId();
            String permission = perm.getPermission();
            permission = permission.substring(0, permission.length() - 1);
            String[] permissions = permission.split(";");
            List<String> permissionList = new ArrayList<String>();
            for (String str : permissions) {
                permissionList.add(str);
            }
            Permission apiPerm = mysqlService.selectFromApi(statement, id);
            String apiPermersion = apiPerm.getPermission();
            apiPermersion = apiPermersion.substring(0, apiPermersion.length() - 1);
            String[] apiPermissions = apiPermersion.split(";");
            List<String> apiPermissionList = new ArrayList<String>();
            for (String str : apiPermissions) {
                apiPermissionList.add(str);
            }
            permissionList.retainAll(apiPermissionList);
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : permissionList) {
                stringBuffer.append(str).append(";");
            }
            mysqlService.saveToJiaoji(statement, id, docId, stringBuffer.toString());
        }

        // bianhao交集
        List<BianHao> bianHaoList = mysqlService.selectFromBianHao(statement);
        for (BianHao bian : bianHaoList) {
            String id = bian.getId();
            String bianhao = bian.getBianhao();
            bianhao = bianhao.substring(1, bianhao.length() - 1);
            String[] bianhaos = bianhao.split(", ");
            List<String> bianhaoList = new ArrayList<String>();
            for (String str : bianhaos) {
                bianhaoList.add(str);
            }
            BianHao bianHao2 = mysqlService.selectFromBianApi(statement, id);
            String apiBianhao = bianHao2.getBianhao();
            apiBianhao = apiBianhao.substring(1, apiBianhao.length() - 1);
            String[] apiBianhaos = apiBianhao.split(", ");
            List<String> apiBianhaoList = new ArrayList<String>();
            for (String str : apiBianhaos) {
                apiBianhaoList.add(str);
            }
            bianhaoList.retainAll(apiBianhaoList);
            StringBuffer stringBuffer = new StringBuffer("[");
            for (String str : bianhaoList) {
                stringBuffer.append(str).append(", ");
            }
            String string = stringBuffer.toString();
            mysqlService.saveToJiaojiBian(statement, id, string.substring(0, string.length() - 1) + "]");
        }
    }


}
