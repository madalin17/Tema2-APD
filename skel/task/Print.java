package task;

import result.FinalResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("ClassCanBeRecord")
public final class Print implements Runnable {
    private final ExecutorService tpe; /* pool of tasks */
    private final List<FinalResult> results; /* list of results after the reduce tasks */
    private final String outFile; /* file where the output is written */

    /**
     * Constructor for a print task
     * @param tpe the pool of tasks
     * @param results the list of results after the reduce tasks
     * @param outFile the file where the output is written
     */
    public Print(final ExecutorService tpe,
                 final List<FinalResult> results,
                 final String outFile) {
        this.tpe = tpe;
        this.results = results;
        this.outFile = outFile;
    }

    /**
     * Running function for the task
     */
    @Override
    public void run() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            /* sort files by rank, descending */
            results.sort(Comparator.comparing(FinalResult::getRank).reversed());

            for (FinalResult result : results) {
                writer.write(result.toString()); /* print each file information to the output file */
            }
            writer.close(); /* close the writing file */
            tpe.shutdown(); /* shutdown the pool service managing the pool of tasks */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
