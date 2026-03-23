package Project_OOP;

import java.util.Scanner;

public final class InputHelper {
    private InputHelper() {
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    public static String readOptionalString(Scanner scanner, String prompt, String currentValue) {
        System.out.print(prompt + " [" + currentValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException exception) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt, double minValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value < minValue) {
                    System.out.println("Please enter a value greater than or equal to " + minValue + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException exception) {
                System.out.println("Invalid decimal number. Please try again.");
            }
        }
    }

    public static boolean confirm(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(input) || "yes".equals(input)) {
                return true;
            }
            if ("n".equals(input) || "no".equals(input)) {
                return false;
            }
            System.out.println("Please answer with y or n.");
        }
    }
}
