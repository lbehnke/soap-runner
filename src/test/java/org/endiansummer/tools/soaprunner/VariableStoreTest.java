package org.endiansummer.tools.soaprunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class VariableStoreTest
{
    @Before
    public void setup() throws Exception
    {
        VariableStore.getInstance().clear();
    }
    
    @Test
    public void testAddGetVariable() throws Exception
    {   
        VariableStore.getInstance().addVariable("list", "A");
        VariableStore.getInstance().addVariable("list", "B");
        VariableStore.getInstance().addVariable("list", "C");
        
        assertEquals(3, VariableStore.getInstance().getVariableCount("list"));
        assertEquals("C", VariableStore.getInstance().getVariable("list[2]"));
        assertEquals("B", VariableStore.getInstance().getVariable("list[1]"));
        assertEquals("A", VariableStore.getInstance().getVariable("list [ 00 ]"));
        assertEquals("C", VariableStore.getInstance().getVariable("list"));
        
        assertNull(VariableStore.getInstance().getVariable("list[100]"));
        assertNull(VariableStore.getInstance().getVariable("notfound"));
    }
}