package edu.exp;


import edu.exp.Steps.StepFour;
import edu.exp.Steps.StepOne;
import edu.exp.Steps.StepThree;
import edu.exp.Steps.StepTwo;
import org.json.JSONException;

import java.io.IOException;
import java.sql.SQLException;

public class Experiment {
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        // Begin by importing documents from text to feature sequences
        // 首先将文档从文本导入到特征序列
//        StepOne.impStepOne();

        StepTwo.setBing();

//        Thread thread1 = new Thread(new Runnable() {
//            public void run() {
//                try {
//                    StepTwo.setBing();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        Thread thread2 = new Thread(new Runnable() {
//            public void run() {
//                try {
//                    StepTwo.setJiao();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        thread1.start();
//        thread2.start();
////
//        StepThree.callProcOne();
//        StepThree.callProcTwo();
////
////
//        String file = "good";
//        StepFour.runResult(file, "0.8");
    }
}
