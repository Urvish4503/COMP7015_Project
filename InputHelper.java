import java.util.List;
import java.util.Scanner;

// Helper methods for reading user input safely
public class InputHelper {

    private Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            // no more input (e.g. Ctrl+D)
            System.out.println();
            System.out.println("No more input. Exiting.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    public String readNonEmptyLine(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  Input cannot be empty. Please try again.");
        }
    }

    // keeps asking until a number between min and max is entered
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  That is not a valid number. Please try again.");
            }
        }
    }

    public int readAnyInt(String prompt) {
        return readInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt).toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println("  Please answer y or n.");
        }
    }

    // Prints a numbered menu and returns the index chosen (0-based).
    // Option 0 is the cancel option, which returns -1.
    public int chooseFromMenu(String title, List<String> options, String cancelLabel) {
        System.out.println(title);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }
        System.out.println("  0. " + cancelLabel);
        int choice = readInt("Enter choice (0-" + options.size() + "): ", 0, options.size());
        return choice - 1;
    }
}
