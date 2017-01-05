package smile.stream;

import org.json.JSONObject;

import smile.classification.Classifier;
import smile.classification.DecisionTree;
import smile.classification.KNN;
import smile.data.*;

import java.text.ParseException;
import java.util.Iterator;

/**
 * @author joanna
 *         Implementation of Drift Detector Method algorithm with Sliding Window techniques
 * @since 10/25/16
 */
public class DriftDetector {

    private static DriftDetector instance = null;
    private Classifier classifier;
    private int maxWindowSize;
    private String classifierName;
    private Window window;

    private double minProbability = Double.MAX_VALUE;
    private double minDeviation = Double.MAX_VALUE;

    private boolean driftDetected = false;
    private int warningIndex;
    private int numberOfDrifts = 0;

    private int driftIndex;
    private double currentProbability;
    private double currentStandardDeviation;
    private double minSum = Double.MAX_VALUE;
    private AttributeDataset attributeDataset;
    private Attribute[] attributes;
    private Attribute response;

    private DriftDetector(int maxWindowSize, String method, JSONObject jsonObj) {
        this.maxWindowSize = maxWindowSize;
        this.classifierName = method;

        //create attributes[]
        Iterator iterator = jsonObj.keys();
        int i = 0;
        attributes = new Attribute[jsonObj.length()];
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            this.attributes[i] = new StringAttribute(key);
            System.out.println("ilosć iteracji: " + i + " key: " + key);
            i++;
        }

        this.response = new NominalAttribute("class");

