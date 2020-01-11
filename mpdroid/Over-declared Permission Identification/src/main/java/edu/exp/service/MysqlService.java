package edu.exp.service;

import edu.exp.entity.BianHao;
import edu.exp.entity.Permission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MysqlService {

    public void saveToApi(Statement statement, int apkId, String apkName, String permissionName, String apiName) throws SQLException {
        String sql = "insert into api(apk_id,apk_name,permission_name,api_name) values"
                + "(\"" + apkId + "\","
                + "\"" + apkName + "\","
                + "\"" + permissionName + "\","
                + "\"" + apiName + "\")";
//        System.out.println(sql);
        statement.execute(sql);
    }

    public void saveToApk(Statement statement, int id, String apkName, String permissionNames, String permissionCodes) throws SQLException {
        String sql = "insert into apk(id,apk_name,permission_names,permission_codes) values"
                + "(\"" + id + "\","
                + "\"" + apkName + "\","
                + "\"" + permissionNames + "\","
                + "\"" + permissionCodes + "\")";
//        System.out.println(sql);
        statement.execute(sql);
    }

    public List<Permission> selectFromPermission(Statement statement) throws SQLException {
        List<Permission> list = new ArrayList<Permission>();
        String sql = "select * from permission";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            Permission permission = new Permission();
            permission.setId(resultSet.getString("id"));
            permission.setDocId(resultSet.getString("docid"));
//            permission.setPermission(resultSet.getString("lessper"));
            permission.setPermission(resultSet.getString("permission"));
            list.add(permission);
        }
        return list;
    }

    public Permission selectFromApi(Statement statement, String id) throws SQLException {
        Permission permission = new Permission();
        String sql = "select * from api_permission where id = \"" + id + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            permission.setId(resultSet.getString("id"));
            permission.setDocId(resultSet.getString("docid"));
            permission.setPermission(resultSet.getString("lessper"));
        }
        return permission;
    }

    public void saveToJiaoji(Statement statement, String id, String docid, String permission) throws SQLException {
        String sql = "insert into jiaoji(id,docid,permission) values"
                + "(\"" + id + "\","
                + "\"" + docid + "\","
                + "\"" + permission + "\")";
//        System.out.println(sql);
        statement.execute(sql);
    }

    public List<BianHao> selectFromBianHao(Statement statement) throws SQLException {
        List<BianHao> list = new ArrayList<BianHao>();
        String sql = "select * from permission_bianhao";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            BianHao bianHao = new BianHao();
            bianHao.setId(resultSet.getString("id"));
            bianHao.setBianhao(resultSet.getString("bianhao"));
            list.add(bianHao);
        }
        return list;
    }

    public BianHao selectFromBianApi(Statement statement, String id) throws SQLException {
        BianHao bianHao = new BianHao();
        String sql = "select * from api_permission_bianhao where id = \"" + id + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            bianHao.setId(resultSet.getString("id"));
            bianHao.setBianhao(resultSet.getString("bianhao"));
        }
        return bianHao;
    }

    public void saveToJiaojiBian(Statement statement, String id, String bianhao) throws SQLException {
        String sql = "insert into jiaoji_bianhao(id,bianhao) values"
                + "(\"" + id + "\","
                + "\"" + bianhao + "\")";
//        System.out.println(sql);
        statement.execute(sql);
    }

    public void saveToBingji(Statement statement, String id, String docid, String permission) throws SQLException {
        String sql = "insert into bingji(id,docid,permission) values"
                + "(\"" + id + "\","
                + "\"" + docid + "\","
                + "\"" + permission + "\")";
//        System.out.println(sql);
        statement.execute(sql);
    }

    public void saveToBingjiBian(Statement statement, String id, String bianhao) throws SQLException {
        String sql = "insert into bingji_bianhao(id,bianhao) values"
                + "(\"" + id + "\","
                + "\"" + bianhao + "\")";
//        System.out.println(sql);
        statement.execute(sql);
    }
}
