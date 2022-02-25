import parser.Parser;
import java.io.FileNotFoundException;

public class Tema2 {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        /* Get CLI params */
        int workers = Integer.parseInt(args[0]);
        String file = args[1];
        String outFile = args[2];

        Parser parser = new Parser(workers, file, outFile);
        parser.parseAndAddTasks(); /* parse input file */
        parser.goFirstTasks(); /* start tasks */
    }
}
