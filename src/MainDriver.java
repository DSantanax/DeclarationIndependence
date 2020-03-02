import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
/*import java.util.ArrayList;
import java.util.Collections;
import java.util.List;*/
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Stream;

/*
 * Name: Daniel Santana Medina
 * Description:
 *       I tried using the fork join framework to compare
 *       multiple threads vs main thread in IO reading.
 *       Not much of a time difference using multiple threads
 *       may be due to bottleneck of the IO.
 *
 */

public class MainDriver {
    public static void main(String[] args) throws IOException {
        File file = new File("DeclarationIndependence.txt");
        //    int bytes = (int) file.length();
        File fileAdd = new File("backwards.txt");
        //System.out.println("Total bytes: " + bytes);
        BufferedWriter bf = null;
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processor(s): " + processors);


        int lines;
        try (Stream<String> fileStream = Files.lines(Paths.get("DeclarationIndependence.txt"))) {
            lines = (int) fileStream.count();
            System.out.println("Total lines: " + lines);
        }
        String[] allLines = new String[lines];

        //TODO: fix single thread reading
        System.out.println("Reading with main.");
        long time1 = 0;

        for (int i = 0; i < 10; i++) {
            //have to insert stream up top to get total lines
            ReadText rt = new ReadText(0, lines, file, allLines);
            long start = System.nanoTime();
            rt.addToFile();
            bf = new BufferedWriter(new FileWriter(fileAdd));
            //can create or modify to add directly to file for main
            for (String line : allLines) {
                bf.write(line + "\n");
            }
            time1 += (System.nanoTime() - start);
        }
        System.out.println("Total time reading with main: " + time1 / 10);


        System.out.println("Reading with multiple threads.");
        long time = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            String[] list = ReadText.getList(file, lines);

            bf = new BufferedWriter(new FileWriter(fileAdd));
            for (String line : list) {
                bf.write(line + "\n");
            }
            time += (System.nanoTime() - start);
        }
        System.out.println("Total time using multiple threads: " + time / 10);
        bf.close();
    }

}

class ReadText extends RecursiveAction {
    int startLine;
    int endLine;
    File file;
    String[] fileAdd;
    static long threshold = 100;

    ReadText(int start, int end, File file, String[] fileAdd) {
        this.startLine = start;
        this.endLine = end;
        this.file = file;
        this.fileAdd = fileAdd;
    }

    public void addToFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            //read lines
            int start = startLine;
            int count = 0;
            //used to print out where to start and end for threads
//            System.out.println("Start: " + start + " end:" + endLine);
            while (start < endLine) {
                //read at starting line
                if (count < startLine) {
                    br.readLine();
                    count++;
                } else {
                    line = br.readLine();
                    String[] words = line.split(" ");

                    StringBuilder stringBuild = new StringBuilder();
                    //append words backwards
                    for (int i = words.length - 1; i >= 0; i--) {
                        stringBuild.append(words[i]).append(" ");
                    }
                    //Use an array or something to write to that index then return
                    // write at location start to endLine
                    fileAdd[start] = stringBuild.toString();
                    start++;
                }
            }

            br.close();
//                bw.close();
        } catch (FileNotFoundException fe) {
            System.out.println("File does not exist");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void compute() {
        // System.out.println("Current thres: " + (endLine - startLine));
        if (endLine - startLine < threshold) {
//            System.out.println("Starting thread");
            addToFile();
        } else {
            int mid = (endLine + startLine) / 2;
            invokeAll(new ReadText(startLine, mid, file, fileAdd), new ReadText(mid, endLine, file, fileAdd));
        }
    }

    public static String[] getList(File file, int lines) {
        String[] dest = new String[lines];

        ReadText read = new ReadText(0, lines, file, dest);
        ForkJoinPool fjp = new ForkJoinPool();
        fjp.invoke(read);
        //close pool
        fjp.shutdown();

        return dest;
    }
}

