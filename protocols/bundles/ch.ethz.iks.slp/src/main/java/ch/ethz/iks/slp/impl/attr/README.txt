Classes in gen/ have been generated with aParse [1]. Whenever the ABNF (attributes.abnf) change, they have to be regenerated with:

java -cp /path/to/aParse.jar -package ch.ethz.iks.slp.impl.attr.gen -java 1.4 attributes.abnf

ParserException and Parser have been modified though, check CVS diff!!!

[1] http://www.parse2.com/index.shtml