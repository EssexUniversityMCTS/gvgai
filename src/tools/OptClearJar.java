package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class OptClearJar {
    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
	String dataRuns = "examples/dataRuns.txt";
	String outputPath = "outputs/";

	String[] data = new tools.IO().readFile(dataRuns);
	PrintWriter writer = new PrintWriter(dataRuns, "UTF-8");
	writer.println(data[0]);
	writer.println("current runs: 0");
	writer.close();

	File[] files = new File(outputPath).listFiles();
	for (int i = 0; i < files.length; i++) {
	    files[i].delete();
	}
    }
}
