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

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class VariableExtractorTest
{
    private VariableExtractor extractor;
    
    @Before
    public void setup() throws Exception
    {
        extractor = new VariableExtractor("src/test/resources/extractor.properties", true);
        VariableStore.getInstance().clear();
    }

    @Test
    public void testExtractSimpleXml() throws Exception
    {
        String testXml = IOUtils.toString(getClass().getResourceAsStream("/test_simple.xml"));
        extractor.addExtractor("test", "//person[@id='1']/name");
        extractor.extractVariables(testXml, "test1");
        assertEquals(1, VariableStore.getInstance().getVariableCount("test"));
        assertEquals("Lars", VariableStore.getInstance().getVariable("test"));
    }
    
    @Test
    public void testExtractSchemaXml() throws Exception
    {
        String testXml = IOUtils.toString(getClass().getResourceAsStream("/test_schema.xml"));
        
        extractor.extractVariables(testXml, "test2");
        assertEquals(1, VariableStore.getInstance().getVariableCount("moduleName"));
        assertEquals("Module 1", VariableStore.getInstance().getVariable("moduleName"));
        
        extractor.addExtractor("buildNumber", "//getSystemVersionInfosResponse/getSystemVersionInfosReturn/item[2]/moduleBuildNumber");
        extractor.extractVariables(testXml, "test3");
        assertEquals(1, VariableStore.getInstance().getVariableCount("buildNumber"));
        assertEquals("1017", VariableStore.getInstance().getVariable("buildNumber"));
        
        extractor.addExtractor("test3", "//moduleName");
        extractor.extractVariables(testXml, "test2");
        assertEquals(1, VariableStore.getInstance().getVariableCount("test3"));
        assertEquals("Module 1", VariableStore.getInstance().getVariable("test3"));
    }
}
