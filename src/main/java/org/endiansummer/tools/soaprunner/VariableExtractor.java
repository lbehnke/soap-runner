package org.endiansummer.tools.soaprunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class VariableExtractor
{
    

    private Map<String, XPathExpression> extractors;
    private XPath xPath;
    private DocumentBuilder docBuilder;
    private boolean verbose;
    
    public VariableExtractor(String extractorFileName, boolean verbose) 
    {
        this.verbose = verbose;
        
        initExtractors(extractorFileName);
    }
    
    public void addExtractor(String key, String xpath) throws XPathExpressionException
    {
        XPathExpression  xPathExpression= xPath.compile(xpath);
        extractors.put(key, xPathExpression);
    }
    
    private void initExtractors(String extractorsFileName)
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory =  DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            docBuilder = docBuilderFactory.newDocumentBuilder();
            XPathFactory xPathFactory=XPathFactory.newInstance();
            xPath=xPathFactory.newXPath();
            extractors = new HashMap<String, XPathExpression>();
            
            if (extractorsFileName == null)
            {
                extractorsFileName = "extractor.properties";
            }
            Properties props = new Properties();
            props.load(new FileReader(new File(extractorsFileName)));
           
            for (Object o : props.keySet())
            {
                String key = o.toString();
                String extractor = props.getProperty(key);
                addExtractor(key, extractor);

            }
        }
        catch (FileNotFoundException e)
        {
            if (verbose)
            {
                Log.line("No extractor file " + extractorsFileName + " found.");
            }
        }
        catch (IOException e)
        {
            Log.err("Processing extractor file " + extractorsFileName + " failed.");
        }
        catch (ParserConfigurationException e)
        {
            Log.err("Initializing xml parser failed.");
        }
        catch (XPathExpressionException e)
        {
            Log.err("Invalid xpath expression. " + e);
        }
    }
    
    public void extractVariables(String xml, String requestId)
    {
        for (Object o: extractors.keySet())
        {
            String key = o.toString();
            XPathExpression xPathExpr= extractors.get(key);
            
            try
            {
                /* In order to avoid additional complexity of xpath expressions we
                 * do not want to deal with namespaces. So we use a document as input 
                 * source created by a builder with namespace awareness being turned off. 
                 * If we used plain XML as input source a default document builder would have been used, 
                 * which is namespace aware.
                 */
                InputSource inputSource = new InputSource(new StringReader(xml));
                Document doc = docBuilder.parse(inputSource);
                
                Node node = (Node)xPathExpr.evaluate(doc, XPathConstants.NODE);
                if (node != null)
                {
                    String str = null;
                    if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE)
                    {
                        str = node.getTextContent();
                    }
                    else if (node.getNodeType() == Node.ELEMENT_NODE && node.getChildNodes().getLength() > 0 && node.getFirstChild().getNodeType() == Node.TEXT_NODE)
                    {
                        str = node.getFirstChild().getTextContent();
                    }
                    if (str != null)
                    {
                        VariableStore.getInstance().addVariable(key, str);
                    }
                }
            }
            catch (XPathExpressionException e)
            {
                Log.err("Evaluation of xpath expression " + xPathExpr + " failed (" + requestId + ").");
            }
            catch (Exception e)
            {
                Log.err("XML Parser exception (" + requestId + ").");
                if (verbose)
                {
                    Log.err("Input document:\n" + xml);
                }
            }
        }

    }

  
    
}
