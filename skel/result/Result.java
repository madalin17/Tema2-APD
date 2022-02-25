package result;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class Result {
    private final String file; /* file to operate on */
    private final HashMap<Integer, Integer> map; /* map of words' size and the number of words with that size */
    private final List<String> longestWords; /* list of the longest words */
    private final int numWords; /* the number of words in this segment of the file */

    /**
     * Constructor for the result of the map task for a block in the respective file
     * @param file file to operate on
     * @param map map of words size and the number of words with that size
     * @param longestWords list of the longest words
     * @param numWords the number of words in this segment of the file
     */
    public Result(final String file,
                  final HashMap<Integer, Integer> map,
                  final List<String> longestWords,
                  final int numWords) {
        this.file = file;
        this.map = map;
        this.longestWords = longestWords;
        this.numWords = numWords;
    }

    /**
     * Getter for the file to operate on, used to update corresponding reduce task by the main thread
     * @return file name
     */
    public String getFile() {
        return file;
    }

    /**
     * Getter for the map, used to update corresponding reduce task by the main thread
     * @return a map of words' size and the number of words with that size in this segment of the file
     */
    public HashMap<Integer, Integer> getMap() {
        return map;
    }

    /**
     * Getter for the list, used to update corresponding reduce task by the main thread
     * @return a list of the longest words in this segment of the file
     */
    public List<String> getLongestWords() {
        return longestWords;
    }

    /**
     * Getter for the number of words, used to update corresponding reduce task by the main thread
     * @return the number of words in this segment of the file
     */
    public int getNumWords() {
        return numWords;
    }
}