        attributeDataset = new AttributeDataset("dataset_name", attributes, response);
    }

    public static DriftDetector getInstance(int maxWindowSize, String classifierName, String data) {
        if (instance == null) {
            JSONObject jsonObj = new JSONObject(data.toString());
            instance = new DriftDetector(maxWindowSize, classifierName, jsonObj);
        }
        return instance;
    }

    public boolean isDriftDetected() {
        return driftDetected;
    }

    public int getNumberOfDrifts() {
        return numberOfDrifts;
    }

    private Datum<double[]> parseString2Datum(String data, int classIndex, int elementNumber) throws ParseException {
        JSONObject jsonObj = new JSONObject(data.toString());

        double[] x = new double[this.attributes.length];
        double y = Double.NaN;

        //parse values from json: key into attributes and value into strings
        String[] strings = new String[jsonObj.length()];
        Iterator iterator = jsonObj.keys();
        int i = 0;
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = String.valueOf(jsonObj.get(key));
            System.out.println("key: " + key + ", value: " + value);
            strings[i] = value;
            i++;
        }

        for (int l = 0, k = 0; l < strings.length; l++) {
            if (l == classIndex) {
                y = response.valueOf(strings[l]);
                System.out.println("y: " + y);
            } else {
                if (attributes[k] != null) {
                    x[k] = attributes[k].valueOf(strings[l]);
                    System.out.println("x[" + k + "]: " + x[k]);
                } else {
                    System.out.println("attributes[" + k + "] is null");
                }
                k++;
            }
        }

        Datum<double[]> datum = new Datum<>(x, y);

        return datum;
    }

    /**
     * Methods for test-then-train current classifier
     * @param data a {@code String} data of JSON example form dataset
     * @param classIndex a {@code int} index of a class in data
     * @param elementNumber a {@code int} of the number of the element
     * @return {@code true} if dirft is detected, otherwise {@code false}
     * @throws ParseException
     */
    public boolean update(String data, int classIndex, int elementNumber) throws ParseException {

        //add element from stream to attributes
        Datum<double[]> datum = parseString2Datum(data, classIndex, elementNumber);
        attributeDataset.add(datum);
        System.out.println("AttributeDataset: size=" + attributeDataset.size());

        //init first window
        if (elementNumber == maxWindowSize) {
            addDataToWindowAndTrainModel(classifierName, maxWindowSize - 1, 2, 3);
            System.out.println("Create a window");
        } else if (elementNumber > maxWindowSize) { //when window is initialized

            int i = addToWindowCheckTheModelIfDriftReturnWarningIndex(datum.x, (int) datum.y, elementNumber, 2, 3);
            System.out.println("i from update is: " + i + " while elementNumber: " + elementNumber);

            if (driftDetected) {
                int diffBetweenDriftAndWarningIndex = elementNumber - i;
                if (diffBetweenDriftAndWarningIndex < maxWindowSize) {
                    for (int j = maxWindowSize - diffBetweenDriftAndWarningIndex; j < maxWindowSize - 1; j++) {
                        addElementToWindow(window.getX(j), window.getY(j));
                    }
                }
                ++numberOfDrifts;
                initialize();
            }
            System.out.println("Number of Drifts: " + this.getNumberOfDrifts() + " for alpha=" + 2 + ", beta=" + 3);
        }

        return driftDetected;
    }

    private void addDataToWindowAndTrainModel(String classifierName, int maxWindowSize, int alpha, int beta) {

        double[][] x = attributeDataset.toArray(new double[0][]);
        int[] y = attributeDataset.toArray(new int[0]);

        window = Window.getInstance(maxWindowSize);
        for (int i = 0; i < maxWindowSize; i++) {
            if (i < x.length && i < x.length) {
                window.add(x[i], y[i]);
            } else {
                System.out.println("i: " + i + " x.length=" + x.length + " y.length=" + y.length);
            }
        }

        this.classifier = trainModel(window, classifierName);
        System.out.println("Model is trained.");
    }

    private void addElementToWindow(double[] x, int y) {
        //add an element to window
        if (window != null) {
            window.add(x, y);
            System.out.print("Added to window: ");
            printValue(x, y);
        }
    }

    private int addToWindowCheckTheModelIfDriftReturnWarningIndex(double[] x, int y, int i, int alpha, int beta) {

        System.out.println("***" + i + "***");

        //add an element to window
        addElementToWindow(x,y);

        //test model
        int driftIndex = testModel(window, classifier, i, alpha, beta);

        if (driftIndex != -1) {
            return warningIndex;
        } else {
            //update model
            classifier = trainModel(window, classifierName);

            return i;
        }
    }

    private Classifier trainModel(Window window, String algorithmType) throws IllegalArgumentException {

        System.out.println("Train model");

        double[][] newX = window.getX();
        int[] newY = window.getY();

        switch (algorithmType) {
            case "knn":
                KNN<double[]> knn = KNN.learn(newX, newY, 15);
                return knn;
            case "tree":
                DecisionTree tree = new DecisionTree(newX, newY, 100, DecisionTree.SplitRule.ENTROPY);
                return tree;
            default:
                return null;
        }
    }

    private void initialize() {
        System.out.println("Initializing all stored constants.");

        driftDetected = false;
        warningIndex = -1;
        driftIndex = -1;

        currentProbability = 1.0;
        currentStandardDeviation = 1.0;
        minSum = Double.MAX_VALUE;
        minProbability = (Double.MAX_VALUE / 2) - 1;
        minDeviation = (Double.MAX_VALUE / 2) - 1;
    }

    private int testModel(Window window, Classifier classifier, int iterator, int alpha, int beta) {
        System.out.println("Test model");

        int error = 0;

        for (int i = 0; i < window.getSize(); i++) {
            if (classifier.predict(window.getX()[i]) != window.getY()[i]) {
                error++;
            }
        }

        System.out.println("Errors: " + error);

        return checkDrift(error, iterator, alpha, beta);
    }

    private int checkDrift(int error, int iterator, int alpha, int beta){
        currentProbability = (double) error / window.getSize();

        currentStandardDeviation = Math.sqrt((currentProbability * (1 - currentProbability)) / window.getSize());

        System.out.format("Probability: %.5f%%%n", currentProbability * 100.0);
        System.out.format("Deviation: %.5f%%%n", currentStandardDeviation);

        //approximated normal distribution according to algorithm 3.5 proposed by Brzezinski
        if (window.getSize() > 30) {
            System.out.println("Window.getSize()=" + window.getSize());
            //checking the min probability and deviation
            if (currentProbability + currentStandardDeviation < minSum) {
                minProbability = currentProbability;
                minDeviation = currentStandardDeviation;
                minSum = currentProbability + currentStandardDeviation;
                System.out.format("MIN probability: %.5f%%%n", minProbability * 100.0);
                System.out.format("MIN deviation: %.5f%%%n", minDeviation);
            }

            System.out.println("Warning index=" + warningIndex);
            //check the first occurrence of warning level
            if (warningIndex <= 0) {
                if (currentProbability + currentStandardDeviation >= minProbability + alpha * minDeviation) {
                    warningIndex = iterator;
                    System.out.println("WARNING LEVEL on " + warningIndex + " index");
                    System.out.format("Probability of misclassified: %.5f%%%n", currentProbability);
                    System.out.format("Standard deviation: %.5f%%%n", currentStandardDeviation);
                }
            }

            //check the occurrence of concept drift
            if (currentProbability + currentStandardDeviation >= minProbability + beta * minDeviation) {
                driftIndex = iterator;
                System.out.println("DRIFT LEVEL on " + driftIndex + " index");
                System.out.format("Probability of misclassified: %.5f%%%n", currentProbability);
                System.out.format("Standard deviation: %.5f%%%n", currentStandardDeviation);
                driftDetected = true;
            }
        }

        return driftIndex;
    }

    private void printValue(double[] x, int y) {
        for (double element : x) {
            System.out.print(element + " ");
        }
        System.out.println("Class: " + y);
    }
}
