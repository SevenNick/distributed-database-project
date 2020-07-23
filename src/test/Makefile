RMI_PORT =3345
PROJECT_ROOT = ..
PATH = /usr/bin
JUNIT_PATH = /usr/share/java/junit-4.12.jar
HAMCREST_PATH = /usr/share/java/hamcrest-core-1.3.jar

all : normal

normal: NormalTest.class

run_normal_test : normal
	$(PATH)/java -classpath $(PROJECT_ROOT):$(JUNIT_PATH):$(HAMCREST_PATH) -DrmiPort=$(RMI_PORT) org.junit.runner.JUnitCore test.NormalTest

%.class : %.java
	$(PATH)/javac -classpath $(PROJECT_ROOT):$(JUNIT_PATH) $<

clean:
	/bin/rm *.class

.PHONY : run_normal_test clean
