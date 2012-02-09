/*
 *  SOAP Runner
 *  Copyright (C) 2012 Lars Behnke
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.endiansummer.tools.soaprunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class SoapRunnerApp
{

    public static void main(String[] args) 
    {
        String version = getVersion();
        
        Log.line("SOAP Runner " + version + " Copyright (C) 2012 Lars Behnke");
        Log.line("This program comes with ABSOLUTELY NO WARRANTY.");
        Log.line("This is free software, and you are welcome to redistribute it.");
        Log.line("under certain conditions. See license.txt for details.\n");
        
        /* Parsing arguments */
        Options options = buildOptions();
        
        boolean verbose = false;
        String outputFolder = ".";
        String inputFile = "tcpmon.txt";
        String host = "localhost";
        String replacementsFile = null;
        String extractorFile = null;
        int port = 8080;
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
            if (cmd.hasOption("r")) {
                replacementsFile = cmd.getOptionValue("r");
            }
            if (cmd.hasOption("e")) {
                extractorFile = cmd.getOptionValue("e");
            }
            if (cmd.hasOption("x")) {
                excludeHttpHeader = true;
            }
            
        }
        catch (Exception e)
        {
            Log.line("Parsing command line failed: " + e);
            printHelp(options);
            System.exit(1);
        }
        
        SoapRunner runner = new SoapRunner(host, port, outputFolder, replacementsFile, extractorFile, excludeHttpHeader, verbose);
        try
        {
            runner.run(new File(inputFile));
        }
        catch (FileNotFoundException e)
        {
            Log.err("File not found: " + inputFile);
            System.exit(1);
        }    
        catch (IOException e)
        {
            Log.err("IO exception while processing " + inputFile + ": " + e);
            System.exit(1);
        }  
        catch (Exception e)
        {
            Log.err("A problem occurred:  " + e);
            System.exit(1);
        } 
    
    }

    private static String getVersion() 
    {
        Properties props = new Properties();
        try
        {
            props.load(SoapRunnerApp.class.getResourceAsStream("/app.properties"));
        }
        catch (Exception e)
        {
            Log.err("Cannot load version.");
        }
        String version = props.getProperty("version");
        return version == null ? "" : version;
    }

    private static Options buildOptions()
    {
        Options options = new Options();
        options.addOption("f", "file",    true, "input file (TCPMon output format)");
        options.addOption("v", "verbose", false, "verbose log output");
        options.addOption("h", "host",    true, "remote host");        
        options.addOption("p", "port",    true, "remote port");        
        options.addOption("d", "dir",     true, "response output folder");
        options.addOption("r", "replacements",  true, "name of replacements file");
        options.addOption("e", "extractors",  true, "name of xpath extractor file");
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
