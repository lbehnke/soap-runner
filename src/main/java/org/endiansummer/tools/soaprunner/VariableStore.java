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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class VariableStore
{
    private static VariableStore instance;
    private Map<String, List<String>> variables;
    
    public static VariableStore getInstance()
    {
        if (instance == null)
        {
            instance = new VariableStore();
        }
        return instance;
    }
    
    private VariableStore()
    {
        variables = new HashMap<String,List<String>>();
    }
    
    public String getVariable(String key, int index)
    {
        List<String> list = variables.get(key);
        if (list == null || list.size() == 0 || index >= list.size())
        {
            return null;
        }
        else
        {   
            return list.get(index);
        }
    }
    
    public String getVariable(String key)
    {
        
        String idxStr = StringUtils.substringBetween(key, "[", "]");
        if (idxStr != null)
        {
            key = StringUtils.substringBefore(key, "[").trim();
            idxStr = idxStr.trim();
        }
        
        int index = idxStr != null ? Integer.parseInt(idxStr) : getVariableCount(key) - 1;
        return getVariable(key, index);
    }
    
    public int getVariableCount(String key)
    {
        List<String> list = variables.get(key);
        if (list == null || list.size() == 0)
        {
            return 0;
        }
        else
        {   
            return list.size();
        }
    }

    public void addVariable(String key, String str)
    {
        List<String> list = variables.get(key);
        if (list == null)
        {
            list = new ArrayList<String>();
            variables.put(key, list);
        }
        list.add(str);  
        
    }
    
    public void clear()
    {
        variables.clear();
    }
    
    public void dump(File file) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, List<String>> entry : variables.entrySet())
        {
            String key = entry.getKey();
            List<String> list = entry.getValue();
            StringBuffer vars = new StringBuffer();
            int len = list.size();
            for (String var : list)
            {
                if (vars.length() > 0)
                {
                    vars.append("\n" + StringUtils.repeat(" ", 35));
                }
                vars.append(var);
                if (--len>0)
                {
                    vars.append(" ,\\");
                }
            }
            sb.append(StringUtils.rightPad(key, 32));
            sb.append(" = ");
            sb.append(vars.toString());
            sb.append("\n\n");
        }
        FileUtils.writeStringToFile(file, sb.toString(), "UTF-8");
    }
    
}
