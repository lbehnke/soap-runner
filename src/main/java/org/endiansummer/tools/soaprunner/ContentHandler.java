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

public interface ContentHandler
{
    public String processContent(String xml);
}
