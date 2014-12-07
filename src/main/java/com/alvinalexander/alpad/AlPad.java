package com.alvinalexander.alpad;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.prefs.*;

import javax.swing.text.Element;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

import javax.swing.undo.*;

/**
 * Notes:
 *     - almost all of the code is in one file because i didn't expect it to grow this large
 *     - i'm experimenting with "global" variable names, in a way similar to the Hungarian Notation discussed in this article:
 *           http://www.joelonsoftware.com/articles/Wrong.html
 *     - see the ToDo.md file for a list of bugs
 */
public class AlPad {

    // mac stuff
    Application thisApp = Application.getApplication();
    
    // prefs
    private Preferences gPrefs = Preferences.userNodeForPackage(this.getClass());
    private static final String HEIGHT    = "HEIGHT";
    private static final String WIDTH     = "WIDTH";
    private static final String LAST_X0 = "LAST_X0";
    private static final String LAST_Y0 = "LAST_Y0";
    
    // constants
    private static final int TAB_KEY_CODE = 9;
    private static final String TAB_AS_SPACES = "  ";

    // state
    private boolean gIsDirty = false;
    private int gCurrentRow = 0;
    private int gCurrentCol = 0;

    // font size
    // handle several different possible keystrokes to increase the font size
    private Action gDecreaseFontSizeAction;
    private Action gIncreaseFontSizeAction;
    private static final KeyStroke gDecreaseFontSizeKeystroke  = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.META_MASK);
    private static final KeyStroke gIncreaseFontSizeKeystroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.META_MASK);
    private static final KeyStroke gIncreaseFontSizeKeystroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.META_MASK + Event.SHIFT_MASK);
    private static final KeyStroke gIncreaseFontSizeKeystroke3 = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Event.META_MASK);

    // undo/redo
    private UndoManager gUndoManager = new UndoManager();
    private UndoAction gUndoAction = new UndoAction(gUndoManager);
    private RedoAction gRedoAction = new RedoAction(gUndoManager);
    private UndoableEditListener gUndoHandler = new UndoHandler(gUndoManager, gUndoAction, gRedoAction);
    
    private static final KeyStroke gUndoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.META_MASK);
    private static final KeyStroke gRedoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.META_MASK);    
    
    // "tabs to spaces" action
    private Action gTabsToSpacesAction = null;
    private static final KeyStroke gTabsToSpacesKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.META_MASK);

    // tab-related actions
    // 'next' and 'previous' keystrokes are the same as web browsers
    private Action gNewTabAction      = null;
    private Action gRenameTabAction   = null;
    private Action gCloseTabAction    = null;
    private Action gNextTabAction     = null;
    private Action gPreviousTabAction = null;
    private static final KeyStroke gNewTabKeystroke      = KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.META_MASK);
    private static final KeyStroke gRenameTabKeystroke   = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.META_MASK);
    private static final KeyStroke gCloseTabKeystroke    = KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.META_MASK);
    private static final KeyStroke gNextTabKeystroke     = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.META_MASK + Event.ALT_MASK);
    private static final KeyStroke gPreviousTabKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.META_MASK + Event.ALT_MASK);

    // TODO i should show memory use stats rather than running the gc
    // keystroke to run the garbage collector
    private Action gRunGarbageCollectorAction = null;
    private static final KeyStroke gRunGarbageCollectorKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.META_MASK);

    // the two main objects
    private JFrame gMainFrame = new JFrame("AlPad");
    private final JTabbedPane gTabbedPane = new JTabbedPane();
    
    public AlPad() {
        finishConfiguringUndoRedoActions();
        configureMainFrame(gMainFrame);
        createTextPaneInFirstTab();
        
        // TODO fix this: can't create the menu bar until the actions are created; they aren't created
        // until the first textarea is created.
        gMainFrame.setJMenuBar(createMenuBar());

        makeFrameVisible(gMainFrame);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new AlPad();
                } catch (Throwable t) {
                    // this might help keep the app alive in the event of something bad,
                    // which will hopefully give me enough time to copy what i have in
                    // the text areas.
                }
            }
        });
    }
    
    private void finishConfiguringUndoRedoActions() {
        gUndoAction.setRedoAction(gRedoAction);
        gRedoAction.setUndoAction(gUndoAction);
    }
  
    /**
     * do everything necessary to configure the mainframe
     */
    private void configureMainFrame(JFrame frame) {
        frame.getContentPane().setLayout(new BorderLayout());
        setMainFrameSize(frame);
        setMainFrameLocation(frame);
        frame.addComponentListener(new MainFrameWindowListener(this));
        configureQuitHandler();
    }

    private void setMainFrameSize(JFrame frame) {
        int lastHeight = gPrefs.getInt(HEIGHT, 600);
        int lastWidth = gPrefs.getInt(WIDTH, 500);
        frame.setPreferredSize(new Dimension(lastWidth, lastHeight));
    }

    private void setMainFrameLocation(JFrame frame) {
        int lastX0 = gPrefs.getInt(LAST_X0, 0);
        int lastY0 = gPrefs.getInt(LAST_Y0, 0);
        if (lastX0 == 0 && lastY0 == 0) {
            frame.setLocationRelativeTo(null);
        } else {
            frame.setLocation(lastX0, lastY0);
        }
    }

    private void createTextPaneInFirstTab() {
        JScrollPane sp = createNewScrollPaneWithEditor(createNewTextArea());
        gTabbedPane.add(sp, "main");
        gMainFrame.getContentPane().add(gTabbedPane, BorderLayout.CENTER);
    }
    
    private void makeFrameVisible(JFrame frame) {
        frame.pack();
        frame.setVisible(true);
    }

    private JScrollPane createNewScrollPaneWithEditor(JTextArea textArea) {
        JScrollPane aScrollPane = new JScrollPane();
        aScrollPane.getViewport().add(textArea);
        aScrollPane.getViewport().setPreferredSize(textArea.getPreferredSize());
        return aScrollPane;
    }

    private void addAllListenersToTextArea(JTextArea textArea) {
        addKeyListenerToTextArea(textArea);
        addCaretListenerToTextArea(textArea);
        configureFontSizeControls(textArea);
        configureUndoRedoActions(textArea);
        configureTabsToSpacesAction(textArea);
        configureNewTabAction(textArea);
        configureRenameTabAction(textArea);
        configureCloseTabAction(textArea);
        configureNextTabAction(textArea);
        configurePreviousTabAction(textArea);
        configureDocumentListener(textArea);
        configureGarbageCollectorAction(textArea);
    }

    /**
     * this is a little wrong, since there can/will be multiple documents.
     * but all i care about is when the document gets "dirty".
     * as long as isDirty=false, i can quit without the warning dialog.
     */
    private void configureDocumentListener(JTextArea textArea) {
        final Document doc = textArea.getDocument();
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                handleDocumentWasModifiedEvent();
            }
            public void removeUpdate(DocumentEvent e) {
                handleDocumentWasModifiedEvent();
            }
            public void changedUpdate(DocumentEvent e) {
                handleDocumentWasModifiedEvent();
            }
        });
    }
    
    void handleDocumentWasModifiedEvent() {
        gIsDirty = true;
    }
    
    private JTextArea createNewTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monaco", Font.PLAIN, 12));
        textArea.setMargin(new Insets(20, 20, 20, 20));
        textArea.setBackground(new Color(210, 230, 210));
        textArea.setForeground(new Color(25, 25, 25));
        textArea.setPreferredSize(new Dimension(700, 800));
        addAllListenersToTextArea(textArea);
        return textArea;
    }
    
    void handleNewTabRequest(String desiredTabName) {
        JTextArea newTextArea = createNewTextArea();
        JScrollPane newScrollPane = createNewScrollPaneWithEditor(newTextArea);
        gTabbedPane.add(newScrollPane, desiredTabName);
        gTabbedPane.setSelectedComponent(newScrollPane);
        newTextArea.requestFocus();
    }

    void handleRenameTabRequest(String newTabName, int selectedIndex) {
        gTabbedPane.setTitleAt(selectedIndex, newTabName);
    }

    private void configureTabsToSpacesAction(JTextArea textArea) {
        gTabsToSpacesAction = new TabsToSpacesAction(this, textArea, "Tabs -> Spaces", gTabsToSpacesKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gTabsToSpacesAction, gTabsToSpacesKeystroke, "tabsToSpacesKeystroke");
    }

    private void configureNewTabAction(JTextArea textArea) {
        gNewTabAction = new NewTabAction(this, gMainFrame, "New Tab", gNewTabKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gNewTabAction, gNewTabKeystroke, "newTabKeystroke");
    }

    private void configureRenameTabAction(JTextArea textArea) {
        gRenameTabAction = new RenameTabAction(this, gMainFrame, gTabbedPane, "Rename a Tab", gRenameTabKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gRenameTabAction, gRenameTabKeystroke, "renameTabKeystroke");
    }

    private void configureCloseTabAction(JTextArea textArea) {
        gCloseTabAction = new CloseTabAction(this, gTabbedPane, "Close a Tab", gCloseTabKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gCloseTabAction, gCloseTabKeystroke, "closeTabKeystroke");
    }

    private void configureNextTabAction(JTextArea textArea) {
        gNextTabAction = new NextTabAction(this, gTabbedPane, "Next Tab", gNextTabKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gNextTabAction, gNextTabKeystroke, "nextTabKeystroke");
    }

    private void configurePreviousTabAction(JTextArea textArea) {
        gPreviousTabAction = new PreviousTabAction(this, gTabbedPane, "Previous Tab", gPreviousTabKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gPreviousTabAction, gPreviousTabKeystroke, "previousTabKeystroke");
    }
    
    private void addActionAndKeystrokeToMaps(JTextArea textArea, Action action, KeyStroke keystroke, String keystrokeLabel) {
        textArea.getInputMap().put(keystroke, keystrokeLabel);
        textArea.getActionMap().put(keystrokeLabel, action);
    }

    private void configureGarbageCollectorAction(JTextArea textArea) {
        gRunGarbageCollectorAction = new RunGarbageCollectorAction("Run Garbage Collector", gRunGarbageCollectorKeystroke.getKeyCode());
        addActionAndKeystrokeToMaps(textArea, gRunGarbageCollectorAction, gRunGarbageCollectorKeystroke, "gRunGarbageCollectorKeystroke");
    }

    private void configureUndoRedoActions(JTextArea textArea) {
        addActionAndKeystrokeToMaps(textArea, gUndoAction, gUndoKeystroke, "gUndoKeystroke");
        addActionAndKeystrokeToMaps(textArea, gRedoAction, gRedoKeystroke, "gRedoKeystroke");
        
        Document document = textArea.getDocument();
        document.addUndoableEditListener(gUndoHandler);
    }

    private void addKeyListenerToTextArea(final JTextArea textArea) {
        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleTextAreaKeyPressed(e, textArea);
            }
        });
    }

    private void addCaretListenerToTextArea(final JTextArea textArea) {
        textArea.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                handleCaretUpdate(e, textArea);
            }
        });
    }

    /**
     * Let the user (me) increase and decrease the font size.
     */
    private void configureFontSizeControls(JTextArea textArea) {
        gDecreaseFontSizeAction = new DecreaseFontSizeAction(this, textArea, "Font--", gDecreaseFontSizeKeystroke.getKeyCode());
        gIncreaseFontSizeAction = new IncreaseFontSizeAction(this, textArea, "Font++", gIncreaseFontSizeKeystroke1.getKeyCode());

        addActionAndKeystrokeToMaps(textArea, gDecreaseFontSizeAction, gDecreaseFontSizeKeystroke,  "decreaseFontSizeKeystroke");
        addActionAndKeystrokeToMaps(textArea, gIncreaseFontSizeAction, gIncreaseFontSizeKeystroke1, "largerFontSizeKeystroke1");
        addActionAndKeystrokeToMaps(textArea, gIncreaseFontSizeAction, gIncreaseFontSizeKeystroke2, "largerFontSizeKeystroke2");
        addActionAndKeystrokeToMaps(textArea, gIncreaseFontSizeAction, gIncreaseFontSizeKeystroke3, "largerFontSizeKeystroke3");
    }
        
    // TODO make this so it isn't mac-specific
    private void configureQuitHandler() {
        thisApp.setQuitHandler(new QuitHandler() {
            @Override
            public void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse quitResponse) {
                if (!gIsDirty) System.exit(0);
                boolean proceedWithExit = userWantsToProceedWithQuitAction();
                if (proceedWithExit == true) {
                    System.exit(0);
                } else {
                    quitResponse.cancelQuit();
                }
            }
        });
    }

    private JMenuBar createMenuBar() {
        // create the menubar
        JMenuBar menuBar = new JMenuBar();

        // File menu
        //JMenu fileMenu = new JMenu("File");

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem(gUndoAction));
        editMenu.add(new JMenuItem(gRedoAction));
        editMenu.add(new JMenuItem(gTabsToSpacesAction));
        editMenu.add(new JMenuItem(gRunGarbageCollectorAction));

        // tabs
        JMenu tabsMenu = new JMenu("Tabs");
        tabsMenu.add(new JMenuItem(gNewTabAction));
        tabsMenu.add(new JMenuItem(gRenameTabAction));
        tabsMenu.add(new JMenuItem(gNextTabAction));
        tabsMenu.add(new JMenuItem(gPreviousTabAction));

        // add the menus to the menubar
        //menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(tabsMenu);

        return menuBar;
    }
        
    private void updateDimensions() {
        gPrefs.putInt(LAST_X0, gMainFrame.getX());
        gPrefs.putInt(LAST_Y0, gMainFrame.getY());
        gPrefs.putInt(HEIGHT, gMainFrame.getHeight());
        gPrefs.putInt(WIDTH, gMainFrame.getWidth());
    }

    // MainFrameComponentAdapter method
    void mainFrameMoved(ComponentEvent e) {
        updateDimensions();
    }

    // MainFrameComponentAdapter method
    void mainFrameResized(ComponentEvent e) {
        updateDimensions();
    }

    private void handleTextAreaKeyPressed(final KeyEvent e, final JTextArea tp) {
        if (e.getKeyCode() != TAB_KEY_CODE) return;

        // convert TAB (w/ selected text) by shifting all text over
        if ((e.getKeyCode() == TAB_KEY_CODE) && (!e.isShiftDown()) && (tp.getSelectedText() != null)) {
            String textAfterTabbing = EditActions.insertIndentAtBeginningOfLine(tp.getSelectedText());
            int start = tp.getSelectionStart();
            int end = tp.getSelectionEnd();
            int originalLength = end - start;
            replaceSelectionAndKeepCursor(textAfterTabbing, tp);
            e.consume();
            int newLength = textAfterTabbing.length();
            tp.select(start, end + newLength - originalLength);
        }
        // convert TAB (w/ no selected text) to spaces
        else if ((e.getKeyCode() == TAB_KEY_CODE) && (!e.isShiftDown()) && (tp.getSelectedText() == null)) {
            String textAfterTabbing = TAB_AS_SPACES;
            replaceSelectionAndKeepCursor(textAfterTabbing, tp);
            e.consume();
        }
        // SHIFT-TAB w/ selected text
        else if ((e.getKeyCode() == TAB_KEY_CODE) && (e.isShiftDown()) && (tp.getSelectedText() != null)) {
            String textAfterTabbing = EditActions.removeIndentFromBeginningOfLine(tp.getSelectedText());
            int start = tp.getSelectionStart();
            int end = tp.getSelectionEnd();
            int originalLength = end - start;
            replaceSelectionAndKeepCursor(textAfterTabbing, tp);
            e.consume();
            int newLength = textAfterTabbing.length();
            tp.select(start, end + newLength - originalLength);
        }
        // SHIFT-TAB w/ NO selected text
        // @todo DON'T KNOW HOW TO DO THIS
        // @todo NEED HELP HERE
        // maybe determine the text range; manually select the text; then do the
        // same as is done for selected text above
        else if ((e.getKeyCode() == TAB_KEY_CODE) && (e.isShiftDown()) && (tp.getSelectedText() == null)) {
            Document document = tp.getDocument();
            Element root = document.getDefaultRootElement();
            Element element = root.getElement(gCurrentRow);
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset();
            tp.select(startOffset, endOffset - 1);
            String textOfCurrentLine = getTextOfCurrentLine(element);
            String textAfterRemovingTabs = EditActions
                    .removeIndentFromBeginningOfLine(textOfCurrentLine);
            replaceSelectionAndKeepCursor(textAfterRemovingTabs, tp);
            e.consume();
            // int originalLength = endOffset-startOffset;
            // int newLength = textAfterRemovingTabs.length();
            // tp.select(startOffset,endOffset+newLength-originalLength);
        }
    }

    private void handleCaretUpdate(final CaretEvent e, JTextArea textArea) {
        Element root = textArea.getDocument().getDefaultRootElement();
        int dot = e.getDot();
        int row = root.getElementIndex(dot);
        int col = dot - root.getElement(row).getStartOffset();
        gCurrentRow = row;
        gCurrentCol = col;
        // updateStatusBar(row+1, col+1);
    }

    private String getTextOfCurrentLine(Element element) {
        try {
            return element.getDocument().getText(element.getStartOffset(),
                    (element.getEndOffset() - element.getStartOffset()));
        } catch (BadLocationException e) {
            // this is not a great way to do this, but hopefully it doesn't
            // matter
            e.printStackTrace();
            return "";
        }
    }

    // TODO test/review this method
    private void replaceSelectionAndKeepCursor(final String newText, final JTextArea textArea) {
        textArea.replaceSelection(newText);
        textArea.repaint();
        textArea.requestFocus();
    }
    
    /**
     * Returns true if the user wants to exit. 
     */
    boolean userWantsToProceedWithQuitAction() {
        int choice = JOptionPane.showOptionDialog(gMainFrame,
            "You really want to quit?",
            "Quit?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null, null, null);
         
        // interpret the user's choice
        if (choice == JOptionPane.YES_OPTION) {
                return true;
        } else {
            return false;
        }
    }

    /**
     * Reduce the size of the font in the editor area.
     */
    public void decreaseFontSizeAction(final JTextArea textArea) {
        Font f = textArea.getFont();
        Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() - 1);
        textArea.setFont(f2);
    }

    public void increaseFontSizeAction(final JTextArea tp) {
        Font f = tp.getFont();
        Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() + 1);
        tp.setFont(f2);
    }


}



