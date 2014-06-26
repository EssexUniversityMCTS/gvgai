package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:56 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class IO {

	/**
	 * Reads a file and returns its content as a String[]
	 * 
	 * @param filename
	 *            file to read
	 * @return file content as String[], one line per element
	 */
	public String[] readFile(String filename) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            try (FileReader in1 = new FileReader(filename); BufferedReader in = new BufferedReader(in1)) {
                String line;
                while (null != (line = in.readLine())) lines.add(line);
                in.close();
            }
        } catch (Exception e) {
            System.out.println("Error reading the file " + filename + ": " + e);
            e.printStackTrace();
            return null;
        }
        return lines.toArray(new String[lines.size()]);
    }
}
