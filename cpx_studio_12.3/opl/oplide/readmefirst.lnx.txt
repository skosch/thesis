IBM SDK for Linux, Java Technology Edition, Version 6
=====================================================

This READMEFIRST file applies to Version 6, and to all subsequent
releases, modifications, and service refreshes, until otherwise indicated
in a new READMEFIRST file.

This READMEFIRST file provides release notes that were not incorporated
into the User Guides.  This file must be read in conjunction with any User
Guides.

The SDK provided in this release is functionally equivalent to the Sun FCS
version of Java 6 Update 21 build 06.

IBM provides additional content with the SDK.


Known problems:
---------------

 - There is no ECC provider.

 - In the XL TXE-J XSLT compiler:
   - A low split limit might cause compilation errors
   - It is not recommended to call Java extension functions that have side
     effects. The order of execution is not guaranteed.
   - Versions of Ant prior to 1.7.0 will not work with the XL TXE-J compiler.
     Use the XSLT4J interpreter instead:
     -Djavax.xml.transform.TransformerFactory=
       org.apache.xalan.processor.TransformerFactoryImpl.

 - When using the Attach API directly or through tools such as JConsole,
   you might have the following exception:
	com.ibm.tools.attach.AttachNotSupportedException: acknowledgment timeout from 1234 on port 5678
   This is the result of Attach API resources left by crashed processes.
   To work around this issue, do the following:
   1) Go to the system temporary directory, for example:
	   /tmp
   2) Go into the directory:
	   .com_ibm_tools_attach
      Note: There is a full-stop at the start of the directory name.
      You see a number of directories, each corresponding to a Java process
      and typically identified by the process ID.
   3) Delete all the directories *except* those for the active Java processes
      you wish to attach to; for example process 1234 from the above exception.
   If you accidentally delete a directory that corresponds to a running process,
   the only effect is that you cannot attach to that process.

 - When using the Web Start Secure Static Versioning (SSV) feature with IBM Java 1.4.2 SDK (Linux IA32 only), 
   you might see an error containing the following message:
              java.lang.NoSuchMethodError: sun.net.www.ParseUtil: method encodePath(Ljava/lang/String;Z)Ljava/lang/String; not found
   This limitation will be resolved after IBM Java 1.4.2 SR13 FP4.

IDLJ compiler:
--------------

There is a  new version of the idlj compiler that ensures that the generated code 
is "thread safe". That is, multiple threads can access the code concurrently without 
deadlock situations occurring. You must recompile your IDL source code to take advantage 
of this capability.


IBM Monitoring and Diagnostic Tools for Java - Health Center:
-------------------------------------------------------------

You can enable IBM Monitoring and Diagnostic Tools for Java - Health Center
by using the -Xhealthcenter flag. This increases the native memory requirements
on a per thread basis, and so might cause problems if used on a system
working with limited memory constraints such as the available addressable memory.
