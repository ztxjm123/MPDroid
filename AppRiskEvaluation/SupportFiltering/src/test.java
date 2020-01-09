import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName test
 * @Description TODO
 * @Author miaoxu
 * @Date 2019/1/5 20:44
 * @Version 1.0
 **/
public class test {
    public void writeFile(String filename, App a) throws IOException {
//        /* 第三种：BufferedWriter */
//        BufferedWriter bw = new BufferedWriter(new FileWriter(filename,true));
//        bw.write(a.toString());
//        bw.flush();
//        bw.close();


    }
    public static void main(String[] args) throws IOException {
//        HashMap<Integer, Double> integerDoubleHashMap=  new HashMap<>();
//        integerDoubleHashMap.put(1, 2.0);
//        App a = new App(1);
//        a.getPriid_pro().put(1, 3.0);
//        a.getPriid_pro().put(2, 3.0);
//        new test().writeFile("C:\\Users\\mx\\Desktop\\代码权限-良性App.txt",a);
//        System.out.println(integerDoubleHashMap.toString());

        System.out.println(weekSundayStar(7));


    }

    private static Integer weekSundayStar(int week) {
        return week == 7 ? 1 : (week + 1);
    }
}
