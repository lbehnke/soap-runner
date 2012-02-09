/*
 *  SOAP Runner
 *  Copyright (C) 2012 Lars Behnke
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
