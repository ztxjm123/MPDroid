import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @className: Filters
 * @program: TopicAPP
 * @description: 过滤支持度小于多少的权限
 * @author: linsheng
 * @create: 2019-01-08 14:32
 **/
public class Filters {

    /**
     * 过滤良性推荐和恶性推荐文件， 这个地方是用来把支持的中支持度比较低的数据过滤，
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, JSONException {
        String unionPath = "数据源/正式数据源/测试输出/合并权限-良性App.txt";
        String recommendPath = "数据源/五分之四App.txt";

        Double threshold = 0.1; // 这个是过滤

        // 1、读取合并后的文件，转换成json数组
        BufferedReader 合并权限 = new BufferedReader(new FileReader(unionPath));

        HashMap<Integer, App> 合并权限app = new HashMap<>();
        String read = "";

        while ((read = 合并权限.readLine()) != null) {
            JSONObject readJson = new JSONObject(read);
            int readId = readJson.getInt("id");
            App app = new App(readId);
            JSONArray readPermissions = readJson.getJSONArray("permissions");

            HashMap<Integer, Double> permissions = APPPrivilegeProbablity.jsonToArray(readPermissions);
            permissions.forEach((perr, posi) -> app.addPriPro(perr, posi));
            合并权限app.put(readId, app);
        }

        // 2、读取原始数据库，获取到权限，然后分割，查看权限的编号，然后再查询他的支持度，低则过滤删除
        BufferedReader recommendApp = new BufferedReader(new FileReader(recommendPath));
        String appReader ="";
        while ((appReader = recommendApp.readLine()) != null) {
            int appid = Integer.parseInt(appReader);

        }



        // 3、遍历需要过滤的文件的每个权限，查看它的支持度是多少，支持度小于多少就直接去掉
        // 4、打包返回数据
//        new APPPrivilegeProbablity().writeFile("数据源/0/0/推荐-训练恶性-过滤.txt", 过滤权限app);
    }
}
