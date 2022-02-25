package task;

import parser.Parser;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MapReduce implements Runnable {
    private final String file; /* file to operate on */
    private final Parser parser; /* parser reference */
    private final ExecutorService tpe; /* pool of tasks */
    private final AtomicInteger inMapQueue; /* queue of map tasks - number of tasks waiting or running */
    private final AtomicInteger inReduceQueue; /* queue of reduce tasks - number of tasks waiting or running */

    /**
     * Constructor for maps and reduces tasks
     * @param file file to operate on
     * @param parser parser reference
     * @param tpe pool of tasks
     * @param inMapQueue number of map tasks waiting or running
     * @param inReduceQueue number of reduce tasks waiting or running
     */
    public MapReduce(final String file,
                     final Parser parser,
                     final ExecutorService tpe,
                     final AtomicInteger inMapQueue,
                     final AtomicInteger inReduceQueue) {
        this.file = file;
        this.parser = parser;
        this.tpe = tpe;
        this.inMapQueue = inMapQueue;
        this.inReduceQueue = inReduceQueue;
    }

    /**
     * Getter for file
     * @return file to operate on
     */
    public String getFile() {
        return file;
    }

    /**
     * Getter for parser
     * @return parser reference
     */
    public Parser getParser() {
        return parser;
    }

    /**
     * Getter for pool of tasks
     * @return pool of tasks
     */
    public ExecutorService getTpe() {
        return tpe;
    }

    /**
     * Getter for map queue
     * @return number of map tasks waiting or running
     */
    public AtomicInteger getInMapQueue() {
        return inMapQueue;
    }

    /**
     * Getter for reduce queue
     * @return number of reduce tasks waiting or running
     */
    public AtomicInteger getInReduceQueue() {
        return inReduceQueue;
    }
}
