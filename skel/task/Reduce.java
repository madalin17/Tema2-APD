package task;

import fibo.Fibonacci;
import parser.Parser;
import result.FinalResult;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public final class Reduce extends MapReduce {
    /* list of maps of sizes and number of words with that size from all map tasks operating on this file */
    private final List<HashMap<Integer, Integer>> listOfMaps;
    /* list of lists of the longest words from all map tasks operating on this file */
    private final List<List<String>> listOfLongestWords;

    /* map that combines the list of maps above into a single one with the same uses for keys and values */
    private final HashMap<Integer, Integer> combinedMap;
    /* list of the longest words in the whole file */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> combinedLongestWords;

    private float rank; /* rank of the file */
    private int maxLen; /* maximum length of a word in this file */
    private int numWords; /* number of words in this file */
    private int numWordsMaxLen; /* number of words of maximum length in the file */

    /**
     * Constructor for a reduce task, using a map-reduce constructor
     * @param file file to operate on
     * @param parser parser reference
     * @param tpe pool of tasks
     * @param inMapQueue number of map tasks waiting or running
     * @param inReduceQueue number of reduce tasks waiting or running
     */
    public Reduce(final String file,
                  final Parser parser,
                  final ExecutorService tpe,
                  final AtomicInteger inMapQueue,
                  final AtomicInteger inReduceQueue) {
        super(file, parser, tpe, inMapQueue, inReduceQueue);

        /* Auxiliary fields */
        this.listOfMaps = Collections.synchronizedList(new ArrayList<>());
        this.listOfLongestWords = Collections.synchronizedList(new ArrayList<>());
        this.combinedMap = new HashMap<>();
        this.combinedLongestWords = new ArrayList<>();
        this.rank = 0f;
        this.maxLen = 0;
        this.numWords = 0;
        this.numWordsMaxLen = 0;
    }

    /**
     * Getter for the list of maps, used to update a reduce task, during the map tasks
     * @return the list of maps
     */
    public List<HashMap<Integer, Integer>> getListOfMaps() {
        return listOfMaps;
    }

    /**
     * Getter for the list of lists, used to update a reduce task, during the map tasks
     * @return the list of lists
     */
    public List<List<String>> getListOfLongestWords() {
        return listOfLongestWords;
    }

    /**
     * Getter for the number of words, used to update a reduce task, during the map tasks
     * @return the number of words in this file
     */
    public int getNumWords() {
        return numWords;
    }

    /**
     * Setter for the number of words, used to update a reduce task, during the map tasks
     * @param numWords the new number of words in the file
     */
    public void setNumWords(int numWords) {
        this.numWords = numWords;
    }

    /**
     * Combine phase for a reduce task
     */
    private void combine() {
        for (HashMap<Integer, Integer> map : listOfMaps) {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                int value = 0;
                if (combinedMap.containsKey(entry.getKey())) {
                    value = combinedMap.get(entry.getKey());

                }
                combinedMap.put(entry.getKey(), value + entry.getValue());
            }
        }

        for (List<String> list : listOfLongestWords) {
            for (String word : list) {
                int len = word.length();
                if (len > maxLen) {
                    combinedLongestWords.clear();
                    combinedLongestWords.add(word);
                    maxLen = len;
                    numWordsMaxLen = 1;
                } else if (len == maxLen) {
                    combinedLongestWords.add(word);
                    numWordsMaxLen++;
                }
            }
        }
    }

    /**
     * Process phase for a reduce task
     */
    private void process() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> entry : combinedMap.entrySet()) {
            synchronized (Fibonacci.getFibonacci()) { /* multiple reduce tasks want to access fibo at the same time */
                sum += Fibonacci.getFibonacci().compute(entry.getKey() + 1) * entry.getValue();
            }
        }

        rank = numWords != 0 ? (float) sum / numWords : 0f; /* make sure there are words in the file */
    }

    /**
     * Finishes the task, update the list of results after reduce tasks,
     * and submits a print task if all reduce tasks finished
     */
    private void end() {
        int task = super.getInReduceQueue().decrementAndGet(); /* signal this task is ending */
        /* update the lists of results, after reduce tasks, that are about to be written in the output file */
        super.getParser().getFinalResults().add(new FinalResult(super.getFile(), rank, maxLen, numWordsMaxLen));

        /* if all reduce tasks end, submit a print task, to print all outputs of reduce tasks to the output file */
        synchronized (super.getTpe()) {
            if (task == 0) {
                super.getTpe().submit(new Print(super.getTpe(),
                        super.getParser().getFinalResults(), super.getParser().getOutFile()));
            }
        }
    }

    /**
     * Running function for the task
     */
    @Override
    public void run() {
        combine(); /* combine phase */
        process(); /* process phase */
        end(); /* final phase */
    }
}
