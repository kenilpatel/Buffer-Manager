#
# Makefile for Minibase Projects
# CS 448, Spring 2006, Purdue University
#
# This Makefile was designed for Linux.
# For Windows (cygwin), change each : to \; in the variables below.
#

SRCPATH = ./src
BINPATH = ./bin

JAVAC = javac  -d $(BINPATH) -cp $(BINPATH)
JAVA  = java -classpath $(BINPATH)

bufmgr:
	$(JAVAC) $(SRCPATH)/bufmgr/*.java


bmtest:
	$(JAVAC)  $(SRCPATH)/tests/BMTest.java
	$(JAVA) tests.BMTest

ptest:
	$(JAVAC)  $(SRCPATH)/tests/PolicyTest.java
	$(JAVA) tests.PolicyTest

ptest2:
	$(JAVAC)  $(SRCPATH)/tests/PolicyTest2.java
	$(JAVA) tests.PolicyTest2

clean: clean_classes clean_backups clean_temps
	rm -rf *.minibase $(BINPATH)/*

clean_classes:
	\find . -name \*.class -exec rm -f {} \;

clean_backups:
	\find . -name \*~ -exec rm -f {} \;

clean_temps:
	\find . -name \#* -exec rm -f {} \;

