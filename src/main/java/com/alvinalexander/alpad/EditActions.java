package com.alvinalexander.alpad;

public class EditActions {

    private static final String TAB        = "  ";
    private static final int    TAB_LENGTH = TAB.length();
  
    public static String insertIndentAtBeginningOfLine(String originalText) {
        if (originalText == null)  return null;
        if (originalText.indexOf("\n") < 0) return TAB + originalText;
    
        StringBuffer sb = new StringBuffer();
        String[] lines = originalText.split("\n");
        for (String line: lines) {
            sb.append(TAB + line + "\n");
        }
        return sb.toString();
    }
  
    public static String removeIndentFromBeginningOfLine(String originalText) {
        if (originalText == null)  return null;
        StringBuffer sb = new StringBuffer();
        String[] lines = originalText.split("\n");
        int numLines = lines.length;
        int counter = 0;
        for (String line: lines) {
            if (beginsWithIndent(line)) {
                sb.append(line.substring(TAB_LENGTH, line.length()));
                if (counter++ < numLines) sb.append("\n");
            } else {
                sb.append(line);
                if (counter++ < numLines) sb.append("\n");
            }
        }
        return sb.toString();
    } 

    private static boolean beginsWithIndent(String string) {
        if (string == null) return false;
        if (string.length() < TAB.length()) return false;
        for (int i=0; i<TAB_LENGTH; i++) {
            if (string.charAt(i) != TAB.charAt(i)) return false;
        }
        return true;
    }
  
}




