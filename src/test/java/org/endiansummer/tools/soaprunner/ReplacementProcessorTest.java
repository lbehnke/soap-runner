package org.endiansummer.tools.soaprunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ReplacementProcessorTest
{
    private ReplacementProcessor processor;

    @Before
    public void setup() throws Exception
    {
        VariableResolver resolver = new VariableResolver(true);
        processor = new ReplacementProcessor(resolver, "src/test/resources/replacement.properties", true);
        VariableStore.getInstance().clear();
    }
    
    @Test
    public void testProcessVariable() throws Exception
    {
        VariableStore.getInstance().addVariable("var", "newVar");
        String result = processor.process("This is the oldVar.");
        assertEquals("This is the newVar.", result);
    }
    
    @Test
    public void testProcessDynamicData() throws Exception
    {
        String result = processor.process("This is the dynamicData.");
        assertTrue(result.indexOf("${") < 0);
    }

}
