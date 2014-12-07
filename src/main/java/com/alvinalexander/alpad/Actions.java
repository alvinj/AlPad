package com.alvinalexander.alpad;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

// a do-nothing public class (because last i heard, a java filename
// should correspond to the name of the public class it contains)
//public class Actions {}

class MainFrameWindowListener extends java.awt.event.ComponentAdapter {
    AlPad adaptee;
  
    MainFrameWindowListener(AlPad adaptee) {
        this.adaptee = adaptee;
    }
    public void componentMoved(ComponentEvent e) {
        adaptee.mainFrameMoved(e);
    }
    public void componentResized(ComponentEvent e) {
        adaptee.mainFrameResized(e);
    }
}

class NewTabAction extends AbstractAction {
    AlPad controller;
    JFrame jFrame;
    public NewTabAction(final AlPad controller, final JFrame jFrame, String name, Integer mnemonic) {
        super(name);
        putValue(MNEMONIC_KEY, mnemonic);
        this.controller = controller;
        this.jFrame = jFrame;
    }
    public void actionPerformed(ActionEvent e) {
        // show a dialog requesting a tab name
        String tabName = JOptionPane.showInputDialog(jFrame, "Name for the new tab:");
        if (tabName == null || tabName.trim().equals("")) {
            // do nothing
        } else {
            controller.handleNewTabRequest(tabName);
        }
    }
}    


class RenameTabAction extends AbstractAction {
    AlPad controller;
    JFrame jFrame;
    JTabbedPane tabbedPane;
    public RenameTabAction(final AlPad controller, final JFrame jFrame, final JTabbedPane tabbedPane, String name, Integer mnemonic) {
        super(name, null);
        putValue(MNEMONIC_KEY, mnemonic);
        this.controller = controller;
        this.jFrame = jFrame;
        this.tabbedPane = tabbedPane;
    }
    public void actionPerformed(ActionEvent e) {
        int tabIndex = tabbedPane.getSelectedIndex();
        String tabName = JOptionPane.showInputDialog(jFrame, "New name for the tab:");
        if (tabName == null || tabName.trim().equals("")) {
            // do nothing
        } else {
            controller.handleRenameTabRequest(tabName, tabIndex);
        }
    }
}    

class NextTabAction extends AbstractAction {
    AlPad controller;
    JTabbedPane tabbedPane;
    public NextTabAction(final AlPad controller, final JTabbedPane tabbedPane, String name, Integer mnemonic) {
        super(name, null);
        putValue(MNEMONIC_KEY, mnemonic);
        this.controller = controller;
        this.tabbedPane = tabbedPane;
    }
    public void actionPerformed(ActionEvent e) {
        int tabCount = tabbedPane.getTabCount();
        if (tabCount == 1) return;
        int newTabIndex = tabbedPane.getSelectedIndex() + 1;    // 0-based
        if (newTabIndex > tabCount-1) {
            tabbedPane.setSelectedIndex(0);
        } else {
            tabbedPane.setSelectedIndex(newTabIndex);
        }
    }
}    

class PreviousTabAction extends AbstractAction {
    AlPad controller;
    JTabbedPane tabbedPane;
    public PreviousTabAction(final AlPad controller, final JTabbedPane tabbedPane, String name, Integer mnemonic) {
        super(name, null);
        putValue(MNEMONIC_KEY, mnemonic);
        this.controller = controller;
        this.tabbedPane = tabbedPane;
    }
    public void actionPerformed(ActionEvent e) {
        int tabCount = tabbedPane.getTabCount();
        if (tabCount == 1) return;
        int newTabIndex = tabbedPane.getSelectedIndex() - 1;    // 0-based
        if (newTabIndex < 0) {
            tabbedPane.setSelectedIndex(tabCount-1);
        } else {
            tabbedPane.setSelectedIndex(newTabIndex);
        }
    }
}    

class CloseTabAction extends AbstractAction {
    AlPad controller;
    JTabbedPane tabbedPane;
    public CloseTabAction(final AlPad controller, final JTabbedPane tabbedPane, String name, Integer mnemonic) {
        super(name, null);
        putValue(MNEMONIC_KEY, mnemonic);
        this.controller = controller;
        this.tabbedPane = tabbedPane;
    }
    // TODO i don't like having two ways of exiting the app
    public void actionPerformed(ActionEvent e) {
        int tabCount = tabbedPane.getTabCount();
        if (tabCount == 1) {
            // if there's only one tab, see if they want to quit the app
            if (controller.userWantsToProceedWithQuitAction()) {
                System.exit(0);
            }
        } else {
            // there are multiple tabs; close the current one
            int choice = JOptionPane.showOptionDialog(null,
                "Close this tab?",
                "Close Tab?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (choice == JOptionPane.YES_OPTION) {
                int tabIndex = tabbedPane.getSelectedIndex();
                tabbedPane.remove(tabIndex);
            }
        }
    }
}    

