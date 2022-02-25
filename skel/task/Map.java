package task;

import constants.Constants;
import parser.Parser;
import result.Result;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public final class Map extends MapReduce {
    private long offset; /* offset where the reading starts in this file, denotes where fragment starts */
    private int blocks; /* the number of characters processed by this task, denotes where fragment ends */
    private final long size; /* the total size of the files in characters */
    private final HashMap<Integer, Integer> map; /* map of words' size and the number of words with that size */
    private final List<String> longestWords; /* list of the longest words */
    private int numWords; /* the number of words in this segment of the file */

    /**
     * Constructor for a map task, using a map-reduce constructor
     * @param file file to operate on
     * @param parser parser reference
     * @param tpe pool of tasks
     * @param inMapQueue number of map tasks waiting or running
     * @param inReduceQueue number of reduce tasks waiting or running
     * @param offset offset where the reading starts in this file
     * @param blocks the number of characters processed by this task
     * @param size the total size of the files in characters
     */
    public Map(final String file,
               final Parser parser,
               final ExecutorService tpe,
               final AtomicInteger inMapQueue,
               final AtomicInteger inReduceQueue,
               final long offset,
               final int blocks,
               final long size) {
        super(file, parser, tpe, inMapQueue, inReduceQueue);
        this.offset = offset;
        this.blocks = blocks;
        this.size = size;

        /* Auxiliary fields */
        this.map = new HashMap<>();
        this.longestWords = new ArrayList<>();
        this.numWords = 0;
    }

    /**
     * Moving the start of the fragment to the left if the fragment starts
     * in the middle of a word or with a sequence of delimiters
     */
    private void moveLeft() {
        boolean moves = true; /* verifies if the offset can move */

        try {
            RandomAccessFile mapFile = new RandomAccessFile(super.getFile(), Constants.READ_MODE);

            while (offset > 0 && offset < size && blocks > 0 && moves) { /* verify moving to the left */
                mapFile.seek(offset - 1);
                char first = (char) mapFile.readByte(); /* read a character before the sequence */
                char second = (char) mapFile.readByte(); /* read the first character of the sequence */
                if (Constants.DELIMITERS.indexOf(first) == -1 ||
                        Constants.DELIMITERS.indexOf(second) != -1) {
                    /* if the fragment starts in the middle of a word or with a sequence of delimiters */
                    offset++;
                    blocks--;
                } else {
                    moves = false; /* not in the middle of a word or on a delimiter anymore */
                }
            }

            mapFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Moving the end of the fragment to the right if the fragment ends in the middle of a word
     */
    private void moveRight() {
        boolean moves = true; /* verifies if the offset can move */

        try {
            RandomAccessFile mapFile = new RandomAccessFile(super.getFile(), Constants.READ_MODE);

            while (offset + blocks- 2 < size && moves) { /* verify moving to the right */
                mapFile.seek(Math.min(offset + blocks - 1, size - 2));
                char first = (char) mapFile.readByte(); /* read a character at the end of sequence */
                char second = (char) mapFile.readByte(); /* read a character right after the sequence */
                if (Constants.DELIMITERS.indexOf(first) == -1 && Constants.DELIMITERS.indexOf(second) == -1) {
                    /* if the fragment ends in the middle of a word or with a sequence of delimiters */
                    blocks++;
                } else {
                    moves = false; /* not in the middle of a word anymore */
                }
            }

            mapFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Complete all map operations after adjusting the fragment
     */
    private void getMapAndList() {
        int maxSize = 0; /* maximum size of a word in this fragment */
        byte[] sequence = new byte[blocks]; /* sequence of bytes read from the file */

        try {
            RandomAccessFile mapFile = new RandomAccessFile(super.getFile(), Constants.READ_MODE);
            mapFile.seek(offset); /* go to offset */
            mapFile.read(sequence); /* and read fragment */

            String text = new String(sequence, StandardCharsets.UTF_8); /* cast bytes to text */
            String[] tokens = text.split(Constants.REGEX); /* split text into words by a words-regex */

            for (String t : tokens) {
                if (t.length() != 0) { /* no empty words */
                    numWords++; /* every word increases the number of words */

                    int len = t.length(); /* get size of word */
                    if (map.containsKey(len)) { /* verify if en entry with this size exists in the map */
                        /* if entry exists, update value by 1(one more word with this size) */
                        int app = map.get(len);
                        map.replace(len, app + 1);
                    } else {
                        /* if entry does not exist, set value to be 1(this current word has this size) */
                        map.put(len, 1);
                    }

                    if (len > maxSize) { /* verify if this word is the biggest yet */
                        longestWords.clear(); /* clear the list of old biggest words */
                        longestWords.add(t); /* add the current word */
                        maxSize = len; /* update the maximum size of a word in this segment of the file */
                    } else if (len == maxSize) {
                        longestWords.add(t); /* update the list of the longest words with this word of the same size */
                    }
                }
            }

            mapFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finishes the task, updates reduce tasks and submits them to the pool if all map tasks ended
     */
    public void createReduceTaskAndGo() {
        int tasks = super.getInMapQueue().decrementAndGet(); /* signal this task is ending */
        /* update the corresponding reduce task for this file */
        super.getParser().addWaitingTask(new Result(super.getFile(), map, longestWords, numWords));

        synchronized (super.getTpe()) {
            if (tasks == 0) { /* if all map tasks finished, submit reduce tasks to the pool of workers */
                super.getParser().goWaitingTasks();
            }
        }
    }

    /**
     * Running function for the task
     */
    @Override
    public void run() {
        moveLeft(); /* adjust fragment size */
        moveRight(); /* adjust fragment size */
        getMapAndList(); /* calculate result for this task */
        createReduceTaskAndGo(); /* finish task */
    }
}
