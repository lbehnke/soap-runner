/*******************************************************************************
 * Copyright (c) 2013 Lars behnke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Lars behnke - initial API and implementation
 ******************************************************************************/

package org.endiansummer.tools.soaprunner;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SoapDocumentFormatter implements DocumentFormatter
{
    private boolean verbose;
    private boolean excludeHttpHeader;

    public SoapDocumentFormatter()
    {
        this(false, false);
    }

    public SoapDocumentFormatter(boolean excludeHttpHeader, boolean verbose)
    {
        this.excludeHttpHeader = excludeHttpHeader;
        this.verbose = verbose;
    }

    @Override
    public void format(List<String> lines, PrintWriter writer, ContentHandler handler)
    {

        if (lines == null || lines.size() == 0)
        {
            return;
        }
        List<String> localLines = new ArrayList<String>(lines);
        processStatusLine(writer, localLines);
        processHeaderLines(writer, localLines);
        processContentLines(writer, localLines, handler);
    }

    private void processStatusLine(PrintWriter writer, List<String> localLines)
    {
        String statusLine = localLines.get(0);
        if (!excludeHttpHeader)
        {
            writer.println(statusLine);
        }
        localLines.remove(0);
    }

    private void processHeaderLines(PrintWriter writer, List<String> localLines)
    {
        List<String> headerLines = new ArrayList<String>();
        while (localLines.size() > 0)
        {
            String line = localLines.get(0);
            localLines.remove(0);

            if (line.length() > 0)
            {
                headerLines.add(line);
            }
            else
            {
                break;
            }
        }

        if (!excludeHttpHeader)
        {
            Collections.sort(headerLines);
            for (String header : headerLines)
            {
                writer.println(header);
            }
            writer.println();
        }
    }

    private void processContentLines(PrintWriter writer, List<String> localLines, ContentHandler contentHandler)
    {
        StringBuffer unformattedContent = new StringBuffer();
        for (String string : localLines)
        {
            unformattedContent.append(string);
            unformattedContent.append("\n");
        }
        String formattedXml = formatXml(unformattedContent.toString());
        
        if (contentHandler != null)
        {
            formattedXml = contentHandler.processContent(formattedXml);
        }
        writer.println(formattedXml.trim());
    }

    private String formatXml(String unformattedXml)
    {
        try
        {
            final Document document = parseXmlString(unformattedXml.trim());
            if (document != null)
            {
                OutputFormat format = new OutputFormat(document);
                format.setLineWidth(80);
                format.setIndenting(true);
                format.setIndent(2);
                Writer out = new StringWriter();
                XMLSerializer serializer = new XMLSerializer(out, format);
                serializer.serialize(document);
                return out.toString();
            }
            else
            {
                return unformattedXml;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Document parseXmlString(String str)
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(str));
            return db.parse(is);
        }
        catch (Exception e)
        {
            if (verbose)
            {
                Log.err("Parsing XML failed:\n" + str + "\n" + e);
            }
            return null;
        }

    }

}
