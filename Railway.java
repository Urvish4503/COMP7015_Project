import java.util.ArrayList;
import java.util.TreeMap;

// Stores all the carriages and trains and checks the rules
public class Railway {

    private StationList stations;
    // TreeMap so they are always sorted by ID
    private TreeMap<Integer, Carriage> carriages = new TreeMap<Integer, Carriage>();
    private TreeMap<Integer, Train> trains = new TreeMap<Integer, Train>();
    private int nextTrainId = 1;
    private boolean modified = false; // true if there are unsaved changes

    public Railway(StationList stations) {
        this.stations = stations;
    }

    public boolean isModified() {
        return modified;
    }

    public void setSaved() {
        modified = false;
    }

    // ---------- carriages ----------

    public boolean hasCarriage(int id) {
        return carriages.containsKey(id);
    }

    public Carriage findCarriage(int id) {
        return carriages.get(id);
    }

    public Carriage addCarriage(int id, String configuration, String station) {
        if (id <= 0) {
            throw new IllegalArgumentException("Carriage ID must be positive.");
        }
        if (carriages.containsKey(id)) {
            throw new IllegalArgumentException("A carriage with ID " + id + " already exists.");
        }
        if (!stations.contains(station)) {
            throw new IllegalArgumentException("Unknown station '" + station + "'.");
        }
        Carriage c = new Carriage(id, configuration, station);
        carriages.put(id, c);
        modified = true;
        return c;
    }

    public void retireCarriage(Carriage c) {
        if (c.isAttached()) {
            throw new IllegalStateException(c.describe() + " is still in a train.");
        }
        carriages.remove(c.getId());
        modified = true;
    }

    public ArrayList<Carriage> getAllCarriages() {
        return new ArrayList<Carriage>(carriages.values());
    }

    public ArrayList<Carriage> getUnattachedCarriagesAt(String station) {
        ArrayList<Carriage> result = new ArrayList<Carriage>();
        for (Carriage c : carriages.values()) {
            if (!c.isAttached() && c.getStation().equals(station)) {
                result.add(c);
            }
        }
        return result;
    }

    // stations where a new train could be formed
    public ArrayList<String> getStationsWithUnattachedCarriages() {
        ArrayList<String> result = new ArrayList<String>();
        for (String station : stations.getNames()) {
            if (getUnattachedCarriagesAt(station).size() > 0) {
                result.add(station);
            }
        }
        return result;
    }

    // ---------- trains ----------

    public ArrayList<Train> getAllTrains() {
        return new ArrayList<Train>(trains.values());
    }

    // trains that have free carriages at their station
    public ArrayList<Train> getTrainsWithAvailableCarriages() {
        ArrayList<Train> result = new ArrayList<Train>();
        for (Train t : trains.values()) {
            if (getUnattachedCarriagesAt(t.getStation()).size() > 0) {
                result.add(t);
            }
        }
        return result;
    }

    public Train formTrain(Carriage first) {
        Train t = new Train(nextTrainId, first.getStation());
        nextTrainId++;
        t.addCarriage(first, 0);
        trains.put(t.getId(), t);
        modified = true;
        return t;
    }

    public void addCarriageToTrain(Train t, Carriage c, int index) {
        if (c.isAttached()) {
            throw new IllegalStateException(c.describe() + " is already in a train.");
        }
        if (!c.getStation().equals(t.getStation())) {
            throw new IllegalStateException(c.describe() + " is at " + c.getStation()
                    + " but Train " + t.getId() + " is at " + t.getStation() + ".");
        }
        t.addCarriage(c, index);
        modified = true;
    }

    // returns true if the train was deleted because it became empty
    public boolean removeCarriageFromTrain(Train t, Carriage c) {
        t.removeCarriage(c);
        modified = true;
        if (t.isEmpty()) {
            trains.remove(t.getId());
            return true;
        }
        return false;
    }

    public void moveTrain(Train t, String destination) {
        if (!stations.contains(destination)) {
            throw new IllegalArgumentException("Unknown station '" + destination + "'.");
        }
        if (destination.equals(t.getStation())) {
            throw new IllegalArgumentException("Train " + t.getId() + " is already at " + destination + ".");
        }
        t.moveTo(destination);
        modified = true;
    }

    // used when loading a save file, keeps the saved train ID
    public void loadTrain(int trainId, String station, ArrayList<Carriage> cars) {
        if (trains.containsKey(trainId)) {
            throw new IllegalArgumentException("Train ID " + trainId + " is used more than once.");
        }
        if (!stations.contains(station)) {
            throw new IllegalArgumentException("Unknown station '" + station + "'.");
        }
        Train t = new Train(trainId, station);
        for (Carriage c : cars) {
            addCarriageToTrain(t, c, t.size());
        }
        trains.put(trainId, t);
        if (trainId >= nextTrainId) {
            nextTrainId = trainId + 1;
        }
    }
}
