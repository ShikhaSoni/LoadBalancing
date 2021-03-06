package Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import Common.Constants;
import Common.ExternalSocketCloser;
import Common.Message;
import Common.Performance;
import Common.Task;

public class Worker {

	/** ID of the client */
	private volatile int id;
	
	/** Keeps count of how many tasks are active */
	private volatile int activeTaskCount;
	
	/** Port, on which client is listening */
	private final int listeningPort;
	
	/** Flag, indicating that server knows this client under the stated ID */
	private volatile boolean isRegistered = false;
	
	/** Server's address */
	private InetAddress serverAddress;
	
	/** Server's port */
	private int serverPort;
	
	private ExecutorCompletionService<Task> executor = null;
	
	LinkedBlockingQueue< Future<Task> > completedTasks = 
			new LinkedBlockingQueue< Future<Task> >();
	
	/**
	 * Create a client 'without' ID
	 * Default ID will be 0, will be re-assigned
	 * a real ID by the server at initial contact
	 * @param _port - port, on which client receives messages
	 */
	public Worker(int _port) {
		listeningPort = _port;
		id = Constants.NULL_ID;
		
		executor = new ExecutorCompletionService<Task>
				(Executors.newFixedThreadPool(Constants.THREADS_IN_WORKER), completedTasks);
		
	}
	
	/**
	 * Create a client with the specific ID
	 * If this ID doesn't get approved by the server, 
	 * it will be assigned a new ID by the server at initial contact
	 * @param _port - port, on which client receives messages
	 */
	public Worker(int _id, int _port) {
		listeningPort = _port;
		id = _id;
	}
	
	/**
	 * Accessor
	 * @return - server's address
	 */
	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	/**
	 * Accessor
	 * @return - server's port number
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	/**
	 * Accessor
	 * @return - port, on which client receives data
	 */
	public int getListeningPort() {
		return listeningPort;
	}
	
	/**
	 * Accessor
	 * @return - client ID
	 */
	public int getId() {
		return id;
	}
	
	public LinkedBlockingQueue< Future<Task> > getResultsQueue() {
		return completedTasks;
	}
	
	public int getActiveTaskCount() {
		return activeTaskCount;
	}
	
	/**
	 * Is client recognized by the server ?
	 * @return true, if recognized,
	 *         false, otherwise
	 */
	public boolean isRegistered() {
		return isRegistered;
	}
	
	/**
	 * Assign server's connection information to the client
	 * @param address - server's ip address
	 * @param port - server's receiving port
	 */
	public void setServerInfo(InetAddress address, int port) {
		serverAddress = address;
		serverPort = port;
	}
	
	/**
	 * Method used, when server assigns a new ID to the client
	 * @param newId - client's new ID
	 */
	public void assignId(int newId) {
		if (id != newId) {
			id = newId;
		}
		isRegistered = true;
		System.out.println("Obtained id: " + newId);
	}
	
	/**
	 * New task was received. Increment counter of held tasks and submit it
	 * to the thread pool
	 * @param task - received task
	 */
	public void newTask(Task task) {
		System.out.println("\nSubmitting task Id " + task.getId());
		activeTaskCount++;
		executor.submit(task);
	}
	
	/**
	 * When task is completed, decrement counter of uncompleted tasks
	 */
	public void taskCompleted() {
		activeTaskCount--;
	}
	
	/**
	 * Sends a new task to the server for computation
	 * @param taskId - id of the completed task
	 * @param result - result of this task
	 * @param cpuShareUsed - fraction of CPU used by this task during execution
	 */
	public void sendResult(int taskId, Object result, Double cpuShareUsed) {
		try {
			Socket socket = new Socket(serverAddress, serverPort);
			ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
			Message resultMessage = new Message(Constants.RequestType.RESULT, id, taskId, result, cpuShareUsed);
			ooStream.writeObject(resultMessage);
			ooStream.flush();
			socket.shutdownOutput();
			socket.shutdownInput();
			socket.close();
		} catch (Exception e) {
			System.out.println("Sending task failed...");
		}
	}

	/**
	 * Implements initial communcation with the server workflow
	 *  - client sends sync with its id to the server
	 *  - server responds with another sync message, containing assigned id
	 * If something goes wrong in the workflow, connection is terminated
	 * @param syncId - client's self-assigned id
	 * @return true, if sync was successful and server-assigned id received
	 * 		   false, otherwise
	 */
	public boolean sync(int syncId) {
		boolean syncSuccessful = false;
		Message helloMsg = new Message(Constants.RequestType.SYNC, syncId, listeningPort, Constants.ClientType.WORKER);
		Socket socket = null;
		try {
			socket = new Socket(serverAddress, serverPort);
			ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
			ooStream.writeObject(helloMsg);
			System.out.println("Message sent! Command: " + helloMsg.getCommand());
		} catch (Exception e) {
			System.out.println("Sending failed...Command: " + helloMsg.getCommand());
			return false;
		}
		Timer timer = new Timer();
		try {
			ExternalSocketCloser interrupter = new ExternalSocketCloser(socket);
			ObjectInputStream oiStream = new ObjectInputStream(socket.getInputStream());
			while (! syncSuccessful) {
				timer.schedule(interrupter, 3000);
				Message serverMsg = (Message) oiStream.readObject();
				if (serverMsg.getCommand().equals(Constants.RequestType.SYNC)) {
					int id = (int) serverMsg.getArgs()[0];
					this.assignId(id);
					syncSuccessful = true;
				}
			}
			socket.close();
		} catch (IOException e) {
			System.out.println("SYNC interrupted.");
		} catch (ClassNotFoundException e) {
			// ignore
		} finally {
			timer.cancel();
		}
		return syncSuccessful;
	}
	
	public void sendStat(Performance stats) throws IOException {
		Socket socket = new Socket(serverAddress, serverPort);
		ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
		Message statMessage = new Message(Constants.RequestType.STATS, id, stats);
		ooStream.writeObject(statMessage);
		ooStream.flush();
		socket.shutdownOutput();
		socket.shutdownInput();
		socket.close();
	}
}
