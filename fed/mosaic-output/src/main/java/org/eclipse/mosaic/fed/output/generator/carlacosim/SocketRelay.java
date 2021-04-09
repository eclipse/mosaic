/**
 * The Socket Relay implements first connect to a server as a client then
 * generate a socket server
 *
 * @author Defu Cui
 * @version 1.0
 * @since 2021-4-1
 */
package org.eclipse.mosaic.fed.output.generator.carlacosim;

import java.net.*;
import java.io.*;


public class SocketRelay {

	// Relay Server port
	int carlaPort = 8913;
    // Relay client port and host
	int sumoPort = 8813;
	String sumoHostName = "localhost";
	
	/**
	 * This is the customized class constructor
	 * 
	 * @param sumoHostName  This is the sumo server host name
	 * @param sumPort   This is the sumo server port
	 * @param carlaPort This is the carla client port
	 */
	public SocketRelay(String sumoHostName, int sumoPort, int carlaPort)
	{
		this.sumoHostName = sumoHostName;
		this.sumoPort = sumoPort;
		this.carlaPort = carlaPort;
	}
	
	public SocketRelay()
	{
		this("localhost", 8813, 8913);
	}

	/**
	 * This method is used to run the relay. 
	 * First connect to the SUMO server as a client and generate a socket server for carla
	 * After Carla connected, the message begins to transfer between too ends.
	 */
	public void Run()
	{
        System.out.println("Socket Relay Start");
		try (Socket sumoSocket = new Socket(sumoHostName, sumoPort)) {
			// Connect to SUMO first.
			InputStream fromSumoinputStream = sumoSocket.getInputStream();
			DataInputStream fromSumoDataInputStream = new DataInputStream(fromSumoinputStream);
			OutputStream toSumoOutputStream = sumoSocket.getOutputStream();
			DataOutputStream toSumoDataOutputStream = new DataOutputStream(toSumoOutputStream);
            System.out.println("Sumo Server connected");
			System.out.println("Wait for Carla client connected");
			
			// Listen to CARLA request.
			try (ServerSocket carlaServerSocket = new ServerSocket(carlaPort)) {
				// Blocking call until CARLA is connected.
				Socket carlaSocket = carlaServerSocket.accept();
                System.out.println("Carla connected");
				try {
					InputStream fromCarlaInputStream = carlaSocket.getInputStream();
					DataInputStream fromCarlaDataInputStream= new DataInputStream(fromCarlaInputStream);
					OutputStream toCarlaOutputStream = carlaSocket.getOutputStream();
					DataOutputStream toCarlaDataOutputStream = new DataOutputStream(toCarlaOutputStream);
                    System.out.println("Begin Data Streaming");
					byte[] buffer = new byte[65535];

					String closeMessage = "";
					while (closeMessage != "Close") {
						// From Carla to Sumo
						int length = fromCarlaDataInputStream.read(buffer);
						toSumoDataOutputStream.write(buffer, 0, length);

						// From Sumo to Carla
						length = fromSumoDataInputStream.read(buffer);
						toCarlaDataOutputStream.write(buffer, 0, length);

						// Check the close message.
					}
                    System.out.println("End Data Streaming");
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				} finally {
					carlaSocket.close();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			} finally {
				sumoSocket.close();
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
	}
}

