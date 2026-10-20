# COMP7015 Train Management System

A Java console program for managing train carriages and trains across a list of stations.

## How to run

```
javac TrainSystem.java
java TrainSystem
```

It reads the stations from `stations.txt` (one station per line). You can also give a different file: `java TrainSystem mystations.txt`.

## Menu options

1. Add a new carriage (ID, configuration, starting station)
2. Retire a carriage (if it's in a train, it gets removed from the train first)
3. Form a new train or add carriages to an existing train
4. Remove carriages from a train (a train with no carriages is deleted)
5. Move a train to another station (its carriages move with it)
6. List trains and see the carriages in each one
7. Find which train a carriage is in
8. List all carriages
9. Save to a file
10. Load from a file
0. Exit

## Classes

- `TrainSystem` - main method and menus
- `Railway` - stores all carriages and trains and checks the rules
- `Train` - a list of carriages at a station (front first)
- `Carriage` - a single carriage
- `StationList` - reads the stations file
- `FileManager` - saving and loading
- `SaveFileException` - error for a bad save file
- `InputHelper` - reads and checks user input

## Save file format

```
TRAINSYSTEM-SAVE 1
CARRIAGE|id|station|configuration
TRAIN|trainId|station|carriageId,carriageId,...
```

Carriage IDs in a TRAIN line go from front to back. See `sample_save.txt` for an example.
