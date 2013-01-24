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
import java.util.List;

public interface DocumentFormatter
{
    void format(List<String> lines, PrintWriter writer, ContentHandler handler);
}
