Copyright (c) 2009  Clark N. Hobbie
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
	Clark N. Hobbie - initial API and implementation

=-=-=-=-=-=-=

This directory contains the files for the native (operating system dependent)
portion of the CLIPC library.  The complete version includes the Java (OS 
independent) files as well.

Any 1.6 Java compiler should suffice to build the Java files.  Development
was done using 1.6.0_12.

To build the native libraries on windows you will need to get a copy of 
mingw which can be found at mingw.org  The version that you are looking at 
was built with version 5.1.4.
   
The linux version was built with the default gcc.  The uname -all for that
system was:

Linux fedora 2.6.27.19-170.2.35.fc10.i686 #1 SMP Mon Feb 23 13:21:22 EST 2009 i686 i686 i386 GNU/Linux

The gcc --version info (minus the licensing info) was:

gcc (GCC) 4.3.2 20081105 (Red Hat 4.3.2-7)

The basic development environment was Eclipse 3.4.2 with the 5.0.2 version 
of the C/C++ environment.

Please send bugs/comments/etc to ltsllc@sourceforge.net and/or post to the 
project forums at https://sourceforge.net/projects/clipc
