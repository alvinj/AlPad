package com.alvinalexander.alpad;

import java.util.StringTokenizer;

public class EditActions
{
    private static final String TAB        = "  ";
    private static final int    TAB_LENGTH = 2;
  
    public static String insertTabAtBeginningOfLine(String originalText) {
        if ( originalText == null )  return null;
        if ( originalText.indexOf("\n") < 0 ) return TAB + originalText;
    
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(originalText,"\n");
        while (st.hasMoreTokens()) {
            sb.append(TAB + st.nextToken() + "\n");
        }
        return sb.toString();
    }
  
  
    public static String removeTabFromBeginningOfLine(String originalText) {
      if (originalText == null)  return null;
      StringBuffer sb = new StringBuffer();
      StringTokenizer st = new StringTokenizer(originalText,"\n");
      int numTokens = st.countTokens();
      while (st.hasMoreTokens()) {
        String nextToken = st.nextToken();
        if (beginsWithTab(nextToken)) {
          sb.append( nextToken.substring(TAB_LENGTH,nextToken.length()) );
          if (numTokens > 1) sb.append("\n");
        } else {
          sb.append(nextToken);
          if (numTokens > 1) sb.append("\n");
        }
      }
      return sb.toString();
  }
 
  private static boolean beginsWithTab(String s) {
    for (int i=0; i<TAB_LENGTH; i++) {
      if (s.charAt(i) != TAB.charAt(i)) return false;
    }
    return true;
  }
  
}




