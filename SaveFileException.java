// Thrown when a save file has bad contents
public class SaveFileException extends Exception {

    public SaveFileException(int lineNumber, String message) {
        super("line " + lineNumber + ": " + message);
    }
}
