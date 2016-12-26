package smile.classification;

import smile.math.DoubleArrayList;

import java.util.LinkedList;

/**
 * Created by joanna on 11/14/16.
 * Window is a class, which gather data to process by the Drift Detection Method
 */
public class Window {

    private static Window instance = null;
    private static int maxSize;
    private LinkedList<DoubleArrayList> fifoX;
    private LinkedList<Integer> fifoY;
    private int entropy;

    private Window(int maxWindowSize) {
        maxSize = maxWindowSize;
        this.fifoX = new LinkedList<>();
        this.fifoY = new LinkedList<>();
    }

    public static Window getInstance(int maxWindowSize) {
        if (instance == null) {
            instance = new Window(maxWindowSize);
        }
        return instance;
    }

    public static Window getInstance() {
        if (instance == null) {
            instance = new Window(maxSize);
        }
        return instance;
    }

    /**
     * <p>Converts an array of object Integers to primitives.
     * Got from Apache Commons library.
     * <p>
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Integer} array, may be {@code null}
     * @return an {@code int} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static int[] toPrimitive(final Integer[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new int[0];
        }
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private LinkedList<DoubleArrayList> getFifoX() {
        return fifoX;
    }

    private LinkedList<Integer> getFifoY() {
        return fifoY;
    }

    /**
     * Get the attributes
     *
     * @return double[][]
     */
    double[][] getX() {
        double[][] primitives = new double[fifoX.size()][fifoX.get(0).size()];
        for (int i = 0; i < fifoX.size() - 1; i++) {
            for (int j = 0; j < fifoX.get(i).size() - 1; j++) {
                primitives[i][j] = fifoX.get(i).get(j);
            }
        }
        return primitives;
    }

    /**
     * Get the classes
     *
     * @return int[]
     */
    int[] getY() {
        Integer[] integers = new Integer[fifoY.size()];
        for (int i = 0; i < fifoY.size(); i++) {
            integers[i] = fifoY.get(i);
        }
        int[] primitives = Window.toPrimitive(integers);
        return primitives;
    }

    /**
     * Add new element to window.
     *
     * @param xElement attributes
     * @param yElement class
     */
    void add(double[] xElement, int yElement) {
        if (fifoX.size() == maxSize) {
            fifoX.removeFirst();
            fifoY.removeFirst();
        }
        fifoX.addLast(new DoubleArrayList(xElement));
        fifoY.addLast(yElement);
    }

    /**
     * Clear the window by removing all data.
     */
    void clear() {
        for (int i = 0; i < fifoY.size(); i++) {
            fifoY.remove();
            fifoX.remove();
        }
    }

    /**
     * Change the current window into new window,
     *
     * @param newWindow new window with possible values
     */
    void changeInto(Window newWindow) {
        fifoX = newWindow.getFifoX();
        fifoY = newWindow.getFifoY();
    }

    /**
     * Return the current size of the window with data,
     */
    int getSize() {
        return fifoX.size() == fifoY.size() ? fifoX.size() : 0;
    }

    public int getEntropy() {
        return entropy;
    }
}

