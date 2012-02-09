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
