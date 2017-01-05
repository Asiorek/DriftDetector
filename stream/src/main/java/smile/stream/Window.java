package smile.stream;

import smile.math.DoubleArrayList;

import java.util.LinkedList;

/**
 * @author joanna
 * {@link Window} is an implementation of Sliding Windows, which gather data to be processed by the Drift Detection Method
 */
public class Window {

    private static Window instance = null;
    private int maxSize;
    private LinkedList<DoubleArrayList> fifoX;
    private LinkedList<Integer> fifoY;

    private Window(int maxWindowSize) {
        this.maxSize = maxWindowSize;
        this.fifoX = new LinkedList<>();
        this.fifoY = new LinkedList<>();
    }

    public static Window getInstance(int maxWindowSize) {
        if (instance == null) {
            instance = new Window(maxWindowSize);
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

    /**
     * Get the attributes
     *
     * @return {@code double[][]}
     */
    public double[][] getX() {
        double[][] primitives = new double[fifoX.size()][fifoX.get(0).size()];
        for (int i = 0; i < fifoX.size() - 1; i++) {
            for (int j = 0; j < fifoX.get(i).size() - 1; j++) {
                primitives[i][j] = fifoX.get(i).get(j);
            }
        }
        return primitives;
    }

    /**
     * Get the i-th attributes from the {@link Window}
     * @param i the number of the row
     * @return i-th {@code double[]} of attributes
     */
    public double[] getX(int i) {
        return fifoX.get(i).toArray();
    }

    /**
     * Get the classes
     *
     * @return {@code int[]}
     */
    public int[] getY() {
        Integer[] integers = new Integer[fifoY.size()];
        for (int i = 0; i < fifoY.size(); i++) {
            integers[i] = fifoY.get(i);
        }
        int[] primitives = Window.toPrimitive(integers);
        return primitives;
    }

    /**
     * Get the i-th class from the {@link Window}
     * @param i the number of the row
     * @return an i-th {@code int} of class
     */
    public int getY(int i) {
        return fifoY.get(i);
    }

    /**
     * Add new element to {@link Window}
     *
     * @param xElement attributes
     * @param yElement class
     */
    public void add(double[] xElement, int yElement) {
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
    public void clear() {
        for (int i = 0; i < fifoY.size(); i++) {
            fifoY.remove();
            fifoX.remove();
        }
    }

    /**
     * Return the current size of Window with data,
     * @return {@code int}, which indicates current Window's size
     */
    public int getSize() {
        return fifoX.size() == fifoY.size() ? fifoX.size() : 0;
    }

}

