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
        System.out.println("[MESSAGE HANDLING] Encoding to Morse Code: " + input);
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
    public static String fromMorse(String morseCode) {
        System.out.println("[MESSAGE HANDLING] Decoding Morse Code: " + morseCode);
        StringBuilder decodedText = new StringBuilder();
        String[] morseWords = morseCode.split("/"); // Split by word Seperator
        
        for (String word : morseWords) {
            String[] morseChats = word.trim().split(""); // Split by character
            
            for (String morseChar : morseChats) {
                if (morseChar.isEmpty()) {
                    continue; // Skip empty strings
                }

                // Check if the morse character is a letter
                for (int i = 0; i < morseAlphabet.length; i++) {
                    if (morseChar.equals(morseAlphabet[i])) {
                        decodedText.append(normalAlphabet[i]);
                        break;
                    }
                }

                // Check in numbers
                for (int i = 0; i < morseNumbers.length; i++) {
                    if (morseChar.equals(morseNumbers[i])) {
                        decodedText.append(i); // Append the number directly
                        break;
                    }
                }

                // TODO: Check for punctuation for now we just skip it
    
            }
            // Add Space between words if it is not the last word    
            if (!word.equals(morseWords[morseWords.length - 1])) decodedText.append(" ");
        }
        return decodedText.toString().trim(); // Return the decoded text without trailing spaces
    }
}