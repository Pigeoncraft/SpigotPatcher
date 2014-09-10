package org.spigotmc.patcher;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import net.md_5.jbeat.Patcher;

public class Main 
{

    public static void main(String[] args) throws Exception
    {
        
        if (args.length == 0)
        {
            new MainWindow();
            
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
            return;
        }
        
        if ( args.length != 3 )
        {
            System.out.println( "Welcome to the Spigot patch applicator." );
            System.out.println( "In order to use this tool you will need to specify three command line arguments as follows:" );
            System.out.println( "\tjava -jar SpigotPatcher.jar original.jar patch.bps output.jar" );
            System.out.println( "This will apply the specified patch to the original jar and save it to the output jar" );
            System.out.println( "Please ensure that you save your original jar for later use." );
            System.out.println( "If you have any queries, please direct them to http://www.spigotmc.org/" );
            return;
        }

        File originalFile = new File( args[0] );
        File patchFile = new File( args[1] );
        File outputFile = new File( args[2] );

        if ( !originalFile.canRead() )
        {
            System.out.println( "Specified original file " + originalFile + " does not exist or cannot be read!" );
            return;
        }
        if ( !patchFile.canRead() )
        {
            System.out.println( "Specified patch file " + patchFile + " does not exist or cannot be read!!" );
            return;
        }
        if ( outputFile.exists() )
        {
            System.out.println( "Specified output file " + outputFile + " exists, please remove it before running this program!" );
            return;
        }
        if ( !outputFile.createNewFile() )
        {
            System.out.println( "Could not create specified output file " + outputFile + " please ensure that it is in a valid directory which can be written to." );
            return;
        }

        System.out.println( "***** Starting patching process, please wait." );
        System.out.println( "\tInput md5 Checksum: " + Files.hash( originalFile, Hashing.md5() ) );
        System.out.println( "\tPatch md5 Checksum: " + Files.hash( patchFile, Hashing.md5() ) );

        try
        {
            new Patcher( patchFile, originalFile, outputFile ).patch();
        } catch ( Exception ex )
        {
            System.out.println( "Error occured whilst patching file!" );
            System.out.println( "Please make sure you have build 1649 of Spigot!" );
            ex.printStackTrace();
            outputFile.delete();
            return;
        }

        System.out.println( "Your file has been patched and verified! We hope you enjoy using Spigot!" );
        System.out.println( "Your new patched Spigot is located in the same directory as your patch file!" );
        System.out.println( "\tOutput md5 Checksum: " + Files.hash( outputFile, Hashing.md5() ) );
       
    }
    
    public static void SystemStream()
    {
        
        PipedOutputStream pOut = new PipedOutputStream();
        FileWriter logOut = null;
        
        try
        {
            PipedInputStream pIn = new PipedInputStream(pOut);
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new InputStreamReader(pIn));
            
            System.setOut(new PrintStream(pOut));
           //   System.setErr(new PrintStream(pOut));
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
                            MainWindow.textArea.append(line + "\n");
                            MainWindow.textArea.setCaretPosition(MainWindow.textArea.getDocument().getLength());
                            
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
