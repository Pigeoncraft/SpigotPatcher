package org.spigotmc.patcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.util.Properties;
import javax.swing.UIManager;
import net.md_5.jbeat.Patcher;

public class Main 
{
    public String version = "0.05";
    private Properties props;
    private String spigotLocation; 
    private MainWindow gui;
    
    
    public void startGui(){
        gui = new MainWindow(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SystemStream();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        loadProperties();
        gui.spigotJarField.setText(spigotLocation);
        
    }
    
    public static void main(String[] args) throws Exception
    {
        if ( args.length == 0 )
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName() );
            Main main = new Main();
            main.startGui();
            return;
        }
        
        if ( args.length != 3 )
        {
            System.out.println( "Welcome to the Spigot patch applicator." );
            System.out.println( "In order to use this tool you will need to specify three command line arguments as follows:" );
            System.out.println( "java -jar SpigotPatcher.jar original.jar patch.bps output.jar" );
            System.out.println( "This will apply the specified patch to the original jar and save it to the output jar" );
            System.out.println( "Please ensure that you save your original jar for later use." );
            System.out.println( "If you have any queries, please direct them to http://www.spigotmc.org/" );
            return;
        }

        File originalFile   = new File( args[0] );
        File patchFile      = new File( args[1] );
        File outputFile     = new File( args[2] );

        if ( !originalFile.canRead() )
        {
            System.err.println( "Specified original file " + originalFile + " does not exist or cannot be read!" );
            return;
        }
        if ( !patchFile.canRead() )
        {
            System.err.println( "Specified patch file " + patchFile + " does not exist or cannot be read!!" );
            return;
        }
        if ( outputFile.exists() )
        {
            System.err.println( "Specified output file " + outputFile + " exists, please remove it before running this program!" );
            return;
        }
        if ( !outputFile.createNewFile() )
        {
            System.err.println( "Could not create specified output file " + outputFile + " please ensure that it is in a valid directory which can be written to." );
            return;
        }

        System.out.println( "Starting patching process, please wait" );
        System.out.println( "Spigot md5 Checksum: " + getMd5OfFile(originalFile.getAbsolutePath( ) ) );
        System.out.println( "Patch  md5 Checksum: " + getMd5OfFile(patchFile.getAbsolutePath( ) ) );

        try
        {
            new Patcher( patchFile, originalFile, outputFile ).patch();
        } catch ( Exception ex )
        {
            System.err.println( "Error occured whilst patching file!" );
            System.err.println( "Please make sure you have build 1649 of Spigot!" );
            ex.printStackTrace();
            outputFile.delete();
            return;
        }

        System.out.println( "\tOutput md5 Checksum: " + getMd5OfFile(outputFile.getAbsolutePath()) );
        System.out.println( "Your new patched Spigot is located in the same directory as your patch file!" );
        System.out.println( "Your file has been patched and verified! Enjoy!" );
        
       
    }
    
    //Get md5 hash of File
    public static String getMd5OfFile(String filePath)
    {
        String returnVal = "";
        try 
        {
            InputStream   input   = new FileInputStream(filePath); 
            byte[]        buffer  = new byte[1024];
            MessageDigest md5Hash = MessageDigest.getInstance("MD5");
            int           numRead = 0;
            while (numRead != -1)
            {
                numRead = input.read(buffer);
                if (numRead > 0)
                {
                    md5Hash.update(buffer, 0, numRead);
                }
            }
            input.close();

            byte [] md5Bytes = md5Hash.digest();
            for (int i=0; i < md5Bytes.length; i++)
            {
                returnVal += Integer.toString( ( md5Bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
        } 
        catch(Throwable t) {t.printStackTrace();}
        return returnVal.toLowerCase();
    }
    
    public void loadProperties() {
        props = new Properties();
        InputStream is = null;
        File f = null;
        try {
            f = new File(getHomeDirectory() + "\\Patcher.properties");
            if (!f.exists()){
                f.createNewFile();
            }
            is = new FileInputStream( f );
        }
        catch ( Exception e ) { 
            e.printStackTrace();
        }
        
     
        try {
            props.load( is );
        }
        catch ( Exception e ) {
            System.err.println( "Error Loading Properties File" );
        }
        
        spigotLocation = props.getProperty("LastSpigotLocation", "");
    }
    
    public static void saveParamChanges(String Key, String Value) {
        try {
            Properties props = new Properties();
            props.setProperty(Key, Value);
            File f = new File(getHomeDirectory() + "\\Patcher.properties");
            OutputStream out = new FileOutputStream( f );
            props.store(out, "This is an optional header comment string");
        }
        catch (Exception e ) {
            e.printStackTrace();
        }
    }
    
    //Getting Home Directory
    public static File getHomeDirectory() {
        String userHome = System.getProperty("user.home");
        if(userHome == null) {
            throw new IllegalStateException("user.home==null");
        }
        File home = new File(userHome);
        File settingsDirectory = new File(home, ".myappdir");
        if(!settingsDirectory.exists()) {
            if(!settingsDirectory.mkdir()) {
                throw new IllegalStateException(settingsDirectory.toString());
            }
        }
        return settingsDirectory;
    }
    
    //Steal console output.
    public void SystemStream()
    {
        
        PipedOutputStream pOut = new PipedOutputStream();
        FileWriter logOut = null;
        
        try
        {
            PipedInputStream pIn = new PipedInputStream(pOut);
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new InputStreamReader(pIn));
            
            System.setOut(new PrintStream(pOut));
            System.setErr(new PrintStream(pOut));
            while(true) 
            {
                try
                {
                    
                    String line = reader.readLine();
                    if(line != null) 
                    {
                        line = line.trim(); 
                        if ( line.length() > 0 )
                        {
                            gui.textArea.append(line + "\n");
                            gui.textArea.setCaretPosition(gui.textArea.getDocument().getLength());
                            
                            if ( logOut != null )
                            {
                                logOut.write( ( line + "\n" ).toCharArray() );
                                logOut.flush();
                            }
                        }
                    }
                    
                } catch (IOException ex) {
                }
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
            
    }
}
