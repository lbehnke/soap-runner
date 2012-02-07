package org.endiansummer.tools.soaprunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class SoapRunnerApp
{

    public static void main(String[] args) 
    {
        /* Parsing arguments */
        Options options = buildOptions();
        
        boolean verbose = false;
        String inputFile = "input.data";
        String host = "localhost";
        int port = 8080;
        String outputFolder = "responses";
        boolean excludeHttpHeader = false;
        
        CommandLineParser parser = new PosixParser();
        try
        {
            CommandLine cmd = parser.parse( options, args);

            if(cmd.hasOption("?")) {
                printHelp(options);
                System.exit(0);
            }
            if (cmd.hasOption("v")) {
                verbose = true;
            }
            if (cmd.hasOption("f")) {
                inputFile = cmd.getOptionValue("f");
            }
            if (cmd.hasOption("h")) {
                host = cmd.getOptionValue("h");
            }
            if (cmd.hasOption("p")) {
                port = Integer.parseInt(cmd.getOptionValue("p"));
            }
            if (cmd.hasOption("d")) {
                outputFolder = cmd.getOptionValue("d");
            }
            if (cmd.hasOption("x")) {
                excludeHttpHeader = true;
            }
            
        }
        catch (Exception e)
        {
            System.out.println("Parsing command line failed: " + e);
            printHelp(options);
            System.exit(1);
        }
        
        SoapRunner runner = new SoapRunner(host, port, outputFolder, excludeHttpHeader, verbose);
        try
        {
            runner.run(new File(inputFile));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found: " + inputFile);
            System.exit(1);
        }    
        catch (IOException e)
        {
            System.out.println("IO exception while processing " + inputFile + ": " + e);
            System.exit(1);
        }  
    
    }

    private static Options buildOptions()
    {
        Options options = new Options();
        options.addOption("f", "file",    true, "input file (TCPMon output format)");
        options.addOption("v", "verbose", false, "verbose log output");
        options.addOption("h", "host",    true, "remote host");        
        options.addOption("p", "port",    true, "remote port");        
        options.addOption("d", "dir",     true, "response output folder");
        options.addOption("x", "excludeheader", false, "exclude HTTP header");
        options.addOption("?", "help",    false, "print help");
        return options;
    }

    private static void printHelp(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar soaprunner-all-<version>.jar", options );
    }
}
