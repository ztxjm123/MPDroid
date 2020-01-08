import java.util.HashMap;

/**
 * @ClassName Repository
 * @Description TODO
 * @Author miaoxu
 * @Date 2018/12/28 21:27
 * @Version 1.0
 **/
public class Repository {
    //app的id和app
    HashMap<Integer, App> appid_app = new HashMap<>();
    //测试集中的app的id和app
    HashMap<Integer, App> test_appid_app = new HashMap<>();
    //topic的id和topic
    HashMap<Integer, Topic> topicid_topic = new HashMap<>();
    //权限的名字和id
    HashMap<String, Integer> pri_pridid = new HashMap<>();

    public void addApp(App app)
    {
       appid_app.put(app.id, app);
    }

    public void addTopic(Topic t)
    {
        topicid_topic.put(t.topicid, t);
    }

    public void addpri_priid(String priname, Integer priid)
    {
        pri_pridid.put(priname, priid);
    }

    public HashMap<Integer, App> getAppid_app() {
        return appid_app;
    }

    public HashMap<Integer, Topic> getTopicid_topic() {
        return topicid_topic;
    }

    public HashMap<String, Integer> getPri_pridid() {
        return pri_pridid;
    }

    public HashMap<Integer, App> getTest_appid_app() {
        return test_appid_app;
    }
}
