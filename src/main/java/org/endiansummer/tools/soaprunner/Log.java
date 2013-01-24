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

public class Log
{
    public static void line(String ln)
    {
        System.out.println(ln);
    }
    
    public static void append(String ln)
    {
        System.out.print(ln);
    }
    
    public static void err(String ln)
    {
        System.err.println("ERROR: " + ln);
    }
}
