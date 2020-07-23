RMIREGPORT =3345
PROJECTROOT = ..
PATH = /usr/bin
JUNIT_PATH = /usr/share/java/junit-4.12.jar
HAMCREST_PATH = /usr/share/java/hamcrest-core-1.3.jar

all : normal

normal: NormalTest.class

runnormaltest : normal
	$(PATH)/java -classpath $(PROJECTROOT):$(JUNIT_PATH):$(HAMCREST_PATH) -DrmiPort=$(RMIREGPORT) org.junit.runner.JUnitCore test.NormalTest

%.class : %.java
	$(PATH)/javac -classpath $(PROJECTROOT):$(JUNIT_PATH) $<

.PHONY : runnormaltest
