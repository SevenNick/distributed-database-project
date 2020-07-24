RMI_PORT =3345
PROJECT_ROOT = ..
PATH = /usr/bin
JUNIT_PATH = /usr/share/java/junit-4.12.jar
HAMCREST_PATH = /usr/share/java/hamcrest-core-1.3.jar

all : normal_test failure_test

normal_test : NormalTest.class

failure_test: RMFailureTest.class \
              TMFailureTest.class \
              SystemFailureTest.class

run_normal_test : normal_test
	$(PATH)/java -classpath $(PROJECT_ROOT):$(JUNIT_PATH):$(HAMCREST_PATH) -DrmiPort=$(RMI_PORT) org.junit.runner.JUnitCore test.NormalTest

run_rm_failure_test: failure_test
	$(PATH)/java -classpath $(PROJECT_ROOT):$(JUNIT_PATH):$(HAMCREST_PATH) -DrmiPort=$(RMI_PORT) org.junit.runner.JUnitCore test.RMFailureTest

run_tm_failure_test: failure_test
	$(PATH)/java -classpath $(PROJECT_ROOT):$(JUNIT_PATH):$(HAMCREST_PATH) -DrmiPort=$(RMI_PORT) org.junit.runner.JUnitCore test.TMFailureTest

run_system_failure_test: failure_test
	$(PATH)/java -classpath $(PROJECT_ROOT):$(JUNIT_PATH):$(HAMCREST_PATH) -DrmiPort=$(RMI_PORT) org.junit.runner.JUnitCore test.SystemFailureTest

%.class : %.java ReservationSystemTest.java
	$(PATH)/javac -classpath $(PROJECT_ROOT):$(JUNIT_PATH) $< ReservationSystemTest.java

clean:
	/bin/rm *.class

.PHONY : clean
