// ChatClient.java
// Description: Client side for SCP Chat Application
//
// Author: Lee Humes c3256223

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient 
{
    public static void main(String[] args) throws IOException
    {
        ChatClient cli = new ChatClient();
        cli.run(args);
    }

    public void run(String[] args) throws IOException
    {
        //Initialize local variables
        String host;
        int port;
        Scanner keyboard = new Scanner(System.in);

        //Use default options if specs not given
        if(args.length != 2)
        {
            host = "localhost";
            port = 3400;
        }

        //Set variables based on user specifcations
        else
        {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        //Attempts to establish connection to server
        Socket newSocket = new Socket(host, port);
        PrintWriter out = new PrintWriter(newSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String serverMessage;
        String userMessage;

        //Take username for connect message to server
        String screenName;
        System.out.println("Please enter your screen name:");
        screenName = keyboard.nextLine();

        //Create and send connect message to server
        long time = System.currentTimeMillis();
        userMessage = "SCP CONNECT\n";
        userMessage += "SERVERADDRESS " + host + "\n";
        userMessage += "SERVERPORT " + port + "\n";
        userMessage += "REQUESTCREATED " + time + "\n";
        userMessage += "USERNAME \"" + screenName + "\"\n";
        userMessage += "SCP END";
            
        System.out.println("\nClient:\n" + userMessage);

        out.println(userMessage);
        userMessage = "";

        System.out.println("\nWaiting for server response...");

        //Checks if connection was accepted
        boolean accept;
        serverMessage = in.readLine();
        if(serverMessage.equals("SCP ACCEPT"))
        {
            accept = true;
        }
        else
        {
            accept = false;
        }

        System.out.println("\nServer:");
        while(!serverMessage.equals("SCP END"))
        {
            System.out.println(serverMessage);
            serverMessage = in.readLine();
        }
        System.out.println(serverMessage);

        //Terminates client and connection if rejected
        if(!accept)
        {
            newSocket.close();
            System.out.println("\nConnection rejected, now exiting...");
            System.exit(0);
        }

        //Create and send connection acknowledgement to server
        System.out.println("\nConnection accepted, sending acknowledgment...");
        System.out.println("\nClient:");
        userMessage += "SCP ACKNOWLEDGE\n";
        userMessage += "USERNAME \"" + screenName + "\"\n";
        userMessage += "SERVERADDRESS " + host + "\n";
        userMessage += "SERVERPORT " + port + "\n";
        userMessage += "SCP END";
        System.out.println(userMessage);
        out.println(userMessage);

        System.out.println("\nWaiting for server respone...");

        //Loops chat cycle until disconnect message sent or received
        String state = "";
        while(!state.equals("DISCONNECT"))
        {
            //Read incoming message from server
            serverMessage = in.readLine();
            if(serverMessage.equals("SCP DISCONNECT"))
            {
                state = "DISCONNECT";
            }

            System.out.println("\nServer:");

            //Loops until end of SCP message
            while(!serverMessage.equals("SCP END"))
            {
                System.out.println(serverMessage);
                serverMessage = in.readLine();
            }
            System.out.println(serverMessage);

            //Breaks loop if server sent DISCONNECT to send acknowledgement
            if(state.equals("DISCONNECT"))
            {
                break;
            }

            System.out.println("\nEnter message, or 'DISCONNECT' to end chat:");
            System.out.println(
                "Press enter to begin new line. Enter 'END' to finish message"
                );
            
            String message = "";
            String current = "";

            //Take input for chat message to send to server
            while(!current.equals("END") && !current.equals("DISCONNECT"))
            {
                current = keyboard.nextLine();
                if(!current.equals("END") && !current.equals("DISCONNECT"))
                {
                    message += current + "\n";
                }
            }

            //Run to send chat message to server
            if(!current.equals("DISCONNECT"))
            {
                userMessage = "";
                userMessage += "SCP CHAT\n";
                userMessage += "REMOTEADDRESS " + host + "\n";
                userMessage += "REMOTEPORT " + port + "\n";
                userMessage += "MESSAGECONTENT\n";
                userMessage += "\n\n";
                userMessage += message;
                userMessage += "SCP END";

                System.out.println("\nClient:");
                System.out.println(userMessage);
                out.println(userMessage);

                System.out.println("\nAwaiting response...");
            }

            //Run to send disconnect message to server
            else
            {
                state = "DISCONNECT";
                userMessage = "";
                userMessage += "SCP DISCONNECT\n";
                userMessage += "SCP END";
                System.out.println("\nClient:");
                System.out.println(userMessage);
                out.println(userMessage);

                //Wait for acknowledge message from server
                System.out.println("Awaiting acknowledgement...");
                serverMessage = in.readLine();
                System.out.println("\nServer:");

                //Loops until end of SCP message
                while(!serverMessage.equals("SCP END"))
                {
                    System.out.println(serverMessage);
                    serverMessage = in.readLine();
                }
                System.out.println(serverMessage);

                //Break loop and terminate client and connection
                System.out.println("\nAcknowledgment received. Now exiting...");
                newSocket.close();
                System.exit(0);
            }
        }

        //Runs when server sends disconnect
        //Sends acknowledge message to server, terminates connection and client
        System.out.println("\nServer requested disconnect, sending acknowledgment...");
        userMessage = "";
        userMessage += "SCP ACKNOWLEDGE\n";
        userMessage += "SCP END";
        System.out.println("\nClient:");
        System.out.println(userMessage);
        out.println(userMessage);

        newSocket.close();
    }
}