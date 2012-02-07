package org.endiansummer.tools.soaprunner;

import java.io.PrintWriter;
import java.util.List;

public interface DocumentFormatter
{
    void format(List<String> lines, PrintWriter writer);
}
