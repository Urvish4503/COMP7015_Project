import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// Holds the list of stations read from the stations file
public class StationList {

    private ArrayList<String> names = new ArrayList<String>();

    public StationList(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // skip blank lines and duplicates
            if (!line.isEmpty() && !names.contains(line)) {
                names.add(line);
            }
        }
        reader.close();

        if (names.isEmpty()) {
            throw new IOException("the file contains no station names");
        }
    }

    public int size() {
        return names.size();
    }

    public String get(int index) {
        return names.get(index);
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public boolean contains(String name) {
        return names.contains(name);
    }
}
