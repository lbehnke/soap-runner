/*******************************************************************************
 * Copyright (c) 2013 Lars Behnke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Lars Behnke - initial API and implementation
 ******************************************************************************/

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
    private static final String ACTUAL_DIR_NAME = "actual";
    private static final String ORIGINAL_DIR_NAME = "original";
    private static final String RESPONSE_MARKER = "==== Response ====";
    private static final String REQUEST_MARKER = "==== Request ====";
    private static final String END_MARKER = "==============";
    
    private boolean verbose;
    private boolean dumpVariables;
    private String host;
    private int port;
    private String outputFolderName;
    private File currentResponseDir;
    private File originalResponseDir;
    private File currentRequestDir;
    private File originalRequestDir;
    private File varDumpFile;
    private DocumentFormatter formatter;
    private boolean excludeHttpHeader;

    private VariableExtractor extractor;
    private VariableResolver variableResolver;
    private ReplacementProcessor replacementProcessor;

    public SoapRunner(String host, int port, String outputFolder, String replacementsFileName, String extractorsFileName, 
            boolean excludeHttpHeader, boolean dumpVariables, boolean verbose)
    {
        this.verbose = verbose;
        this.dumpVariables = dumpVariables;
        this.host = host;
        this.port = port;
        this.outputFolderName = outputFolder;
        this.excludeHttpHeader = excludeHttpHeader;
        
        this.formatter = new SoapDocumentFormatter(excludeHttpHeader, verbose);
        this.extractor = new VariableExtractor(extractorsFileName, verbose);
        this.variableResolver = new VariableResolver(verbose);
        this.replacementProcessor = new ReplacementProcessor(variableResolver, replacementsFileName, verbose);
    }

    public void run(File inputFile) throws FileNotFoundException, IOException
    {
        if (verbose)
        {
            Log.line("Processing file " + inputFile.getAbsolutePath());
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
        if (!verbose)
        {
            Log.append("Please wait.");
        }
        
        /* Recreate output folders */
        initFolders();
        
        /* Start working */
        doRun(reader);
        
        if (dumpVariables)
        {
            if (verbose)
            {
                Log.line("Dumping variables to " + varDumpFile);
            }
            VariableStore.getInstance().dump(varDumpFile);
        }
        
        if (!verbose)
        {
            Log.line("\nDone.");
        }
    }

    protected void doRun(Reader reader) throws IOException, UnknownHostException, UnsupportedEncodingException
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
                    writeRequests(headerBuffer, contentBuffer, requestId);
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
                    writeRequests(headerBuffer, contentBuffer, requestId);
                    inRequest = false;
                }
                else if (inResponse)
                {
                    writeResponse(headerBuffer, contentBuffer, requestId, true);
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

    private void writeRequests(StringBuffer headerBuffer, StringBuffer contentBuffer, final String requestId) throws IOException, FileNotFoundException
    {
        List<String> requestLines = createLinesByString(headerBuffer, contentBuffer);
        writeFile(requestId, requestLines, originalRequestDir, null);
        writeFile(requestId, requestLines, currentRequestDir, new ContentHandler(){
            public String processContent(String xml)
            {
                return replacementProcessor.process(xml);
            }});        
    }

    private void writeResponse(StringBuffer headerBuffer, StringBuffer contentBuffer, final String requestId, boolean original)
            throws IOException, FileNotFoundException
    {
        List<String> responseLines = createLinesByString(headerBuffer, contentBuffer);
        writeResponse(responseLines, requestId, original);
    }

    private void writeResponse(List<String> responseLines, final String requestId, final boolean original)
            throws IOException, FileNotFoundException
    {
        File dir = original ? originalResponseDir : currentResponseDir;
        writeFile(requestId, responseLines, dir, new ContentHandler(){
            public String processContent(String xml)
            {
                if (!original)
                {
                    extractor.extractVariables(xml, requestId);
                }
                return xml;
            }});
    }
    
    private List<String> createLinesByString(StringBuffer headerBuffer, StringBuffer contentBuffer) throws IOException
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
            File responses = new File(new File(outputFolderName), "responses");
            File requests = new File(new File(outputFolderName), "requests");
            try
            {
                if (responses.exists())
                {
                    FileUtils.deleteDirectory(responses);
                }
                originalResponseDir = new File(responses, ORIGINAL_DIR_NAME);
                currentResponseDir = new File(responses, ACTUAL_DIR_NAME);
                
                responses.mkdir();
                originalResponseDir.mkdir();
                currentResponseDir.mkdir();
                
                if (requests.exists())
                {
                    FileUtils.deleteDirectory(requests);
                }
                originalRequestDir = new File(requests, ORIGINAL_DIR_NAME);
                currentRequestDir = new File(requests, ACTUAL_DIR_NAME);
                
                requests.mkdir();
                originalRequestDir.mkdir();
                currentRequestDir.mkdir();
                
                varDumpFile = new File(outputFolderName, "variables.out");
            }
            catch (IOException e)
            {
                Log.err("Initializing folders failed. Check permissions.");
                throw e;
            }
        }
    }

    private void appendLine(String line, StringBuffer headerBuffer, StringBuffer contentBuffer, boolean inHeader)
    {
        StringBuffer sb = inHeader ? headerBuffer : contentBuffer;
        sb.append(line);
        sb.append("\n");
    }

    /*
     * Due to a bug in TCPMon the end marker is appended to the last line of the previous section.
     * This must be sorted out...
     */
    private void documentStarts(String line, StringBuffer headerBuffer, StringBuffer contentBuffer, boolean inHeader, String startMarker)
    {
        int endIdx = line.length() - startMarker.length();
        if (endIdx > 0)
        {
            line = line.substring(0, endIdx);
            appendLine(line, headerBuffer, contentBuffer, inHeader);
        }
    }

    @SuppressWarnings("unchecked")
    private void sendRequest(StringBuffer headerBuffer, StringBuffer contentBuffer, final String requestId) throws UnknownHostException, IOException,
            UnsupportedEncodingException
    {
        if (verbose)
        {
            Log.line("Sending server request " + requestId);
        }

        String content = contentBuffer.toString();
        
        /* Apply replacements if applicable */
        content = replacementProcessor.process(content);
        
        /* Set new content length */
        int contentLength = content.getBytes("UTF-8").length;
        headerBuffer.append("Content-Length: " + contentLength + "\n");
        
        Socket socket = new Socket(host, port);
        try
        {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            
            /* Send request */
            String document = headerBuffer.toString() + "\n" + contentBuffer.toString();
            IOUtils.write(document, out, "UTF-8");

            /* Get response */
            List<String> responseLines = IOUtils.readLines(in, "UTF-8");
            
            /* Process response */
            String statusLine = responseLines.size() > 0 ? responseLines.get(0) : null;
            logStatus(statusLine);
            writeResponse(responseLines, requestId, false);
        }
        finally 
        {
            socket.close();
        }
   }
  
    private void logStatus(String status)
    {
        if (status != null)
        {
            if (verbose)
            {
                Log.line(status);
            }
            else
            {
                boolean ok = status.startsWith("HTTP/1.1 2");
                if (ok)
                {
                    Log.append(".");
                }
                else
                {
                    Log.append("!");
                }
            }
        }
    }
    
    private void writeFile(final String requestId, List<String> requestLines, File folder, ContentHandler handler) throws FileNotFoundException
    {
        if (folder != null)
        {
            if (verbose)
            {
                Log.line("Writing request " + requestId + " to " + folder);
            }
            String extension = excludeHttpHeader ? "xml" : "txt";
            File docFile = new File(folder, requestId + "." + extension);
            PrintWriter writer = new PrintWriter(docFile);
            try
            {
                formatter.format(requestLines, writer, handler);
            }
            finally 
            {
                writer.close();
            } 
        }
    }

}
