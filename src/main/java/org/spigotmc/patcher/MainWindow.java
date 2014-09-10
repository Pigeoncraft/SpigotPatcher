package org.spigotmc.patcher;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class MainWindow extends JFrame{
    /**
     * 
     */
    private static final long serialVersionUID = -1715357374780770994L;
    private static final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private JTextField spigotJarField;
    private JTextField patchField;
    public static JTextArea textArea         = new JTextArea("");
    public static JScrollPane areaScrollPane = new JScrollPane(textArea);

    /* Mostly generated by WindowBuilder */
    public MainWindow() {
        setTitle("Spigot Patcher");
        setSize(new Dimension(500, 300));
        setMinimumSize(new Dimension(500, 300));
        setResizable(true);
        getContentPane().setLayout(new MigLayout("", "[][grow]", "[][][][][30.00][][][][][][-61.00][-16.00]"));
        /* Spigot jar label/field */
        JLabel label1 = new JLabel("  Original jar: ");
        label1.setFont(font);
        getContentPane().add(label1, "cell 0 0 2 1,alignx center");
        //
        spigotJarField = new JTextField();
        getContentPane().add(spigotJarField, "cell 1 1,growx");
        spigotJarField.setColumns(10);
        spigotJarField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File jarFile = chooseFile("Spigot-1649.jar", new File("."), ".jar", false);
                if (jarFile != null) {
                    spigotJarField.setText(jarFile.getPath());
                }
            }
        });
        addDragDrop(spigotJarField);
        /* BPS patch label/field */
        JLabel label2 = new JLabel("BPS patch:");
        label2.setFont(font);
        getContentPane().add(label2, "cell 1 2,alignx center");
        //
        patchField = new JTextField();
        getContentPane().add(patchField, "cell 1 3,growx");
        patchField.setColumns(10);
        patchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File patchFile = chooseFile("BPS patch", new File("."), ".bps", false);
                if (patchFile != null) {
                    patchField.setText(patchFile.getPath());
                }
            }
        });
        
        /* Patch button */
        JButton dnlPatch = new JButton("Download Latest Patch");
        dnlPatch.setFont(font);
        getContentPane().add(dnlPatch, "cell 1 5,growx");
        dnlPatch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            patchField.setText(downloadPatch().getAbsolutePath());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        
        addDragDrop(patchField);
        /* Patch button */
        JButton btnPatch = new JButton("Patch!");
        btnPatch.setFont(font);
        getContentPane().add(btnPatch, "cell 1 5,growx");
        btnPatch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String originalFile = spigotJarField.getText();
                final String patchFile = patchField.getText();
                final String outputFile = patchFile + ".jar";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Main.main(new String[] { originalFile, patchFile, outputFile });
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        
        areaScrollPane.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(350, 600));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        
        getContentPane().add(areaScrollPane, "cell 1 6,growx");

        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            Main.main(args);
            return;
        }
        try {
            if (!UIManager.getCrossPlatformLookAndFeelClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new MainWindow();
    }

    public void addDragDrop(final JTextField field) {
        new FileDrop(field, new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                field.setText(files[0].getPath());
            }
        });
    }

    public File chooseFile(String title, File currentFolder, final String filter, boolean directory) {
        JFileChooser chooser = new JFileChooser(currentFolder);
        chooser.setDialogTitle(title);
        chooser.setDragEnabled(true);
        chooser.setFileSelectionMode(directory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getPath().endsWith(filter) || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return filter;
            }
        });
        return chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }
    
    public static File downloadPatch() {
        java.util.List<String> allPatches = new ArrayList<String>();
        try {
            System.out.println("\tChecking spigot for the latest patch ...");
            String url = "http://www.spigotmc.org/spigot-updates/";
            // Fetch the index
            URL address = new URL(url);
            HttpURLConnection con = (HttpURLConnection) address
                    .openConnection();
            con.setRequestMethod("GET");

            // Cloudfare doesn't like empty user agents
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String sourceStr = response.toString();
            Matcher m = Pattern.compile("spigot([-+]\\d+)([a-z])\\.bps")
                    .matcher(sourceStr);
            while (m.find()) {
                allPatches.add(m.group()); 
            }
        } catch (Exception ex) {
            // Error
            System.out
                    .println("\tSomething went wrong checking for the latest patch!");
            System.out
                    .println("\tPlease download it manually from: http://spigotmc.org/spigot-updates/");
        }
        if (allPatches.size() != 0)
            try {
                System.out.println("\tDownloading latest file ["
                        + allPatches.get(allPatches.size() - 1) + "] ...");
                URL address = new URL("http://www.spigotmc.org/spigot-updates/"
                        + allPatches.get(allPatches.size() - 1));
                HttpURLConnection con = (HttpURLConnection) address
                        .openConnection();
                con.setRequestMethod("GET");

                // Cloudfare doesn't like empty user agents
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                File file = new File(allPatches.get(allPatches.size() - 1));
                BufferedInputStream bis = new BufferedInputStream(
                        con.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(file.getName()));
                int i = 0;
                while ((i = bis.read()) != -1) {
                    bos.write(i);
                }
                bos.flush();
                bis.close();
                bos.close();
                System.out.println( "Download complete!" );
                return file;
            } catch (Exception ex) {
                // Error
                System.out
                        .println("\tSomething went wrong downloading the latest patch!");
                System.out
                        .println("\tPlease download it manually from: http://spigotmc.org/spigot-updates/");
            }
        return null;
    }
    
    
}
