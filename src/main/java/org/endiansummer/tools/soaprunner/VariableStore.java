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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (list == null || list.size() == 0)
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
        return getVariable(key, getVariableCount(key) - 1);
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
    
}
