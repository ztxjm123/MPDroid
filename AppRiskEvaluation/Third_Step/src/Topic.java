import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName Topic
 * @Description TODO
 * Topic 对象
 * @Author miaoxu
 * @Date 2018/12/28 21:08
 * @Version 1.0
 **/
public class Topic {
    int topicid;
    Topic(int id)
    {
        this.topicid = id;
    }
    //权限的支持度，<权限id,概率>
    HashMap<Integer, Double> priid_pro = new HashMap<>();

    //包含的appid
    List<Integer> appidlist = new ArrayList<>();

    //包含的app及其对应的概率
    HashMap<Integer, Double> appidPro = new HashMap<>();
    public List<Integer> getAppidlist() {
        return appidlist;
    }

    public void addPriPro(Integer priid, Double pro)
    {
        priid_pro.put(priid, pro);
    }

    public HashMap<Integer, Double> getPriid_pro() {
        return priid_pro;
    }

    public HashMap<Integer, Double> getAppidPro() {
        return appidPro;
    }
}
