// Represents a single carriage
public class Carriage {

    private int id;
    private String configuration;
    private String station;
    private Train train; // null if not in a train

    public Carriage(int id, String configuration, String station) {
        this.id = id;
        this.configuration = configuration;
        this.station = station;
        this.train = null;
    }

    public int getId() {
        return id;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public boolean isAttached() {
        return train != null;
    }

    public String describe() {
        return "Carriage " + id + " [" + configuration + "]";
    }

    public String toString() {
        String status;
        if (train == null) {
            status = "unattached";
        } else {
            status = "in Train " + train.getId();
        }
        return describe() + " at " + station + " (" + status + ")";
    }
}
