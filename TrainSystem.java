import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// Train Management System - main class with the menus
public class TrainSystem {

    private StationList stations;
    private Railway railway;
    private InputHelper input = new InputHelper();

    public TrainSystem(StationList stations) {
        this.stations = stations;
        this.railway = new Railway(stations);
    }

    public static void main(String[] args) {
        String stationsFile = "stations.txt";
        if (args.length > 0) {
            stationsFile = args[0];
        }

        StationList stations = null;
        try {
            stations = new StationList(stationsFile);
        } catch (IOException e) {
            System.out.println("Error: could not load stations from '" + stationsFile + "': " + e.getMessage());
            System.exit(1);
        }
        System.out.println("Loaded " + stations.size() + " stations from '" + stationsFile + "'.");

        TrainSystem system = new TrainSystem(stations);
        system.run();
    }

    public void run() {
        System.out.println();
        System.out.println("=== COMP7015 Train Management System ===");
        boolean running = true;
        while (running) {
            printMenu();
            int choice = input.readInt("Enter choice (0-10): ", 0, 10);
            System.out.println();
            try {
                switch (choice) {
                    case 1: addCarriage(); break;
                    case 2: retireCarriage(); break;
                    case 3: formOrExtendTrain(); break;
                    case 4: removeCarriagesFromTrain(); break;
                    case 5: moveTrain(); break;
                    case 6: listTrains(); break;
                    case 7: findCarriage(); break;
                    case 8: listCarriages(); break;
                    case 9: saveState(); break;
                    case 10: loadState(); break;
                    case 0: running = !confirmExit(); break;
                }
            } catch (RuntimeException e) {
                // just in case something goes wrong, go back to the menu
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println();
        if (railway.isModified()) {
            System.out.println("---------- Main Menu ---------- (unsaved changes)");
        } else {
            System.out.println("---------- Main Menu ----------");
        }
        System.out.println("  1. Add a new carriage");
        System.out.println("  2. Retire a carriage");
        System.out.println("  3. Form a train / add carriages to a train");
        System.out.println("  4. Remove carriages from a train");
        System.out.println("  5. Move a train to another station");
        System.out.println("  6. List trains");
        System.out.println("  7. Find which train a carriage belongs to");
        System.out.println("  8. List all carriages");
        System.out.println("  9. Save to a file");
        System.out.println(" 10. Load from a file");
        System.out.println("  0. Exit");
    }

    // Option 1
    private void addCarriage() {
        System.out.println("--- Add a new carriage ---");
        int id;
        while (true) {
            id = input.readAnyInt("Enter the new carriage ID (0 to cancel): ");
            if (id == 0) {
                System.out.println("Cancelled.");
                return;
            }
            if (id < 0) {
                System.out.println("  Carriage IDs must be positive.");
            } else if (railway.hasCarriage(id)) {
                System.out.println("  A carriage with ID " + id + " already exists.");
            } else {
                break;
            }
        }

        String config = input.readNonEmptyLine("Enter the configuration (e.g. toilet, bike-rack): ");

        int s = input.chooseFromMenu("Choose the starting station:", stations.getNames(), "Cancel");
        if (s < 0) {
            System.out.println("Cancelled.");
            return;
        }

        Carriage c = railway.addCarriage(id, config, stations.get(s));
        System.out.println("Added " + c + ".");
    }

    // Option 2
    private void retireCarriage() {
        System.out.println("--- Retire a carriage ---");
        ArrayList<Carriage> all = railway.getAllCarriages();
        if (all.isEmpty()) {
            System.out.println("There are no carriages.");
            return;
        }
        int index = input.chooseFromMenu("Select the carriage to retire:", carriageLabels(all), "Cancel");
        if (index < 0) {
            System.out.println("Cancelled.");
            return;
        }
        Carriage c = all.get(index);

        if (c.isAttached()) {
            // has to be taken out of the train first
            Train t = c.getTrain();
            System.out.println(c.describe() + " is part of Train " + t.getId() + ".");
            if (!input.readYesNo("Remove it from the train and retire it? (y/n): ")) {
                System.out.println("The carriage was not retired.");
                return;
            }
            if (railway.removeCarriageFromTrain(t, c)) {
                System.out.println("Train " + t.getId() + " had no carriages left and was removed.");
            }
        } else if (!input.readYesNo("Retire " + c.describe() + "? (y/n): ")) {
            System.out.println("The carriage was not retired.");
            return;
        }

        railway.retireCarriage(c);
        System.out.println(c.describe() + " has been retired.");
    }

    // Option 3
    private void formOrExtendTrain() {
        System.out.println("--- Form a train / add carriages ---");
        int choice = input.chooseFromMenu("What would you like to do?",
                Arrays.asList("Form a new train", "Add carriages to an existing train"), "Back");
        if (choice == 0) {
            formNewTrain();
        } else if (choice == 1) {
            extendTrain();
        }
    }

    private void formNewTrain() {
        ArrayList<String> stationOptions = railway.getStationsWithUnattachedCarriages();
        if (stationOptions.isEmpty()) {
            System.out.println("There are no unattached carriages at any station.");
            return;
        }

        int s = input.chooseFromMenu("Choose the station to form the train at:", stationOptions, "Cancel");
        if (s < 0) {
            System.out.println("Cancelled.");
            return;
        }
        String station = stationOptions.get(s);

        ArrayList<Carriage> free = railway.getUnattachedCarriagesAt(station);
        int c = input.chooseFromMenu("Select the front carriage:", carriageLabels(free), "Cancel");
        if (c < 0) {
            System.out.println("Cancelled.");
            return;
        }

        Train t = railway.formTrain(free.get(c));
        System.out.println("Formed Train " + t.getId() + " at " + station + ".");
        addCarriages(t);
    }

    private void extendTrain() {
        ArrayList<Train> options = railway.getTrainsWithAvailableCarriages();
        if (options.isEmpty()) {
            System.out.println("No train has unattached carriages at its station.");
            return;
        }
        int index = input.chooseFromMenu("Select the train:", trainLabels(options), "Cancel");
        if (index < 0) {
            System.out.println("Cancelled.");
            return;
        }
        addCarriages(options.get(index));
    }

    // keep adding carriages until the user is done
    private void addCarriages(Train t) {
        while (true) {
            ArrayList<Carriage> free = railway.getUnattachedCarriagesAt(t.getStation());
            if (free.isEmpty()) {
                System.out.println("No more unattached carriages at " + t.getStation() + ".");
                break;
            }
            System.out.println();
            System.out.println("Current: " + t.summary());
            int index = input.chooseFromMenu("Select a carriage to add:", carriageLabels(free), "Done");
            if (index < 0) {
                break;
            }
            Carriage c = free.get(index);
            int last = t.size() + 1;
            int pos = input.readInt("Position (1 = front, " + last + " = back): ", 1, last);
            railway.addCarriageToTrain(t, c, pos - 1);
            System.out.println("Added " + c.describe() + " at position " + pos + ".");
        }
        System.out.println("Train is now: " + t.summary());
    }

    // Option 4
    private void removeCarriagesFromTrain() {
        System.out.println("--- Remove carriages from a train ---");
        Train t = chooseTrain("Select the train:");
        if (t == null) {
            return;
        }
        while (true) {
            System.out.println();
            System.out.println("Current: " + t.summary());
            ArrayList<Carriage> cars = new ArrayList<Carriage>(t.getCarriages());
            int index = input.chooseFromMenu("Select a carriage to remove:", carriageLabels(cars), "Done");
            if (index < 0) {
                return;
            }
            Carriage c = cars.get(index);
            boolean deleted = railway.removeCarriageFromTrain(t, c);
            System.out.println("Removed " + c.describe() + ", it stays at " + c.getStation() + ".");
            if (deleted) {
                System.out.println("Train " + t.getId() + " has no carriages left and was removed.");
                return;
            }
        }
    }

    // Option 5
    private void moveTrain() {
        System.out.println("--- Move a train ---");
        Train t = chooseTrain("Select the train to move:");
        if (t == null) {
            return;
        }
        // don't show the station it is already at
        ArrayList<String> destinations = new ArrayList<String>(stations.getNames());
        destinations.remove(t.getStation());
        if (destinations.isEmpty()) {
            System.out.println("There is nowhere else to move the train.");
            return;
        }
        int index = input.chooseFromMenu("Choose the destination:", destinations, "Cancel");
        if (index < 0) {
            System.out.println("Cancelled.");
            return;
        }
        String from = t.getStation();
        railway.moveTrain(t, destinations.get(index));
        System.out.println("Train " + t.getId() + " moved from " + from + " to " + t.getStation() + ".");
    }

    // Option 6
    private void listTrains() {
        System.out.println("--- Trains ---");
        ArrayList<Train> trains = railway.getAllTrains();
        if (trains.isEmpty()) {
            System.out.println("There are no trains.");
            return;
        }
        while (true) {
            int index = input.chooseFromMenu("Select a train to see its carriages:", trainLabels(trains), "Back");
            if (index < 0) {
                return;
            }
            Train t = trains.get(index);
            System.out.println();
            System.out.println("Train " + t.getId() + " at " + t.getStation() + ", front to back:");
            for (int i = 0; i < t.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + t.getCarriages().get(i).describe());
            }
            System.out.println();
        }
    }

    // Option 7
    private void findCarriage() {
        System.out.println("--- Find a carriage ---");
        int id = input.readAnyInt("Enter the carriage ID: ");
        Carriage c = railway.findCarriage(id);
        if (c == null) {
            System.out.println("No carriage with ID " + id + " exists.");
        } else if (c.isAttached()) {
            Train t = c.getTrain();
            System.out.println(c.describe() + " is in Train " + t.getId() + " (position "
                    + t.positionOf(c) + ") at " + t.getStation() + ".");
        } else {
            System.out.println(c.describe() + " is not in a train. It is at " + c.getStation() + ".");
        }
    }

    // Option 8
    private void listCarriages() {
        System.out.println("--- All carriages ---");
        ArrayList<Carriage> all = railway.getAllCarriages();
        if (all.isEmpty()) {
            System.out.println("There are no carriages.");
            return;
        }
        System.out.printf("%-8s %-20s %-20s %s%n", "ID", "Configuration", "Station", "Train");
        for (Carriage c : all) {
            String train = "-";
            if (c.isAttached()) {
                train = "Train " + c.getTrain().getId();
            }
            System.out.printf("%-8s %-20s %-20s %s%n", c.getId(), c.getConfiguration(), c.getStation(), train);
        }
    }

    // Option 9 - returns true if it saved
    private boolean saveState() {
        System.out.println("--- Save ---");
        String filename = input.readLine("Enter a file name (blank to cancel): ");
        if (filename.isEmpty()) {
            System.out.println("Cancelled.");
            return false;
        }
        File file = new File(filename);
        if (file.exists() && !input.readYesNo("File already exists. Overwrite? (y/n): ")) {
            System.out.println("Nothing was saved.");
            return false;
        }
        try {
            FileManager.save(railway, filename);
            System.out.println("Saved to '" + filename + "'.");
            return true;
        } catch (IOException e) {
            System.out.println("Error: could not save to '" + filename + "'.");
            return false;
        }
    }

    // Option 10
    private void loadState() {
        System.out.println("--- Load ---");
        if (railway.isModified() && !input.readYesNo("You have unsaved changes. Continue? (y/n): ")) {
            System.out.println("Cancelled.");
            return;
        }
        String filename = input.readLine("Enter the file name (blank to cancel): ");
        if (filename.isEmpty()) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            railway = FileManager.load(filename, stations);
            System.out.println("Loaded " + railway.getAllCarriages().size() + " carriage(s) and "
                    + railway.getAllTrains().size() + " train(s).");
        } catch (FileNotFoundException e) {
            System.out.println("Error: the file '" + filename + "' was not found.");
        } catch (IOException e) {
            System.out.println("Error: could not read '" + filename + "'.");
        } catch (SaveFileException e) {
            System.out.println("Error: invalid save file (" + e.getMessage() + ")");
        }
    }

    // Option 0 - returns true if the program should exit
    private boolean confirmExit() {
        if (!railway.isModified()) {
            return true;
        }
        int choice = input.chooseFromMenu("You have unsaved changes.",
                Arrays.asList("Save and exit", "Exit without saving"), "Back to menu");
        if (choice == 0) {
            return saveState();
        }
        return choice == 1;
    }

    private Train chooseTrain(String title) {
        ArrayList<Train> trains = railway.getAllTrains();
        if (trains.isEmpty()) {
            System.out.println("There are no trains.");
            return null;
        }
        int index = input.chooseFromMenu(title, trainLabels(trains), "Cancel");
        if (index < 0) {
            System.out.println("Cancelled.");
            return null;
        }
        return trains.get(index);
    }

    private ArrayList<String> carriageLabels(ArrayList<Carriage> list) {
        ArrayList<String> labels = new ArrayList<String>();
        for (Carriage c : list) {
            labels.add(c.toString());
        }
        return labels;
    }

    private ArrayList<String> trainLabels(ArrayList<Train> list) {
        ArrayList<String> labels = new ArrayList<String>();
        for (Train t : list) {
            labels.add(t.summary());
        }
        return labels;
    }
}
