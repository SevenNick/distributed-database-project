RMIREGPORT =3345
PROJECTROOT = ..
PATH = /usr/bin
all : server client

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
         TransactionAbortedException.class \
		 RMManagerCars.class \
		 RMManagerCustomers.class \
		 RMManagerFlights.class \
		 RMManagerHotels.class

client: Client.class

runregistry :
	$(PATH)/rmiregistry -J-classpath -J$(PROJECTROOT) $(RMIREGPORT)

runtm : server
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -Djava.security.policy=./security-policy transaction.TransactionManagerImpl

runrmflights : server
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMFlights -Djava.security.policy=./security-policy transaction.RMManagerFlights

runrmrooms : server
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMRooms -Djava.security.policy=./security-policy transaction.RMManagerHotels

runrmcars : server
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMCars -Djava.security.policy=./security-policy transaction.RMManagerCars

runrmcustomers : server
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMCustomers -Djava.security.policy=./security-policy transaction.RMManagerCustomers

runwc : server
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -Djava.security.policy=./security-policy transaction.WorkflowControllerImpl

runclient : client
	$(PATH)/java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) transaction.Client

clean :
	$(RM) *.class

%.class : %.java
	$(PATH)/javac -classpath $(PROJECTROOT) $<

%_Stub.class : %.java
	$(PATH)/rmic -classpath $(PROJECTROOT) -d $(PROJECTROOT) transaction.$*

.PHONY : runregistry runtm runrmflights runrmrooms runrmcars runrmcustomers runwc runclient clean
