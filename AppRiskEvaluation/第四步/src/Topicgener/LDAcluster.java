package Topicgener;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 * LDA 算法
 */
import writeTotxt.test;

public class LDAcluster {
    public static int numTopics = 100;
    public static double thresh = 0.01;

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(new File("data0201/topic_train.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                3, 2, 1)); // data, label, name fields

        fileReader = new InputStreamReader(new FileInputStream(new File("data0201/topic_test.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                3, 2, 1));


        // Create a model with 20 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.1, 0.01);
        //ParallelTopicModel model = new ParallelTopicModel(numTopics, 50.0, 0.01);// alpha = alphaSum/k; alpha=50/k
        //System.out.println(model.alpha[0]);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);
        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)

        //model.setNumIterations(100);
        model.setNumIterations(50);

        model.setSaveSerializedModel(50, "shiyan/savemodel");

        model.estimate();

        Alphabet dataAlphabet = instances.getDataAlphabet();

        //???????
        // Get an array of sorted sets of word ID/count pairs
        StringBuilder sb = new StringBuilder();
        Formatter out = new Formatter(new StringBuilder(), Locale.US);

        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        //???????topicid;????????topic words(???)
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
            System.out.println(out);
        }
        test.writeTxtFile(sb.toString(), "shiyan/model_cf_0.01.txt");
        sb.delete(0, sb.length());

        //lda???
        File output1 = new File("shiyan/topic_train0201.txt");
        File output2 = new File("shiyan/topic_test0201_malicious.txt");
        File output3 = new File("shiyan/topic_test0201_suspend.txt");
        File output4 = new File("shiyan/topic_test0201_live.txt");
        writeInferredDistributions(model, output1, output2, output3, output4, thresh, 20);

    }

    public static void writeInferredDistributions(ParallelTopicModel model,
                                                  File output1, File output2, File output3, File output4, double threshold, int max) throws IOException {

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
