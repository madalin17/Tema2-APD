package parser;

import result.FinalResult;
import result.Result;
import task.Map;
import task.Reduce;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class Parser {
    private final String file; /* input filename */
    private final String outFile; /* output filename */
    private int blocks; /* size of blocks for map tasks */
    private int numberOfFiles; /* number of files to map-and-reduce */

    private final ExecutorService tpe; /* pool of tasks */
    private final AtomicInteger inMapQueue; /* queue(number) of map tasks */
    private final AtomicInteger inReduceQueue; /* queue(number) of reduce tasks */
    private final List<Map> firstTasks; /* list of map tasks */
    private final List<Reduce> waitingTasks; /* synchronised list of reduce tasks */
    private final List<FinalResult> finalResults; /* list of results after map-and-reduce to be printed */
    private final HashMap<String, Integer> fileToIndex; /* easy indexing map from a file to an index */

    /**
     * Constructor using CLI parameters
     * @param workers number of workers in the pool
     * @param file reading file
     * @param outFile writing file
     */
    public Parser(final int workers,
                  final String file,
                  final String outFile) {
        this.file = file;
        this.outFile = outFile;

        this.blocks = 0;
        this.numberOfFiles = 0;

        /* Auxiliary fields */
        this.tpe = Executors.newFixedThreadPool(workers);
        this.inMapQueue = new AtomicInteger(0);
        this.inReduceQueue = new AtomicInteger(0);
        this.firstTasks = new ArrayList<>();
        this.waitingTasks = Collections.synchronizedList(new ArrayList<>());
        this.finalResults = Collections.synchronizedList(new ArrayList<>());
        this.fileToIndex = new HashMap<>();
    }

    /**
     * @return writing file
     */
    public String getOutFile() {
        return outFile;
    }

    /**
     * @return list of results after REDUCE operations
     */
    public List<FinalResult> getFinalResults() {
        return finalResults;
    }

    /**
     * Get the number of map and reduce tasks(IN QUEUES)
     * !!! BETTER TO BE CALCULATED BEFORE SUBMITTING TASKS !!!
     * Create map tasks
     * Create default reduce tasks(with only the file name) and make them wait
     * @throws FileNotFoundException for reading from the file
     */
    public void parseAndAddTasks() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(file));
        blocks = Integer.parseInt(scanner.nextLine());
        numberOfFiles = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < numberOfFiles; i++) {
            String newFile = scanner.nextLine();

            /* for every file, there is a reduce task */
            inReduceQueue.incrementAndGet();
            waitingTasks.add(new Reduce(newFile, this, tpe, inMapQueue, inReduceQueue));
            fileToIndex.put(newFile, i);

            /* divide a file in blocks of set size, read above(see blocks field) */
            long size = new File(newFile).length();
            for (long j = 0; j < size; j += blocks) {
                /* for every block, there is a map task */
                inMapQueue.incrementAndGet();
                firstTasks.add(new Map(newFile, this, tpe, inMapQueue, inReduceQueue, j, blocks, size));
            }
        }

        scanner.close();
    }

    /**
     * Submit all map tasks after parsing the input file
     */
    public void goFirstTasks() {
        firstTasks.forEach(tpe::submit);
    }

    /**
     * Map tasks send a Result object to this instance
     * For the corresponding Reduce task(same name as the result task), the list of maps
     * and list of lists are updated, as well as the number of words in the file
     * @param result object resulting from a map task
     */
    public void addWaitingTask(Result result) {
        /* get corresponding reduce task */
        Reduce reduce = waitingTasks.get(fileToIndex.get(result.getFile()));

        /* update reduce task */
        reduce.getListOfMaps().add(result.getMap());
        reduce.getListOfLongestWords().add(result.getLongestWords());

        /* synchronise updating number of words in the file by executors pool */
        synchronized (tpe) {
            int words = reduce.getNumWords();
            reduce.setNumWords(words + result.getNumWords());
        }
    }

    /**
     * Submit all reduce tasks to pool, after being fully updated and after all map tasks finished
     */
    public void goWaitingTasks() {
        waitingTasks.forEach(tpe::submit);
    }
}
