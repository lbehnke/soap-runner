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
