# README #

### Authors ###
- Sviatoslav Sivov  
- Oleg Vyshnyvetskyi  
- Shikha Soni

### What is this repository for? ###
This repository contains our group project for the RIT Distributed Systems (CSCI 652-01) class.

### Project description ###

### Module info ###

CLIENT:  
- Client files are contained in the Client package/folder.  
- Main method is contained in the Main.java class  
[Client communication protocol](https://bitbucket.org/internationals/distributed-load-balancing/src/master/Distributed%20Load%20Balancing/docs/Client%20communication.txt) 

SERVER:  
- Server files are contained in the Server package/folder.  
- Main method is contained in the PubSub.java class  
[Server communication protocol](https://bitbucket.org/internationals/distributed-load-balancing/src/master/Distributed%20Load%20Balancing/docs/Server%20communication.txt)

WORKER:  
- Worker files are contained in the Worker package/folder.  
- Main method is contained in the Main.java class  
[Worker communication protocol](https://bitbucket.org/internationals/distributed-load-balancing/src/master/Distributed%20Load%20Balancing/docs/Worker%20communication.txt)

To compile:
-Run "javac */*.java" from within "src" folder 
To run:
You need to launch at least 3 applications: Server, Client, Worker(s) 
-Launch the server using "java Server.PubSub" 
NOTE: the port 19999 has to be available for the server to accept client's 
connections. The default port can be changed (impl.Constants.SERVER_PORT)
-Launch the server using "java Server.PubSub" 
NOTE: the port 19999 has to be available for the server to accept client's 
connections. The default port can be changed (impl.Constants.SERVER_PORT)

-Launch the worker using "java Worker.Main"
-Enter the host for the server and the port that the server is using for 
	incoming connections
 
 -Launch the client using "java Client.Main"
-Enter the host for the server and the port that the server is using for 
	incoming connections
-Select option 1 to connect to the server and request a new ID; select option 2
if you wish to connect as one of pre-existing clients
-Follow the UI menu to submit the tasks for execution

The deliverables contain 3 different tasks. See the content of "Tasks" package 
for the details.
You can create your own computational tasks. See Common.Task class for 
additional details. See the examples in Tasks package.   