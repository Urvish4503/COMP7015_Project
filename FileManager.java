import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Saves and loads the railway to a text file.
// Format:
//   TRAINSYSTEM-SAVE 1
//   CARRIAGE|id|station|configuration
//   TRAIN|trainId|station|carriageId,carriageId,...
public class FileManager {

    public static final String HEADER = "TRAINSYSTEM-SAVE 1";

    public static void save(Railway railway, String filename) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(filename));
        out.println(HEADER);
        for (Carriage c : railway.getAllCarriages()) {
            out.println("CARRIAGE|" + c.getId() + "|" + c.getStation() + "|" + c.getConfiguration());
        }
        for (Train t : railway.getAllTrains()) {
            String ids = "";
            for (Carriage c : t.getCarriages()) {
                if (!ids.isEmpty()) {
                    ids += ",";
                }
                ids += c.getId();
            }
            out.println("TRAIN|" + t.getId() + "|" + t.getStation() + "|" + ids);
        }
        out.close();
        railway.setSaved();
    }

    // Loads into a new Railway, so if the file is bad the current one is not changed
    public static Railway load(String filename, StationList stations) throws IOException, SaveFileException {
        Railway railway = new Railway(stations);
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        // train lines are kept until the end so all carriages exist first
        ArrayList<String[]> trainLines = new ArrayList<String[]>();
        ArrayList<Integer> trainLineNumbers = new ArrayList<Integer>();

        try {
            String line = reader.readLine();
            if (line == null || !line.trim().equals(HEADER)) {
                throw new SaveFileException(1, "this is not a TrainSystem save file.");
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\|", 4);
                if (parts.length != 4) {
                    throw new SaveFileException(lineNumber, "expected 4 fields separated by '|'.");
                }

                if (parts[0].equals("CARRIAGE")) {
                    int id = parseNumber(parts[1], lineNumber);
                    try {
                        railway.addCarriage(id, parts[3], parts[2]);
                    } catch (IllegalArgumentException e) {
                        throw new SaveFileException(lineNumber, e.getMessage());
                    }
                } else if (parts[0].equals("TRAIN")) {
                    trainLines.add(parts);
                    trainLineNumbers.add(lineNumber);
                } else {
                    throw new SaveFileException(lineNumber, "unknown record type '" + parts[0] + "'.");
                }
            }
        } finally {
            reader.close();
        }

        // now build the trains
        for (int i = 0; i < trainLines.size(); i++) {
            String[] parts = trainLines.get(i);
            int lineNumber = trainLineNumbers.get(i);
            int trainId = parseNumber(parts[1], lineNumber);

            ArrayList<Carriage> cars = new ArrayList<Carriage>();
            for (String s : parts[3].split(",")) {
                int carriageId = parseNumber(s, lineNumber);
                Carriage c = railway.findCarriage(carriageId);
                if (c == null) {
                    throw new SaveFileException(lineNumber, "carriage " + carriageId + " does not exist.");
                }
                cars.add(c);
            }

            try {
                railway.loadTrain(trainId, parts[2], cars);
            } catch (RuntimeException e) {
                throw new SaveFileException(lineNumber, e.getMessage());
            }
        }

        railway.setSaved();
        return railway;
    }

    private static int parseNumber(String text, int lineNumber) throws SaveFileException {
        try {
            int n = Integer.parseInt(text.trim());
            if (n <= 0) {
                throw new SaveFileException(lineNumber, "IDs must be positive.");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new SaveFileException(lineNumber, "'" + text + "' is not a valid number.");
        }
    }
}