/**
* Convert tabs to spaces
*/
class TabsToSpacesAction extends AbstractAction {
    JTextArea textArea;
    public TabsToSpacesAction(final AlPad controller, final JTextArea textArea, String name, Integer mnemonic) {
        super(name, null);
        putValue(MNEMONIC_KEY, mnemonic);
        this.textArea = textArea;
    }
    public void actionPerformed(ActionEvent e) {
        String text = textArea.getText();
        String newText = text.replaceAll("\t", "  ");
        textArea.setText(newText);
    }
}

/**
 * Hopefully this will be a short-lived addition to the project.
 * For some reason the memory consumption on OS X Yosemite is crazy,
 * even with just one text area, and I'm trying to find out why.
 */
class RunGarbageCollectorAction extends AbstractAction {
    public RunGarbageCollectorAction(String name, Integer mnemonic) {
        super(name, null);
        putValue(MNEMONIC_KEY, mnemonic);
        putValue(SHORT_DESCRIPTION, name);
    }
    public void actionPerformed(ActionEvent e) {
        long mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();
        long memUsed = (runtime.totalMemory() - runtime.freeMemory()) / mb;
        long memFree = runtime.freeMemory() / mb;
        long memTotal = runtime.totalMemory() / mb;
        long memMax = runtime.maxMemory() / mb;
        String output = "Memory Stats:\n" +
                        "-------------\n" +
                        "Used:  " + memUsed  + " MB\n" +
                        "Free:  " + memFree  + " MB\n" +
                        "Total: " + memTotal + " MB\n" +
                        "Max:   " + memMax   + " MB\n";
        showLongMessageInOptionPane(output);
        System.gc();
    }
    
    private void showLongMessageInOptionPane(String longMessage) {
        // text area
        JTextArea textArea = new JTextArea(8, 30);
        textArea.setFont(new Font("Monaco", Font.PLAIN, 12));
        textArea.setText(longMessage);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
         
        // display in a message dialog
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea));
    }
}


// /////////// handle undo and redo actions //////////////////

class UndoHandler implements UndoableEditListener {
  
    private UndoManager undoManager;
    private UndoAction undoAction;
    private RedoAction redoAction;

    public UndoHandler(UndoManager undoManager, UndoAction undoAction, RedoAction redoAction) {
        this.undoManager = undoManager;
        this.undoAction = undoAction;
        this.redoAction = redoAction;
    }

    /**
     * Messaged when the Document has created an edit, the edit is added to
     * <code>undoManager</code>, an instance of UndoManager.
     */
    public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
        undoAction.update();
        redoAction.update();
    }
}

class UndoAction extends AbstractAction {
    private UndoManager undoManager;
    private RedoAction redoAction;

    public UndoAction(UndoManager undoManager) {
        super("Undo");
        setEnabled(false);
        this.undoManager = undoManager;
    }
    
    public void setRedoAction(RedoAction redoAction) {
        this.redoAction = redoAction;
    }
  
    public void actionPerformed(ActionEvent e) {
        try {
            undoManager.undo();
        }
        catch (CannotUndoException ex) {
            // TODO log this or ignore it
            //ex.printStackTrace();
        }
        update();
        redoAction.update();
    }
  
    protected void update() {
        if (undoManager.canUndo()) {
            setEnabled(true);
            putValue(Action.NAME, undoManager.getUndoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Undo");
        }
    }
}

class RedoAction extends AbstractAction {
    private UndoManager undoManager;
    private UndoAction undoAction;

    public RedoAction(UndoManager undoManager) {
        super("Redo");
        setEnabled(false);
        this.undoManager = undoManager;
    }
    
    public void setUndoAction(UndoAction undoAction) {
        this.undoAction = undoAction;
    }
  
    public void actionPerformed(ActionEvent e) {
        try {
            undoManager.redo();
        } catch (CannotRedoException ex) {
            // TODO log this or ignore it
            //ex.printStackTrace();
        }
        update();
        undoAction.update();
    }
  
    protected void update() {
        if (undoManager.canRedo()) {
            setEnabled(true);
            putValue(Action.NAME, undoManager.getRedoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Redo");
        }
    }
}
  


