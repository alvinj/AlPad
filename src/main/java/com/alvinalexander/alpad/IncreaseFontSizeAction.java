package com.alvinalexander.alpad;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class IncreaseFontSizeAction extends AbstractAction
{
	AlPad controller;
	JTextPane textArea;
	
	public IncreaseFontSizeAction(AlPad controller, JTextPane textArea, String name, Integer mnemonic) {
		super(name, null);
		this.controller = controller;
		this.textArea = textArea;
		putValue(MNEMONIC_KEY, mnemonic);
	}
	public void actionPerformed(ActionEvent e)
	{
		controller.increaseFontSizeAction(textArea);
	}
}