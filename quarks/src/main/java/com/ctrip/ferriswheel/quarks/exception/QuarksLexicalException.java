package com.ctrip.ferriswheel.quarks.exception;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class QuarksLexicalException extends QuarksException {

    private static final long serialVersionUID = 1L;

    private String src;
    private int line = 1;
    private int column = 0;

    public QuarksLexicalException(Throwable clause) {
        super(clause);
    }

    public QuarksLexicalException(String src, int line, int column) {
        super();
        this.src = src;
        this.line = line;
        this.column = column;
    }

    public QuarksLexicalException(String src, int line, int column,
            String message) {
        super(message + " @L" + line + "C" + column);
        this.src = src;
        this.line = line;
        this.column = column;
    }

    public QuarksLexicalException(String src, int line, int column,
            Throwable clause) {
        super(clause);
        this.src = src;
        this.line = line;
        this.column = column;
    }

    public QuarksLexicalException(String src, int line, int column,
            String message, Throwable clause) {
        super(message + " @L" + line + "C" + column, clause);
        this.src = src;
        this.line = line;
        this.column = column;
    }

    public String getSource() {
        return src;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void printErrorLocation() {
        StringBuilder sb = new StringBuilder();

        sb.append(line).append(": ");

        int n = 0;

        for (int i = 0; i < src.length(); i++) {
            if (i == column)
                n = sb.length();

            char ch = src.charAt(i);
            if (ch == '\t') {
                sb.append("    ");
            } else {
                sb.append(ch);
            }
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                System.err));
        try {
            writer.write("\n");
            writer.write(sb.toString());
            writer.write("\n");

            if (n == 0)
                n = sb.length();
            for (int i = 0; i < n; i++) {
                writer.write("~");
            }
            writer.write("^\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
