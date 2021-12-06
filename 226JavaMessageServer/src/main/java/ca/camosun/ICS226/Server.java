package ca.camosun.ICS226;

import java.net.*;
import java.io.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.*;

public class Server {

    protected int port;
    protected final String HOST = "";

    protected final int KEY_SIZE = 8;
    protected final int MAX_MSG_SIZE = 160;
    protected final String ERROR_RESPONSE = "NO";
    protected final String NO_RESPONSE = "NO";
    protected final String OK_RESPONSE = "OK";
    protected final String GET_CMD = "GET";
    protected final String PUT_CMD = "PUT";
    protected final String BLANK = "";

    protected HashMap<String, String> messages = new HashMap<String, String>();

    public Server(int port) {
        this.port = port; 
    }

    /*
    # PURPOSE:
    # Given a string, extracts the key and message, and stores the message in the 'messages' hashmap
    #
    # PARAMETERS:
    # 's' is the string that will be used for the key and message extraction
    #
    # RETURN/SIDE EFFECTS:
    # Returns OK_RESPONSE on success, ERROR_RESPONSE otherwise
    #
    # NOTES:
    # To succeed, the string must be of format "KEYMSG" where KEY is of length KEY_SIZE
    # and MSG does not exceed MAX_MSG_SIZE
    */
    private String process_put(String s){

        if (s.length() < KEY_SIZE){
            return ERROR_RESPONSE;
        }
        String key = s.substring(0, KEY_SIZE);
        String msg = s.substring(KEY_SIZE);
        String exMsg = "";

        if (msg.length() > MAX_MSG_SIZE){
            return ERROR_RESPONSE;
        }

        synchronized(this) {
            exMsg = messages.get(key);
        }

        if (exMsg != null){
            return NO_RESPONSE.concat(exMsg);
        }

        synchronized(this) {
            messages.put(key, msg);
        }

        return OK_RESPONSE;
    }

    /*
    #
    # PURPOSE:
    # Given a string, extracts the key and message from it, and returns the message associated with 
    # the key
    #
    # PARAMETERS:
    # 's' is the string that will be used for the key and message extraction
    #
    # RETURN/SIDE EFFECTS:
    # Returns the message if the extraction succeeded, and b'' otherwise
    #
    # NOTES:
    # To succeed, the string must be of format "KEY" where KEY is of length KEY_SIZE
    #
    */
    private String process_get(String s){

        
        if (s.length() < KEY_SIZE){
            return BLANK;
        }
        String key = s.substring(0, KEY_SIZE);
        String msg = s.substring(KEY_SIZE);

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

    /*
    #
    # PURPOSE:
    # Given a string, parses the string and implements the contained PUT or GET command
    #
    # PARAMETERS:
    # 's' is the string that will be used for parsing
    #
    # RETURN/SIDE EFFECTS:
    # Returns the result of the command if the extraction succeeded, ERROR_RESPONSE otherwise
    #
    # NOTES:
    # The string is assumed to be of format "CMDKEYMSG" where CMD is either PUT_CMD or GET_CMD,
    # KEY is of length KEY_SIZE, and MSG varies depending on the command. See process_put(s)
    # and process_get(s) for details regarding what the commands do and their return values
    #
    */
    private String process_line(String s){
        String cmd = s.substring(0, PUT_CMD.length());

        if (cmd.equals(PUT_CMD)){

            synchronized(this) {
                System.out.println(s);
            }
            
            return process_put(s.substring(PUT_CMD.length()));
        }
        else if(cmd.equals(GET_CMD)){
            return process_get(s.substring(GET_CMD.length()));
        }
        else{
            return ERROR_RESPONSE;
        }
    }

    /*
    #
    # PURPOSE:
    # Given a socket, processes client command (refer to 'process_line' method), closes socket when process is complete 
    # 
    # PARAMETERS:
    # 'clientSocket' is an instance of the Socket class
    # 
    #
    */
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
            
            /* synchronized(this) {
                System.out.println(s);
            } */

            response = process_line(inputLine);
            out.println(response);

        } catch (Exception e) {
            System.err.println(e);
            System.exit(-1);
        }
    }

    /*
    #
    # PURPOSE:
    # Run the server on an infinite loop and run each accepted connection on its own thread
    # 
    # NOTES:
    # Catch the following errors:
    # IOException, SecurityException, IllegalArgumentException, IllegalBlockingModeException
    #
    */
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

    /*
    #
    # PURPOSE:
    # Main function to start the server.
    # Requires a port number
    #
    */
    public static void main( String[] args )
	{
		if (args.length != 1) {
			System.err.println("Need <port>");
			System.exit(-99);
		}
		Server s = new Server(Integer.valueOf(args[0]));
		s.serve();
	}
}