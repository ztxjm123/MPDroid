package edu.exp.Steps;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import edu.exp.Utils.WriteToTxt;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class StepOne {


    /**
     * 设置参数
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // 权重过滤阈值
        double thresh = 0.01;

        /**
         * 100 话题测试
         */
//        int numTopics = 100;

//        /**
//         * 80 话题测试
//         */
//        int numTopics = 80;

//        /**
//         * 60 话题测试
//         */
//        int numTopics = 60;

        /**
         * 65 话题测试
         */
//        int numTopics = 65;

        /**
         * 75 话题测试
         */
//        int numTopics = 75;

        /**
         * 85 话题测试
         */
//        int numTopics = 85;

        /**
         * 95 话题测试
         */
        int numTopics = 95;


        getResult(thresh, numTopics);
    }

    private static void getResult(double thresh, int numTopics) throws Exception {
        String outPath = numTopics + "话题数据输出";
        File file = new File(outPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        test(outPath, numTopics, thresh);
    }


    // Information:java: Multiple encodings set for module chunk experiment "GBK" will be used by compiler

    // 过度权限识别
    public static void test(String outPath, int numTopics, double thresh) throws Exception {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        // 管道：小写，标记，删除停用词，映射到功能
        // 1、小写处理
        pipeList.add(new CharSequenceLowercase());

        // 2、标记处理
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));

        // 3、删除停用词
        pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false));

        // 4、功能映射
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        // 5、导入训练集，创建输入流，获取训练集中的数据
        Reader fileReader = new InputStreamReader(new FileInputStream(new File("data/topic_train.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data, label, name fields

        // 6、获取测试集中的数据
        fileReader = new InputStreamReader(new FileInputStream(new File("data/topic_test.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                3, 2, 1));


        // Create a model with 20 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.1, 0.01);// 并行主题模型
        //ParallelTopicModel model = new ParallelTopicModel(numTopics, 50.0, 0.01);// alpha = alphaSum/k; alpha=50/k
        //System.out.println(model.alpha[0]);

        model.addInstances(instances);// 添加实例

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);
        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)

        //model.setNumIterations(100);/// 设置迭代次数
        model.setNumIterations(50);

        model.setSaveSerializedModel(50, outPath + "/savemodel");// 设置保存序列化模型

        model.estimate(); // 模型开始估计

        Alphabet dataAlphabet = instances.getDataAlphabet();/// 获取所有的字母？？？

        //保存模型
        // Get an array of sorted sets of word ID/count pairs
        StringBuilder sb = new StringBuilder();
        Formatter out;

        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords(); // 得到排序的单词
        //第一列是topicid;第三列是topic words(权重)
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t", topic);
            int rank = 0;
            while (iterator.hasNext() && rank < 100) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            sb.append(out + "\r\n");
            //System.out.println(out);
        }
        WriteToTxt.writeTxtFile(sb.toString(), outPath + "/model_cf_0.01.txt");
        sb.delete(0, sb.length());

        //lda结果
        File output1 = new File(outPath + "/topic_train0201.txt");
        File output2 = new File(outPath + "/topic_test0201_malicious.txt");
        File output3 = new File(outPath + "/topic_test0201_suspend.txt");
        File output4 = new File(outPath + "/topic_test0201_live.txt");
        writeInferredDistributions(model, output1, output2, output3, output4, thresh, 20, numTopics);

    }

    private static void writeInferredDistributions(ParallelTopicModel model,
                                                   File output1, File output2, File output3, File output4, double threshold, int max, int numTopics) throws IOException {
        PrintWriter out = new PrintWriter(output1);
        int appNum = model.getData().size();
        IDSorter[] sortedTopics = new IDSorter[numTopics];
        for (int topic = 0; topic < numTopics; topic++) {
            // Initialize the sorters with dummy values
            sortedTopics[topic] = new IDSorter(topic, topic);
        }
        if (max < 0 || max > numTopics) {
            max = numTopics;
        }
        for (int appID = 0; appID < appNum; appID++) {

            if (appID == 428032) {
                break;
            }
            double[] topicDistribution = model.getTopicProbabilities(appID);
            out.print(appID + 1);
            out.print(' ');
            // Print the Source field of the instance
            if (model.getData().get(appID).instance.getName() != null) {
                out.print(model.getData().get(appID).instance.getName());
            } else {
                out.print("null-source");
            }
            out.print(' ');
            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic].set(topic, topicDistribution[topic]);
            }
            Arrays.sort(sortedTopics);
            for (int i = 0; i < max; i++) {
                if (sortedTopics[i].getWeight() < threshold) {
                    break;
                }
                out.print(sortedTopics[i].getID() + " " +
                        sortedTopics[i].getWeight() + " ");
            }
            out.print(" \n");
        }
        out.close();

        out = new PrintWriter(output2);
        for (int appID = 428032; appID < appNum; appID++) {

            if (appID == 428556) {
                break;
            }

            double[] topicDistribution = model.getTopicProbabilities(appID);

            out.print(appID + 1);
            out.print(' ');

            // Print the Source field of the instance
            if (model.getData().get(appID).instance.getName() != null) {
                out.print(model.getData().get(appID).instance.getName());
            } else {
                out.print("null-source");
            }
            out.print(' ');

            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic].set(topic, topicDistribution[topic]);
            }
            Arrays.sort(sortedTopics);

            for (int i = 0; i < max; i++) {
                if (sortedTopics[i].getWeight() < threshold) {
                    break;
                }
                out.print(sortedTopics[i].getID() + " " +
                        sortedTopics[i].getWeight() + " ");
            }
            out.print(" \n");
        }
        out.close();

        out = new PrintWriter(output3);
        for (int appID = 428556; appID < appNum; appID++) {

            if (appID == 444899) {
                break;
            }

            double[] topicDistribution = model.getTopicProbabilities(appID);

            out.print(appID + 1);
            out.print(' ');

            // Print the Source field of the instance
            if (model.getData().get(appID).instance.getName() != null) {
                out.print(model.getData().get(appID).instance.getName());
            } else {
                out.print("null-source");
            }
            out.print(' ');

            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic].set(topic, topicDistribution[topic]);
            }
            Arrays.sort(sortedTopics);

            for (int i = 0; i < max; i++) {
                if (sortedTopics[i].getWeight() < threshold) {
                    break;
                }
                out.print(sortedTopics[i].getID() + " " +
                        sortedTopics[i].getWeight() + " ");
            }
            out.print(" \n");
        }
        out.close();
        out = new PrintWriter(output4);
        for (int appID = 444899; appID < appNum; appID++) {
            double[] topicDistribution = model.getTopicProbabilities(appID);
            out.print(appID + 1);
            out.print(' ');
            // Print the Source field of the instance
            if (model.getData().get(appID).instance.getName() != null) {
                out.print(model.getData().get(appID).instance.getName());
            } else {
                out.print("null-source");
            }
            out.print(' ');
            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic].set(topic, topicDistribution[topic]);
            }
            Arrays.sort(sortedTopics);
            for (int i = 0; i < max; i++) {
                if (sortedTopics[i].getWeight() < threshold) {
                    break;
                }
                out.print(sortedTopics[i].getID() + " " +
                        sortedTopics[i].getWeight() + " ");
            }
            out.print(" \n");
        }
        out.close();
    }

}
