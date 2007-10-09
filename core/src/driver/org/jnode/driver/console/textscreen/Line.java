/*
 * $Id: Line.java 3119 2007-02-11 22:25:20Z fduminy $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.console.textscreen;

import java.util.Arrays;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.spi.ConsolePrintStream;


/**
 * A class that handles the content of the current command line in the shell.
 * That can be : - a new command that the user is beeing editing - an existing
 * command (from the command history)
 * 
 * This class also handles the current cursor position in the command line and
 * keep trace of the position (consoleX, consoleY) of the first character of the
 * command line (to handle commands that are multilines).
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
class Line {
	//TODO get the real screen width (in columns)
    final static private int SCREEN_WIDTH = 80;
    
    private int consoleX;

    private int consoleY;

    /**
     * Contains the current position of the cursor on the currentLine
     */
    private int posOnCurrentLine = 0;

    /** Contains the current line * */
    private StringBuffer currentLine = new StringBuffer(80);

    private boolean shortened = true;

    private int oldLength = 0;

    private int maxLength = 0;
    
    private final TextConsole console;

    private ConsolePrintStream out;

    public Line(TextConsole console) {
        this.console = console;
        this.out = (ConsolePrintStream) console.getOut();
    }

    public void start() {
        start(false);
    }

    public boolean isEmpty() {
        return currentLine.toString().trim().length() == 0;
    }

    public void start(boolean keepContent) {
        if (keepContent) {
            // we stay at the same position in X coordinate
            // only Y may have changed
            consoleY = console.getCursorY();
        } else {
            consoleX = console.getCursorX();
            consoleY = console.getCursorY();

            setContent("");
            console.setCursor( consoleX,consoleY);//move the cursor to the start of the line.
        }
    }

    public String getContent() {
        return currentLine.toString();
    }

    public byte[] getBytes() {
        return currentLine.toString().getBytes();
    }

    public byte[] consumeBytes() {
        byte[] res = getBytes();
        currentLine.setLength(0);
        return res;
    }

    public void setContent(String content) {
        startModif();
        currentLine.setLength(0);
        currentLine.append(content);
        moveEnd();
        endModif();
    }

    public boolean moveLeft() {
        if (posOnCurrentLine > 0) {
            posOnCurrentLine--;
            return true;
        }
        return false;
    }

    public boolean moveRight() {
        if (posOnCurrentLine < currentLine.length()) {
            posOnCurrentLine++;
            return true;
        }
        return false;
    }

    public void moveEnd() {
        posOnCurrentLine = currentLine.length();
    }

    public void moveBegin() {
        posOnCurrentLine = 0;
    }

    public boolean backspace() {
        if (posOnCurrentLine > 0) {
            moveLeft();
            delete();
            return true;
        }
        return false;
    }

    public void delete() {
        if ((posOnCurrentLine >= 0)
                && (posOnCurrentLine < currentLine.length())) {
            startModif();
            currentLine.deleteCharAt(posOnCurrentLine);
            endModif();
        }
    }

    public CompletionInfo complete() {
        CompletionInfo info = null;
        InputCompleter completer = console.getCompleter();
        if (posOnCurrentLine != currentLine.length()) {
            String ending = currentLine.substring(posOnCurrentLine);
            info = completer.complete(currentLine.substring(0, posOnCurrentLine));
            printList(info);
            if (info.getCompleted() != null) {
                setContent(info.getCompleted() + ending);
                posOnCurrentLine = currentLine.length() - ending.length();
            }
        } else {
            info = completer.complete(currentLine.toString());
            printList(info);
            if (info.getCompleted() != null) {
                setContent(info.getCompleted());
                posOnCurrentLine = currentLine.length();
            }
        }

        return info;
    }

    protected void printList(CompletionInfo info) {
        if ((info != null) && info.hasItems()) {
            int oldPosOnCurrentLine = posOnCurrentLine;
            moveEnd();
            refreshCurrentLine();

            out.println();
            String[] list = info.getItems();
            
            final int minItemsToSplit = 5;
            if(list.length > minItemsToSplit)
            {
	            list = splitInColumns(list); 
            }

        	// display items column (may be single or multiple columns)
            for (String item : list)
            {               	
            	// item may actually be a single item or in fact multiple items
            	if((item.length() % SCREEN_WIDTH) == 0)
            	{
            		// we are already at the first column of the next line 
            		out.print(item);
            	}
            	else
            	{
            		// we aren't at the first column of the next line
            		out.println(item);
            	}
            }

            posOnCurrentLine = oldPosOnCurrentLine;
        }
    }
    
    protected String[] splitInColumns(String[] items)
    {
        final int separatorWidth = 3;
        
        // compute the maximum width of items
        int maxWidth = 0;
        for(String item : items)
        {
        	if(item.length() > maxWidth)
        	{
        		maxWidth = item.length();
        	}
        }
        
        final int columnWidth = Math.min(SCREEN_WIDTH, maxWidth + separatorWidth);
        final int nbColumns = SCREEN_WIDTH / columnWidth;
        final boolean lastLineIsFull = ((items.length % nbColumns) == 0);
        final int nbLines = (items.length / nbColumns) + (lastLineIsFull ? 0 : 1);
        
        String[] lines = new String[nbLines];
    	StringBuilder line = new StringBuilder(SCREEN_WIDTH);
    	int lineNum = 0;
        for(int itemNum = 0 ; itemNum < items.length ; )
        {
        	for(int c = 0 ; c < nbColumns ; c++)
        	{
        		final String item = items[itemNum++];
        		line.append(item);
        		
        		// add some blanks
        		final int nbBlanks = columnWidth - item.length();
        		for(int i = 0 ; i < nbBlanks ; i++)
        		{
        			line.append(' ');
        		}

        		if(itemNum >= items.length) break;
        	}
        	
        	lines[lineNum++] = line.toString(); 
        	line.setLength(0); // clear the buffer
        }
        
        return lines; 
    }

    public void appendChar(char c) {
        startModif();
        if (posOnCurrentLine == currentLine.length()) {
            currentLine.append(c);
        } else {
            currentLine.insert(posOnCurrentLine, c);
        }
        posOnCurrentLine++;
        endModif();
    }

    protected void startModif() {
        shortened = false;
        oldLength = currentLine.length();
    }

    protected void endModif() {
        maxLength = Math.max(oldLength, currentLine.length());
        shortened = oldLength > currentLine.length();
        oldLength = 0;
    }
    
    private volatile char[] mySpaces;
    
    private char[] getSpaces(int count) {
    	char[] res = mySpaces;
    	if (res == null || res.length < count) {
    		res = new char[count];
    		Arrays.fill(res, ' ');
    		mySpaces = res;
    	}
    	return res;
    }
    
    public void refreshCurrentLine() {
        try {
            int x = consoleX;
            int width = console.getWidth();
            int nbLines = ((x + maxLength) / width);

            if (((x + maxLength) % width) != 0)
                nbLines++;

            // output the input line buffer contents with the screen cursor hidden
            console.setCursorVisible(false);
            console.setCursor(consoleX, consoleY);
            out.print(currentLine);
            
            // get position of end of line
            // FIXME ... there's a problem here if some application simultaneously
            // writes to console output.
            int newConsoleX = console.getCursorX();
            int newConsoleY = console.getCursorY();
            
            // blank to the end of the screen region
            if (newConsoleX > 0) {
            	int len = width - newConsoleX;
            	console.setChar(newConsoleX, newConsoleY, getSpaces(len), 
            			0, len, out.getFgColor());
            	newConsoleY++;
            }
            for (int i = newConsoleY; i < consoleY + nbLines; i++) {
                console.clearRow(i);
            }

            // reset the screen cursor and reveal it
            // FIXME ... there's a problem here if the input buffer contains
            // characters that do not render as one screen character; e.g. \t or \n.
            int inputCursorX = x + posOnCurrentLine;
            int inputCursorY = consoleY;
            if (inputCursorX >= width) {
                inputCursorY += inputCursorX / width;
                inputCursorX = (inputCursorX % width);
            }
            console.setCursor(inputCursorX, inputCursorY);
            console.setCursorVisible(true);
            
            // if the line has not been shortened (delete, backspace...)
            if (!shortened) {
                // ensure that the location of the input cursor is included.
            	console.ensureVisible(inputCursorY);
            }
            console.setCursorVisible(true);
        } catch (Exception e) {
            // TODO - why ignore these exceptions?  Are they due to the console methods
        	// not being thread-safe???
        }
    }

	public int getLineLength() {
		return currentLine.length();
	}
}