package com.ceridwen.circulation.SIP.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketDaemon extends Thread{
  private static Log logger = LogFactory.getLog(SocketDaemon.class);
		
  private int port;
  private MessageBroker broker;

  public SocketDaemon(int port, MessageHandler handler)
  {
	  this.port = port;
	  this.broker = new MessageBroker(handler);
  }
  
  public void setStrictChecksumChecking(boolean flag) {
	  this.broker.setStrictChecksumChecking(flag);
  }
  
  public boolean getStrictChecksumChecking() {
	  return this.broker.getStrictChecksumChecking();
  }
  
  public void run () {
    try{
      ServerSocket listener = new ServerSocket(port);
      Socket server;

      logger.info("Server daemon listening on port " + port);
      
      while(true){
        server = listener.accept();
        ConnectionThread thread = new ConnectionThread(server, broker);
        thread.start();
      }
    } catch (IOException ioe) {
      System.out.println("IOException on socket listen: " + ioe);
      ioe.printStackTrace();
    }
  }
}

class ConnectionThread extends Thread {
	private static Log logger = LogFactory.getLog(ConnectionThread.class);

	private Socket server;
    private MessageBroker broker;

    ConnectionThread(Socket server, MessageBroker broker) {
      this.broker = broker;
      this.server=server;
    }

    public void run () {
      try {
        logger.info("New connection from " + server.getInetAddress().toString());
        DataInputStream in = new DataInputStream (server.getInputStream());
        PrintStream out = new PrintStream(server.getOutputStream());
        String input = in.readLine();
        do {
        	out.println(broker.process(input));
        	input = in.readLine();
        } while (input != null);
        server.close();
      } catch (IOException ioe) {
        System.out.println("IOException on socket listen: " + ioe);
        ioe.printStackTrace();
      }
    }
}