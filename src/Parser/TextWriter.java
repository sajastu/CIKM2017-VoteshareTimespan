package Parser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sajad on 11/27/2016.
 */
public class TextWriter {
    private static String csvFile;
    public static <T> void writeToCsv(HashMap<String, T> input, String file_name) throws IOException {
        csvFile = "out-files/" + file_name + ".csv";
        FileWriter writer = new FileWriter(csvFile,true);

        for (String inp : input.keySet()){
            writeLine(writer, Arrays.asList(inp, String.valueOf(input.get(inp))));
        }
        writer.flush();
        writer.close();
    }

    public static void writeLine(Writer w, List<String> values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value + ",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("\n");
        w.append(sb.toString());
    }

    public static void writeFileHeader() throws IOException {
        FileWriter writer = new FileWriter(csvFile,true);
        writeLine(writer, Arrays.asList("N","Lambda","Query","MAP","p@1","p@5","p@10"));
        writer.flush();
        writer.close();

    }
}
