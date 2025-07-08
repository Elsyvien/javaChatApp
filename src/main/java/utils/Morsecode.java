package utils;
/*
 * This is a utility class for converting text to Morse code and vice versa.
 * It includes methods to convert strings containing letters, numbers, and punctuation
 * @author Max Staneker, Mia Schienagel
 * @version 0.3.2
 */
public class Morsecode {
    private static final String[] morseAlphabet = {
        ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", 
        ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.",
        "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."
    };

    private static final String[] morseNumbers = {
        "-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."
    };

    private static final String[] morsePunctuation = {
        "--..--", ".-.-.-", "..--..", ".----.", "-....-", "-..-.", "---...", "-.-.-.", "-...-", ".-..-.", ".--.-."
    };

    private static final String[] morseSpecial = {
        " ", "/", "?", "!", "@", "#", "$", "%", "&", "*", "(", ")", "-", "_", "+", "="
    };

    private static final String[] normalAlphabet = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z"
    };

    /*
     * Converts a given string to Morse code.
     * @param input the plain text string to convert, decoded from JSON
     * @return the Morse code representation of the input string
     */

    public static String toMorse(String input) {
        StringBuilder morseCode = new StringBuilder();
        input = input.toUpperCase();
        char[] characters = input.toCharArray(); // Convert the input string to a character array
        for (char c : characters) { // Go over each character in the input string
            if (c >= 'A' && c <= 'Z') { // Convert letters to Morse code
                morseCode.append(morseAlphabet[c - 'A']);
            } else if (c >= '0' && c <= '9') { // Convert numbers to Morse code
                morseCode.append(morseNumbers[c - '0']);
            } else if (c == ' ') {
                morseCode.append(" / ");
            } else {
                // Handle punctuation and special characters if needed
                // For now, just skip unknown characters
                continue;
            }
            morseCode.append(" ");
        }
        return morseCode.toString().trim(); // Return the Morse code string without trailing spaces
    }
}
