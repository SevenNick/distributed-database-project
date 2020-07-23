RMI_PORT =3345
PROJECT_ROOT = ..
PATH = /usr/bin
all : server client

client: Client.class

server : ResourceManager.class \
         ResourceManagerImpl.class \
         ResourceManagerImpl_Stub.class \
         TransactionManager.class \
         TransactionManagerImpl.class \
         TransactionManagerImpl_Stub.class \
         WorkflowController.class \
         WorkflowControllerImpl.class \
         WorkflowControllerImpl_Stub.class \
         InvalidTransactionException.class \
         TransactionAbortedException.class

runregistry :
	$(PATH)/rmiregistry -J-classpath -J$(PROJECT_ROOT) $(RMI_PORT)

runtm : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -Djava.security.policy=./security-policy transaction.TransactionManagerImpl

runrmflights : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -DrmiName=RMFlights -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmrooms : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -DrmiName=RMRooms -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmcars : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -DrmiName=RMCars -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmcustomers : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -DrmiName=RMCustomers -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmreservations : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -DrmiName=RMReservations -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runwc : server
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) -Djava.security.policy=./security-policy transaction.WorkflowControllerImpl

runclient : client
	$(PATH)/java -classpath $(PROJECT_ROOT) -DrmiPort=$(RMI_PORT) transaction.Client

clean :
	/bin/rm *.class

%.class : %.java
	$(PATH)/javac -classpath $(PROJECT_ROOT) $<

%_Stub.class : %.java
	$(PATH)/rmic -classpath $(PROJECT_ROOT) -d $(PROJECT_ROOT) transaction.$*

.PHONY : runregistry runtm runrmflights runrmrooms runrmcars runrmcustomers runwc runclient clean
