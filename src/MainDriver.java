import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
/*import java.util.ArrayList;
import java.util.Collections;
import java.util.List;*/
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Stream;

public class MainDriver {
    public static void main(String[] args) throws IOException {
        File file = new File("DeclarationIndependence.txt");
        int bytes = (int) file.length();
        File fileAdd = new File("backwards.txt");
        System.out.println("Total bytes: " + bytes);
//        ReadText readText = new ReadText(0, bytes, file, fileAdd);
//        long time = 0;
//        for(int i = 0; i < 10; i++) {
//            long start = System.nanoTime();
//            readText.addToFile();
//            time += (System.nanoTime()-start);
//            System.out.println(System.nanoTime() - start);
//        }
//        System.out.println(time/10);


//        int processors = Runtime.getRuntime().availableProcessors();
//        System.out.println("Available processor(s): " + processors);
//        int lines;
//        try (Stream<String> fileStream = Files.lines(Paths.get("DeclarationIndependence.txt"))) {
//            lines = (int) fileStream.count();
//            System.out.println("Total lines: " + lines);
//        }
//        String[] dest = new String[lines];
//
//        ReadText read = new ReadText(0, lines, file, dest);
//        ForkJoinPool fjp = new ForkJoinPool();
//        fjp.invoke(read);
//        //close pool
//        fjp.shutdown();
//
//        while(!fjp.isTerminated()){}
//
//
//
        System.out.println("Reading with multiple threads!");
        long start = System.nanoTime();
        String[] list = ReadText.getList(file);

        BufferedWriter bf = new BufferedWriter(new FileWriter(fileAdd));
        for (String line : list) {
            bf.write(line + "\n");
        }
        System.out.println(System.nanoTime() - start);
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
//            BufferedWriter bw = new BufferedWriter(new FileWriter(fileAdd, true));
            //TODO: get line number to start and to end and insert at that location
            String line;
            //read lines

            int start = startLine;
            int count = 0;
            System.out.println("Start: " + start + " end:" + endLine);
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
                    fileAdd[start] = stringBuild.toString();
                    start++;
                    //TODO: write at location start to endLine
                    //Use an array or something to write to that index then return
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
        System.out.println("Current thres: " + (endLine - startLine));
        if (endLine - startLine < threshold) {
            System.out.println("Starting thread");
            addToFile();
        } else {
            int mid = (endLine + startLine) / 2;
            invokeAll(new ReadText(startLine, mid, file, fileAdd), new ReadText(mid, endLine, file, fileAdd));
        }
    }

    public static String[] getList(File file) throws IOException {

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processor(s): " + processors);
        int lines;
        try (Stream<String> fileStream = Files.lines(Paths.get("DeclarationIndependence.txt"))) {
            lines = (int) fileStream.count();
            System.out.println("Total lines: " + lines);
        }
        String[] dest = new String[lines];

        ReadText read = new ReadText(0, lines, file, dest);
        ForkJoinPool fjp = new ForkJoinPool();
        fjp.invoke(read);
        //close pool
        fjp.shutdown();

        return dest;
    }
}

