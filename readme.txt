SOAP Runner
-----------

Author  : Lars Behnke 
Version : ${project.version}

This tool was created to send a series of SOAP requests to an arbitrary server.
It simply takes a dump file created by TCPMon and resends its content.
The original server response (as captured by TCPMon) and the target server response are stored
in a manner that enable one to perform a file/directory diff or compare.

Furthermore, you may extract information from the SOAP response documents by applying custom XPath expressions.
The extracted data is bound to variables, which in turn can be used in subsequent SOAP requests.

Build binaries:
$ mvn clean install assembly:single

Display available options:
$ java -jar soaprunner.jar --help

Example calls:
$ java -jar soaprunner.jar -v -f tcpmon.txt -d out -h lab1 -p 8280 
$ java -jar soaprunner.jar --verbose --file=src/test/resources/tcpmon_short.txt --host=lab1 --port=8280 --dir=target --excludeheader --extractors=src/test/resources/extractor.properties --replacements=src/test/resources/replacement.properties

