package org.endiansummer.tools.soaprunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SoapRunner
{
    private static final String RESPONSE_MARKER = "==== Response ====";
    private static final String REQUEST_MARKER = "==== Request ====";
    private static final String END_MARKER = "==============";
    
    private boolean verbose = false;
    private String host;
    private int port;
    private String outputFolderName;
    private File currentOutput;
    private File originalOutput;
    private DocumentFormatter formatter;
    
    public SoapRunner(String host, int port, String outputFolder, boolean excludeHttpHeader, boolean verbose)
    {
        this.verbose = verbose;
        this.host = host;
        this.port = port;
        this.outputFolderName = outputFolder;
        this.formatter = new SoapDocumentFormatter(excludeHttpHeader, verbose);
    }
    
    public void run(File inputFile) throws FileNotFoundException, IOException
    {
        if (verbose)
        {
            System.out.println("Processing file " + inputFile.getAbsolutePath());
        }
        FileReader fr = null;
        try
        {
            fr = new FileReader(inputFile);
            run(fr);
        }
        finally
        {
            IOUtils.closeQuietly(fr);
        }
    }

    public void run(Reader reader) throws IOException
    {
        initFolders();
        doRun(reader);
    }

    private void doRun(Reader reader) throws IOException, UnknownHostException, UnsupportedEncodingException
    {
        BufferedReader bis = new BufferedReader(reader);
        String line;
        StringBuffer headerBuffer = new StringBuffer();
        StringBuffer contentBuffer = new StringBuffer();
        boolean inHeader = false;
        boolean inRequest = false;
        boolean inResponse = false;
        int count = 0;
        String requestId = null;
        while ((line = bis.readLine()) != null)
        {
            if (line.endsWith(REQUEST_MARKER)){
                documentStarts(line, headerBuffer, contentBuffer, inHeader, REQUEST_MARKER);
                headerBuffer = new StringBuffer();
                contentBuffer = new StringBuffer();
                inRequest = true;
                inResponse = false;
                inHeader = true;
                requestId = null;
                count++;
            }
            else if (line.endsWith(RESPONSE_MARKER))
            {
                documentStarts(line, headerBuffer, contentBuffer, inHeader, RESPONSE_MARKER);
                if (inRequest)
                {
                    sendRequest(headerBuffer, contentBuffer, requestId);
                }
                headerBuffer = new StringBuffer();
                contentBuffer = new StringBuffer();
                inRequest = false;
                inResponse = true;
                inHeader = true;
            }
            else if (line.equals(END_MARKER))
            {
                if (inRequest)
                {
                    sendRequest(headerBuffer, contentBuffer, requestId);
                    inRequest = false;
                }
                else if (inResponse)
                {
                    List<String> responseLines = createLines(headerBuffer, contentBuffer);
                    writeResponseFile(requestId, responseLines, originalOutput);
                    inResponse = false;
                }
            }
            else if (line.length() == 0 && inHeader)
            {
                inHeader = false;
            }
            else if (!line.startsWith("Content-Length") || !inHeader || !inRequest)
            {
                if (line.startsWith("SOAPAction") && requestId == null)
                {
                    requestId = createRequestIdFromLine(line.substring(11).toLowerCase(), count);
                }
                appendLine(line, headerBuffer, contentBuffer, inHeader);
            }
        }
    }

    private List<String> createLines(StringBuffer headerBuffer, StringBuffer contentBuffer) throws IOException
    {
        List<String> lines = new ArrayList<String>();
        BufferedReader sr = new BufferedReader(new StringReader(headerBuffer + "\n" + contentBuffer));
        try
        {
            String line;
            while ((line = sr.readLine()) != null)
            {
                lines.add(line);
            }
        }
        finally
        {
            sr.close();
        }
        return lines;
    }

    private String createRequestIdFromLine(String line, int count)
    {
        
        if (line == null || line.trim().length() == 0)
        {
            return null;
        }
        else
        {
            String id = line.replaceAll("[\\W]+", "");
            
            return String.format("%03d-%s", new Integer(count), id);
        }
        
    }

    private void initFolders() throws IOException
    {
        if (outputFolderName != null)
        {
            File directory = new File(new File(outputFolderName), "responses");
            if (directory.exists())
            {
                FileUtils.deleteDirectory(directory);
            }
            originalOutput = new File(directory, "original");
            currentOutput = new File(directory, "current");
            
            directory.mkdir();
            originalOutput.mkdir();
            currentOutput.mkdir();
            
        }
    }

    private void appendLine(String line, StringBuffer headerBuffer, StringBuffer contentBuffer, boolean inHeader)
    {
        if (inHeader)
        {
            headerBuffer.append(line);
            headerBuffer.append("\n");
        }
        else
        {
            contentBuffer.append(line);
            contentBuffer.append("\n");
        }
    }

    private void documentStarts(String line, StringBuffer headerBuffer, StringBuffer contentBuffer, boolean inHeader, String startMarker)
    {
        int endIdx = line.length() - startMarker.length();
        if (endIdx > 0)
        {
            line = line.substring(0, endIdx);
            appendLine(line, headerBuffer, contentBuffer, inHeader);
        }
    }

    private void sendRequest(StringBuffer headerBuffer, StringBuffer contentBuffer, String requestId) throws UnknownHostException, IOException,
            UnsupportedEncodingException
    {
        if (verbose)
        {
            System.out.println("Sending server request " + requestId);
        }
        int contentLength = contentBuffer.toString().getBytes("UTF-8").length;
        headerBuffer.append("Content-Length: " + contentLength + "\n");
        String document = headerBuffer.toString() + "\n" + contentBuffer.toString();
        Socket socket = new Socket(host, port);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        IOUtils.write(document, out, "UTF-8");
        @SuppressWarnings("unchecked")
        List<String> responseLines = IOUtils.readLines(in, "UTF-8");
        
        writeResponseFile(requestId, responseLines, currentOutput);
   }

    private void writeResponseFile(String requestId, List<String> responseLines, File folder) throws FileNotFoundException
    {
        if (folder != null)
        {
            if (verbose)
            {
                System.out.println("Writing response " + requestId + " to " + folder);
            }
            File docFile = new File(folder, requestId + ".txt");
            PrintWriter writer = new PrintWriter(docFile);
            try
            {
                formatter.format(responseLines, writer);
            }
            finally 
            {
                writer.close();
            } 
        }
    }

}
