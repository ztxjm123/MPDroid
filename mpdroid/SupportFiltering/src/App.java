import java.util.*;

/**
 * @ClassName App
 * @Description TODO
 * app对象
 * @Author miaoxu
 * @Date 2018/12/28 21:07
 * @Version 1.0
 **/
public class App {
//    app属于这个topic的概率
    HashMap<Topic, Double> topicPro = new HashMap<>();
    //权限列表
    List<Integer> privilegeList= new ArrayList<>();

    //权限支持的概率
    HashMap<Integer, Double> priid_pro = new HashMap<>();
    int id;
    App(int id)
    {
        this.id = id;
    }
    /**
     * @Author miaoxu
     * @Description //TODO
     * @Date 21:11 2018/12/28
     * @Param [t, p] Topic&&Probablity app属于这个topic的概率
     * @return void
     **/
    public void addTopicProbablity(Topic t, double p)
    {
        topicPro.put(t, p);
    }
    /**
     * @Author miaoxu
     * @Description //TODO
     * 加入属于qpp的privilege的id
     * @Date 21:16 2018/12/28
     * @Param [privilegeid]
     * @return void
     **/
    public void addPrivilege(int privilegeid)
    {
        privilegeList.add(privilegeid);
    }

    public void addPriPro(Integer priid, Double pro)
    {
        this.priid_pro.put(priid, pro);

    }

    public HashMap<Topic, Double> getTopicPro() {
        return topicPro;
    }

    public List<Integer> getPrivilegeList()
    {
        return this.privilegeList;
    }

    public HashMap<Integer, Double> getPriid_pro() {
        return priid_pro;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\"id\":" + id + "," + "\"permissions\":");

        Iterator<Map.Entry<Integer, Double>>i =  priid_pro.entrySet().iterator();
        if (!i.hasNext())
        {
            sb.append("[]");
        }
        sb.append("[");
        for (;;) {
            Map.Entry<Integer, Double> e = i.next();
            Integer key = e.getKey();
            Double value = e.getValue();
            sb.append("[");
            sb.append(key);
            sb.append(',');
            sb.append(value);
            if (! i.hasNext())
            {
                sb.append(']').toString();
                break;
            }
            sb.append("],");
        }
        sb.append("]}");
        return sb.toString();
    }


}
