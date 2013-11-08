/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.tutorials.applet;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.ChocoLogging.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Main class for the demo
 */
public class DemoUI {

    public final static Logger LOGGER = ChocoLogging.getMainLogger();

    private static String base_url = "http://choco.svn.sourceforge.net/viewvc/choco/trunk/samples/" +
            "src/main/java/";

    public JList list;
    public JTextPane code;
    public JTextArea result;
    int demoNb;
    public boolean solving = false;

    PrintStream out;

    /**
     * Properties file
     */
    public static final Properties properties;
    public static final String[] classes;

    static {
        properties = new Properties();
        try {
            final InputStream is = DemoUI.class.getResourceAsStream("/gui.properties");
            properties.load(is);
        } catch (IOException e) {
            LOGGER.severe("Could not open gui.properties");
        }
        classes = new String[properties.size()];
        int i = 0;
        for(Object  k : properties.keySet()){
            classes[i++] = (String)properties.get(k);
        }
        Arrays.sort(classes);
    }

    public void createGUI(Container contentPane) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].substring(classes[i].lastIndexOf(".") + 1);
        }
        list = new JList(names);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                updateCode();
            }
        });
        panel.add(list, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Problems"));
        panel.setBackground(Color.white);
        JButton button = new JButton("Run !");
        button.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!solving) runDemo();
            }
        });
        panel.add(button, BorderLayout.SOUTH);
        result = new JTextArea();
        result.setFont(new Font("Courier new", Font.BOLD, 12));
        result.setEditable(false);
        result.setBorder(BorderFactory.createTitledBorder("Results"));
        try {
            out = new PrintStream(new MyOutputStream(result), true, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Handler handler = new StreamHandler(out, ChocoLogging.LIGHT_FORMATTER);
        ChocoLogging.getChocoLogger().addHandler(handler);
        System.setOut(out);

        JScrollPane scroll = new JScrollPane(result);
        MyDocument document = new MyDocument();
        code = new JTextPane(document);
        code.setFont(new Font("Courier new", Font.PLAIN, 11));
        code.setEditable(false);
        code.setBorder(BorderFactory.createTitledBorder("What does the code look like ?"));
        JScrollPane scroll2 = new JScrollPane(code);
        JSplitPane subsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll2, scroll);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, subsplit);
        split.setDividerLocation(100);
        contentPane.add(split, BorderLayout.CENTER);
        subsplit.setDividerLocation(200);
    }

    public void runDemo() {
        demoNb = list.getSelectedIndex();
        if (demoNb < 0) {
            result.setText("No selected demonstration !\n" +
                    "Please click on one demonstration on the left.");
        } else {
            solving = true;
            try {
//                Thread th = new Thread() {
//                    public void run() {
                try {
                    Class demoClass =
                            this.getClass().getClassLoader().
                                    loadClass(classes[demoNb]);
                    Object demoObject = demoClass.newInstance();
                    Method demoMethod = demoClass.getMethod("execute");
                    demoMethod.invoke(demoObject);

                } catch (ClassNotFoundException e) {
                    result.append("Class not found !");
                } catch (NoSuchMethodException e) {
                    result.append("No valid class found !");
                } catch (InstantiationException e) {
                    result.append("Cannot create the demo object !");
                } catch (IllegalAccessException e) {
                    result.append("Cannot access to the constructor and/or method of the demo !");
                } catch (InvocationTargetException e) {
                    result.append("Demo throwed an exception : ");
                    Throwable t = e;
                    while (t != null) {
                        Object[] traces = t.getStackTrace();
                        for (Object trace : traces) {
                            result.append(trace + "\n");
                        }
                        LOGGER.info(t.toString() + "\n");
                        t = t.getCause();
                    }
                }

                solving = false;
//                    }
//                };
//                th.start();
//                th.join();
            } catch (Exception e) {
                LOGGER.severe("Solving error !");
            }
        }
    }

    public void updateCode() {
        result.setText("");

        int nb = list.getSelectedIndex();
        StringBuffer buf = new StringBuffer();
        buf.append(MessageFormat.format("Code of {0}.java.\n\n", classes[nb]));

        URL url = null;
        try {
            url = new URL(base_url + classes[nb].replace('.', '/') + ".java");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        InputStream in = null;
        try {
            in = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (in != null) {
            Reader reader = new BufferedReader(new InputStreamReader(in));
            int ch;
            try {
                while ((ch = reader.read()) > -1) {
                    buf.append((char) ch);
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("IO exception in DemoUI : " + e.getStackTrace());
            }
        } else {
            buf.append("Source code not found !");
        }
        code.setText(buf.toString());
        code.setCaretPosition(0);
    }

    static class MyDocument extends DefaultStyledDocument {
        private Hashtable<String, Object> keywords;

        DefaultStyledDocument doc;
        MutableAttributeSet normal;
        MutableAttributeSet keyword;
        MutableAttributeSet comment;
        MutableAttributeSet quote;
        MutableAttributeSet impclasses1;

        public MyDocument() {
            doc = this;
            putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
            normal = new SimpleAttributeSet();
            StyleConstants.setForeground(normal, Color.black);
            comment = new SimpleAttributeSet();
            StyleConstants.setForeground(comment, Color.lightGray);
            StyleConstants.setItalic(comment, true);
            keyword = new SimpleAttributeSet();
            StyleConstants.setForeground(keyword, new Color(110, 80, 220));
            StyleConstants.setBold(keyword, true);
            quote = new SimpleAttributeSet();
            StyleConstants.setForeground(quote, new Color(54, 150, 54));
            StyleConstants.setBold(quote, true);
            Object dummyObject = new Object();
            keywords = new Hashtable<String, Object>();
            keywords.put("abstract", dummyObject);
            keywords.put("boolean", dummyObject);
            keywords.put("break", dummyObject);
            keywords.put("byte", dummyObject);
            keywords.put("byvalue", dummyObject);
            keywords.put("case", dummyObject);
            keywords.put("cast", dummyObject);
            keywords.put("catch", dummyObject);
            keywords.put("char", dummyObject);
            keywords.put("class", dummyObject);
            keywords.put("const", dummyObject);
            keywords.put("continue", dummyObject);
            keywords.put("default", dummyObject);
            keywords.put("do", dummyObject);
            keywords.put("double", dummyObject);
            keywords.put("else", dummyObject);
            keywords.put("extends", dummyObject);
            keywords.put("false", dummyObject);
            keywords.put("final", dummyObject);
            keywords.put("finally", dummyObject);
            keywords.put("float", dummyObject);
            keywords.put("for", dummyObject);
            keywords.put("future", dummyObject);
            keywords.put("generic", dummyObject);
            keywords.put("goto", dummyObject);
            keywords.put("if", dummyObject);
            keywords.put("implements", dummyObject);
            keywords.put("import", dummyObject);
            keywords.put("inner", dummyObject);
            keywords.put("instanceof", dummyObject);
            keywords.put("int", dummyObject);
            keywords.put("interface", dummyObject);
            keywords.put("long", dummyObject);
            keywords.put("native", dummyObject);
            keywords.put("new", dummyObject);
            keywords.put("null", dummyObject);
            keywords.put("operator", dummyObject);
            keywords.put("outer", dummyObject);
            keywords.put("package", dummyObject);
            keywords.put("private", dummyObject);
            keywords.put("protected", dummyObject);
            keywords.put("public", dummyObject);
            keywords.put("rest", dummyObject);
            keywords.put("return", dummyObject);
            keywords.put("short", dummyObject);
            keywords.put("static", dummyObject);
            keywords.put("super", dummyObject);
            keywords.put("switch", dummyObject);
            keywords.put("synchronized", dummyObject);
            keywords.put("this", dummyObject);
            keywords.put("throw", dummyObject);
            keywords.put("throws", dummyObject);
            keywords.put("transient", dummyObject);
            keywords.put("true", dummyObject);
            keywords.put("try", dummyObject);
            keywords.put("var", dummyObject);
            keywords.put("void", dummyObject);
            keywords.put("volatile", dummyObject);
            keywords.put("while", dummyObject);
        }

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            super.insertString(offset, str, a);
            processChangedLines(offset, str.length());
        }

        public void remove(int offset, int length) throws BadLocationException {
            super.remove(offset, length);
            processChangedLines(offset, 0);
        }

        public void processChangedLines(int offset, int length) throws BadLocationException {
            String content = doc.getText(0, doc.getLength());
            Element root = doc.getDefaultRootElement();
            int startLine = root.getElementIndex(offset);
            int endLine = root.getElementIndex(offset + length);
            for (int i = startLine; i <= endLine; i++) {
                int startOffset = root.getElement(i).getStartOffset();
                int endOffset = root.getElement(i).getEndOffset();
                applyHighlighting(content, startOffset, endOffset - 1);
            }
        }

        public void applyHighlighting(String content, int startOffset, int endOffset) throws BadLocationException {
            int index;
            int lineLength = endOffset - startOffset;
            int contentLength = content.length();
            if (endOffset >= contentLength) endOffset = contentLength - 1;
            //  set normal attributes for the line
            doc.setCharacterAttributes(startOffset, lineLength, normal, true);
            //  check for multi line comment
            String multiLineStartDelimiter = "/*";
            String multiLineEndDelimiter = "*/";
            index = content.lastIndexOf(multiLineStartDelimiter, endOffset);
            if (index > -1) {
                int index2 = content.indexOf(multiLineEndDelimiter, index);
                if ((index2 == -1) || (index2 > endOffset)) {
                    doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
                    return;
                } else if (index2 >= startOffset) {
                    doc.setCharacterAttributes(index, index2 + 2 - index, comment, false);
                    return;
                }
            }            //  check for single line comment
            String singleLineDelimiter = "//";
            index = content.indexOf(singleLineDelimiter, startOffset);
            if ((index > -1) && (index < endOffset)) {
                doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
                endOffset = index - 1;
            }            //  check for tokens
            checkForTokens(content, startOffset, endOffset);
        }

        private void checkForTokens(String content, int startOffset, int endOffset) {
            while (startOffset <= endOffset) {                //  find the start of a new token
                while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
                    if (startOffset < endOffset) startOffset++;
                    else return;
                }            //
                if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1)))
                    startOffset = getQuoteToken(content, startOffset, endOffset);
                else
                    startOffset = getOtherToken(content, startOffset, endOffset);
            }
        }

        private boolean isDelimiter(String character) {
            String operands = ";:{}()[]+-/%<=>!&|^~*";
            return Character.isWhitespace(character.charAt(0)) || operands.indexOf(character) != -1;
        }

        private boolean isQuoteDelimiter(String character) {
            String quoteDelimiters = "\"'";
            return quoteDelimiters.indexOf(character) != -1;
        }

        private boolean isKeyword(String token) {
            Object o = keywords.get(token);
            return o != null;
        }

        private int getQuoteToken(String content, int startOffset, int endOffset) {
            String quoteDelimiter = content.substring(startOffset, startOffset + 1);
            String escapedDelimiter = "\\" + quoteDelimiter;
            int index;
            int endOfQuote = startOffset;            //  skip over the escaped quotes in this quote
            index = content.indexOf(escapedDelimiter, endOfQuote + 1);
            while ((index > -1) && (index < endOffset)) {
                endOfQuote = index + 1;
                index = content.indexOf(escapedDelimiter, endOfQuote);
            }            // now find the matching delimiter
            index = content.indexOf(quoteDelimiter, endOfQuote + 1);
            if ((index == -1) || (index > endOffset)) endOfQuote = endOffset;
            else endOfQuote = index;
            doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);
            //String token = content.substring(startOffset, endOfQuote + 1);
            //LOGGER.info( "quote: " + token );
            return endOfQuote + 1;
        }

        private int getOtherToken(String content, int startOffset, int endOffset) {
            int endOfToken = startOffset + 1;
            while (endOfToken <= endOffset) {
                if (isDelimiter(content.substring(endOfToken, endOfToken + 1))) break;
                endOfToken++;
            }
            String token = content.substring(startOffset, endOfToken);
            //LOGGER.info( "found: " + token );
            if (isKeyword(token)) doc.setCharacterAttributes(startOffset, endOfToken - startOffset, keyword, false);
            //if (isimpclasses1(token)) doc.setCharacterAttributes(startOffset, endOfToken - startOffset, impclasses1, false);
            return endOfToken + 1;
        }
    }

    public class MyOutputStream extends OutputStream {

        private PipedOutputStream out = new PipedOutputStream();
        private Reader reader;
        private JTextArea pane;

        public MyOutputStream(JTextArea pane) throws IOException {
            PipedInputStream in = new PipedInputStream(out);
            reader = new InputStreamReader(in, "UTF-8");
            this.pane = pane;
        }

        public void write(int i) throws IOException {
            out.write(i);
        }

        public void write(byte[] bytes, int i, int i1) throws IOException {
            out.write(bytes, i, i1);
        }

        public void flush() throws IOException {
            if (reader.ready()) {
                char[] chars = new char[1024];

                int n = reader.read(chars);
                // this is your text
                String txt = new String(chars, 0, n);
                pane.append(txt);
            }
        }
    }
}


