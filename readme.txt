SOAP Runner
-----------

Author: Lars Behnke 

This tool was created to send a series of SOAP requests to an arbitrary server.
It simply takes a dump file created by TCPMon and resends its content.
The original server response (as captured by TCPMon) and the target server response are stored
in a manner that enable one to perform a file/directory diff or compare.

Build binary:
$ mvn assembly:assembly

Display available options:
$ java -jar soaprunner-all-1.0.jar --help

Example call:
$ java -jar soaprunner-all-1.0.jar -v -f tcpmon.txt -d out -h lab1 -p 8280 

