package ca.camosun.ICS226;

import java.net.*;
import java.io.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.*;

public class App {

    protected int port;
    protected final String HOST = "";


    protected final int KEY_SIZE = 8;
    protected final int MAX_MSG_SIZE = 160;
    protected final String ERROR_RESPONSE = "NO";
    protected final String OK_RESPONSE = "OK";
    protected final String GET_CMD = "GET";
    protected final String PUT_CMD = "PUT";
    protected final String BLANK = "";


    protected HashMap<String, String> messages = new HashMap<String, String>();

    public App(int port) {
        this.port = port; 
    }

    private String process_put(String s){
        if (s.length() < KEY_SIZE){
            return ERROR_RESPONSE;
        }
        String key = s.substring(0, KEY_SIZE);
        String msg = s.substring(KEY_SIZE);

        if (msg.length() > MAX_MSG_SIZE){
            return ERROR_RESPONSE;
        }

        synchronized(this) {
            messages.put(key, msg);
        }

        return OK_RESPONSE;
    }

    private String process_get(String s){

        //System.out.println(s);

        if (s.length() < KEY_SIZE){
            return BLANK;
        }
        String key = s.substring(0, KEY_SIZE);
        String msg = s.substring(KEY_SIZE);

        //System.out.println(msg);

        if (msg.length() != 0){
            return BLANK;
        }
        
        synchronized(this) {
            msg = messages.get(key);
        }

        if (msg == null){
            return BLANK;
        }
        
        return msg;

    }

    private String process_line(String s){
        String cmd = s.substring(0, PUT_CMD.length());

        if (cmd.equals(PUT_CMD)){
            return process_put(s.substring(PUT_CMD.length()));
        }
        else if(cmd.equals(GET_CMD)){
            return process_get(s.substring(GET_CMD.length()));
        }
        else{
            return ERROR_RESPONSE;
        }
    }


    void delegate(Socket clientSocket) {

        String response;

        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine = in.readLine();
            if (inputLine == null) {
                return;
            }
            synchronized(this) {
                System.out.println(inputLine);
            }

            response = process_line(inputLine);
            out.println(response);

        } catch (Exception e) {
            System.err.println(e);
            System.exit(-1);
        }
    }

    public void serve() {
        try (
            ServerSocket serverSocket = new ServerSocket(port);
        ) {
            while(true) {
                Socket clientSocketCopy = null;
                try {
                    Socket clientSocket = serverSocket.accept();
                    Runnable runnable = () -> this.delegate(clientSocket);
                    Thread t = new Thread(runnable);
                    t.start();
                } catch (Exception e) {
                    System.err.println(e);
                    if (clientSocketCopy != null) {
                        clientSocketCopy.close();
                    }
                    System.exit(-2);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-2);
        } catch (SecurityException e) {
            System.err.println(e);
            System.exit(-3);
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            System.exit(-4);
        } catch (IllegalBlockingModeException e) {
            System.err.println(e);
            System.exit(-6);
        }
    }

    public static void main( String[] args )
	{
		if (args.length != 1) {
			System.err.println("Need <port>");
			System.exit(-99);
		}
		App s = new App(Integer.valueOf(args[0]));
		s.serve();
	}
}