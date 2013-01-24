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

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.RandomUtils;

public class VariableResolver
{
    private Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z0-9\\-_\\. \\[\\]]+)\\}");
    private boolean verbose = false;

    
    public VariableResolver(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    public String resolve(String expression)
    {
        StringBuffer resolvedReplacement = new StringBuffer();
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find())
        {
            String variable = matcher.group(1);
            String unresolvedVar = variable; 
            
            if ("random_uuid".equals(variable))
            {
                variable = UUID.randomUUID().toString();
            }
            else if ("random_int".equals(variable))
            {
                variable = "" + RandomUtils.nextInt();
            }
            else if ("current_time".equals(variable))
            {
                variable = new Date().toString();
            }
            else 
            {
                variable = VariableStore.getInstance().getVariable(variable);
                if (variable == null)
                {
                    variable = "";
                }
                else if (verbose)
                {
                    Log.line("Resolved variable " + unresolvedVar + " to " + variable);
                }
            }
            
            matcher.appendReplacement(resolvedReplacement, variable);
        }

        return resolvedReplacement.toString();
    }

    
}
