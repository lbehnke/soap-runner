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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ReplacementProcessor
{
    private boolean verbose = false;
    private VariableResolver variableResolver;
    private List<Replacement> replacements;
    
    public ReplacementProcessor(VariableResolver resolver, String replacementsFileName, boolean verbose)
    {
        this.verbose = verbose;
        this.variableResolver = resolver;
        
        initReplacements(replacementsFileName);
    }
    
    private void initReplacements(String replacementsFile)
    {
        replacements = new ArrayList<Replacement>();
        try
        {
            if (replacementsFile == null)
            {
                replacementsFile = "replacement.properties";
            }
            Properties props = new Properties();
            File file = new File(replacementsFile);
            if (file.exists())
            {
                props.load(new FileReader(file));
                
                String replacementStr = props.getProperty("replacements");
                String[] replacementArr = replacementStr.split(",");
                for (String repl : replacementArr)
                {
                    repl = repl.trim();
                    String regex = props.getProperty(repl + ".regex");
                    String value = props.getProperty(repl + ".replacement");
                    if (regex != null && value != null)
                    {
                        replacements.add(new Replacement(regex, value));
                    }
                }
            }
            else
            {
                if (verbose)
                {
                    Log.line("No replacements file " + replacementsFile + " found.");
                }
            }
        }
        catch (IOException e)
        {
            Log.err("Reading replacements file " + replacementsFile + " failed.");
        }
    }
    
    public String process(String content)
    {
        for (Replacement repl : replacements)
        {
            String regex = repl.getRegex();
            String replacement = variableResolver.resolve(repl.getReplacement());
            content = content.replaceAll(regex, replacement);
        }
        return content;
    }
    
    
    public static class Replacement
    {
        private String regex;
        private String replacement;
        
        public Replacement(String regex, String value)
        {
            this.regex = regex;
            this.replacement = value;
        }
        
        public String getRegex()
        {
            return regex;
        }
        public void setRegex(String regex)
        {
            this.regex = regex;
        }
        public String getReplacement()
        {
            return replacement;
        }
        public void setReplacement(String replacement)
        {
            this.replacement = replacement;
        }
    }
}
