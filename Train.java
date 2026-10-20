import java.util.ArrayList;

// A train is a list of carriages at a station. Index 0 is the front.
public class Train {

    private int id;
    private String station;
    private ArrayList<Carriage> carriages = new ArrayList<Carriage>();

    public Train(int id, String station) {
        this.id = id;
        this.station = station;
    }

    public int getId() {
        return id;
    }

    public String getStation() {
        return station;
    }

    public ArrayList<Carriage> getCarriages() {
        return carriages;
    }

    public int size() {
        return carriages.size();
    }

    public boolean isEmpty() {
        return carriages.isEmpty();
    }

    // returns position starting from 1 (front)
    public int positionOf(Carriage c) {
        return carriages.indexOf(c) + 1;
    }

    public void addCarriage(Carriage c, int index) {
        carriages.add(index, c);
        c.setTrain(this);
    }

    public void removeCarriage(Carriage c) {
        carriages.remove(c);
        c.setTrain(null);
    }

    // moving the train moves all its carriages too
    public void moveTo(String newStation) {
        station = newStation;
        for (Carriage c : carriages) {
            c.setStation(newStation);
        }
    }

    public String summary() {
        String s = "Train " + id + " at " + station + " - " + carriages.size() + " carriage(s): ";
        for (int i = 0; i < carriages.size(); i++) {
            if (i > 0) {
                s += ", ";
            }
            s += carriages.get(i).getId();
        }
        return s;
    }

    public String toString() {
        return summary();
    }
}
