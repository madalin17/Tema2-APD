package result;

import java.io.File;
import java.util.Locale;

@SuppressWarnings("ClassCanBeRecord")
public final class FinalResult {
    private final String file; /* file to operate on */
    private final float rank; /* rank of file */
    private final int maxLen; /* maximum length of a word */
    private final int numWords; /* number of words of maximum length in the file */

    /**
     * Constructor creating a result after map and reduce operations on a file
     * @param file file to operate on
     * @param rank rank of file
     * @param maxLen maximum length of a word
     * @param numWords number of words of maximum length in the fil
     */
    public FinalResult(final String file,
                       final float rank,
                       final int maxLen,
                       final int numWords) {
        this.file = file;
        this.rank = rank;
        this.maxLen = maxLen;
        this.numWords = numWords;
    }

    /**
     * Getter for the rank, used in sorting the files by rank descending
     * @return rank of the file
     */
    public float getRank() {
        return rank;
    }

    /**
     * Representing an object of this class as a string
     * @return a way to display the results for a file in the output file
     */
    @Override
    public String toString() {
        return new File(file).getName() + "," + String.format(Locale.US, "%.2f", rank) + "," + maxLen + "," + numWords + "\n";
    }
}

