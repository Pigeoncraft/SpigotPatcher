package org.spigotmc.patcher;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

public class Console extends WindowAdapter implements ActionListener, Runnable {
    private JFrame frame;
    private JTextArea textArea;
    private Thread reader;
    private Thread reader2;
    private boolean quit;
    private final PipedInputStream pin = new PipedInputStream();
    private final PipedInputStream pin2 = new PipedInputStream();

    @SuppressWarnings("resource")
    public Console() {
        frame = new JFrame("Console");
        Dimension size = new Dimension(800, 600);
        int x = size.width / 2;
        int y = size.height / 2;
        frame.setBounds(x, y, size.width, size.height);
        textArea = new JTextArea();
        textArea.setEditable(false);
        JButton button = new JButton("Exit");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textArea, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        button.addActionListener(this);
        try {
            PipedOutputStream pout = new PipedOutputStream(pin);
            System.setOut(new PrintStream(pout, true));
        } catch (Exception ex) {
            textArea.append("Couldn't redirect output to this console\n" + ex.getMessage());
        }
        try {
            PipedOutputStream pout2 = new PipedOutputStream(pin2);
            System.setErr(new PrintStream(pout2, true));
        } catch (Exception ex) {
            textArea.append("Couldn't redirect error output to this console\n" + ex.getMessage());
        }
        quit = false;
        //
        reader = new Thread(this);
        reader.setDaemon(true);
        reader.start();
        //
        reader2 = new Thread(this);
        reader2.setDaemon(true);
        reader2.start();
    }

    @Override
    public synchronized void actionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    public boolean isClosed() {
        return frame != null && !frame.isValid();
    }

    public synchronized String readLine(PipedInputStream in) throws IOException {
        String input = "";
        do {
            int available = in.available();
            if (available == 0) {
                break;
            }
            byte b[] = new byte[available];
            in.read(b);
            input = input + new String(b, 0, b.length);
        } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
        return input;
    }

    @Override
    public synchronized void run() {
        try {
            while (Thread.currentThread() == reader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {}
                if (pin.available() != 0) {
                    String input = readLine(pin);
                    textArea.append(input);
                }
                if (quit) return;
            }
            while (Thread.currentThread() == reader2) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {}
                if (pin2.available() != 0) {
                    String input = readLine(pin2);
                    textArea.append(input);
                }
                if (quit) return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void windowClosed(WindowEvent evt) {
        quit = true;
        notifyAll();
        try {
            reader.join(1000);
            pin.close();
        } catch (Exception e) {}
        try {
            reader2.join(1000);
            pin2.close();
        } catch (Exception e) {}
        System.exit(0);
    }

    @Override
    public synchronized void windowClosing(WindowEvent evt) {
        frame.setVisible(false);
        frame.dispose();
    }
}
