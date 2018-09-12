// ChatServer.java
// Description: Server side for SCP Chat Application
//
// Author: Lee Humes c3256223

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ChatServer 
{
    public static void main(String[] args) throws IOException
    {
        ChatServer serv = new ChatServer();
        serv.run(args);
    }

    public void run(String[] args) throws IOException
    {
        //Initialize local variables
        String host;
        int port;
        String welcome;
        Scanner keyboard = new Scanner(System.in);

        //Use default options if specs not given
        if (args.length != 3) 
        {
            host = "localhost";
            port = 3400;
            welcome = "Welcome to SCP";
        }

        //Set variables based on user specifcations
        else 
        {
            host = args[0];
            port = Integer.parseInt(args[1]);
            welcome = args[2];
        }

        //Creates address object to pass to SocketServer
        InetAddress address = InetAddress.getByName(host);

        ServerSocket server = new ServerSocket(port, 1, address);

        //Loops until user terminates server
        while (3 > 2) 
        {
            System.out.println("\nWaiting for client to connect...");

            //Establishes connection to client
            Socket client = server.accept();
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String input = "";
            String output = "";

            System.out.println("\nAwaiting connection request...\n");

            long requestTime = 0;
            String userName = "";

            //Begins reading connection message from client
            input = in.readLine();
            System.out.println("Client:");

            //Loops until end of SCP message
            while (!input.equals("SCP END")) 
            {
                System.out.println(input);
                if (!input.equals("SCP CONNECT") && !input.equals("SCP END")) 
                {
                    //Takes values for request time and username from message
                    String[] split = input.split(" ");
                    if (split[0].equals("REQUESTCREATED")) 
                    {
                        requestTime = Long.valueOf(split[1]);
                    } 
                    else if (split[0].equals("USERNAME")) 
                    {
                        userName = split[1];
                    }
                }
                input = in.readLine();
            }
            System.out.println(input);

            //Establish time difference between request and processing
            long time = System.currentTimeMillis();
            long diff = time - requestTime;

            System.out.println("\nRequest received, sending response...");

            System.out.println("\nServer:");

            //Send reject message if time difference is over 5 seconds
            if (diff > 5000) 
            {
                output += "SCP REJECT\n";
                output += "TIMEDIFFERENTIAL " + diff + "\n";
                output += "REMOTEADDRESS " + client.getLocalAddress() + "\n";
                output += "SCP END";
                System.out.println(output);
                out.println(output);
                client.close();

                //Resets the loop to await a new connection 
                continue;
            } 
        
            //Send accept message if not
            else 
            {
                output += "SCP ACCEPT\n";
                output += "USERNAME " + userName + "\n";
                output += "CLIENTADDRESS " + client.getLocalAddress() + "\n";
                output += "CLIENTPORT " + client.getLocalPort() + "\n";
                output += "SCP END";
                System.out.println(output);
                out.println(output);
            }

            System.out.println("\nWaiting for client response...\n");

            //Begins to read client acknowledge message
            input = in.readLine();
            System.out.println("Client:");

            //Loops until end of SCP message
            while (!input.equals("SCP END")) 
            {
                System.out.println(input);
                input = in.readLine();
            }
            System.out.println(input);

            System.out.println("\nAcknowledged, sending welcome message...\n");

            //Sends welcome message as SCP CHAT to client
            output = "";
            output += "SCP CHAT\n";
            output += "REMOTEADDRESS " + client.getLocalAddress() + "\n";
            output += "REMOTEPORT " + client.getLocalPort() + "\n";
            output += "MESSAGECONTENT\n";
            output += "\n\n";
            output += welcome;
            output += "\nSCP END";

            System.out.println("Server:");
            System.out.println(output);
            out.println(output);

            System.out.println("\nWaiting for client response...");

            //Loops chat cycle until disconnect message sent or received
            String state = "";
            while (!state.equals("DISCONNECT")) 
            {
                //Read incoming message from client
                input = in.readLine();
                if (input.equals("SCP DISCONNECT")) 
                {
                    state = "DISCONNECT";
                }

                System.out.println("\nClient:");

                //Loops until end of SCP message
                while (!input.equals("SCP END")) 
                {
                    System.out.println(input);
                    input = in.readLine();
                }
                System.out.println(input);

                //Breaks loop if client sent DISCONNECT to send acknowledgement
                if (state.equals("DISCONNECT")) 
                {
                    break;
                }

                System.out.println("\nEnter message, or 'DISCONNECT' to end chat:");
                System.out.println("Press enter to begin new line. Enter 'END' to finish message");

                String message = "";
                String current = "";

                //Take input for message to send to client
                while (!current.equals("END") && !current.equals("DISCONNECT")) 
                {
                    current = keyboard.nextLine();
                    if (!current.equals("END") && !current.equals("DISCONNECT")) 
                    {
                        message += current + "\n";
                    }
                }

                //Run to send chat message to client
                if (!current.equals("DISCONNECT")) 
                {
                    input = "";
                    input += "SCP CHAT\n";
                    input += "REMOTEADDRESS " + client.getLocalAddress() + "\n";
                    input += "REMOTEPORT " + client.getLocalPort() + "\n";
                    input += "MESSAGECONTENT\n";
                    input += "\n\n";
                    input += message;
                    input += "SCP END";

                    System.out.println("\nServer:");
                    System.out.println(input);
                    out.println(input);

                    System.out.println("\nAwaiting response...");
                }

                //Run to send disconnect message to client
                else 
                {
                    state = "DISCONNECT";
                    input = "";
                    input += "SCP DISCONNECT\n";
                    input += "SCP END";
                    System.out.println("\nServer:");
                    System.out.println(input);
                    out.println(input);

                    //Wait for acknowledge message from client
                    System.out.println("Awaiting acknowledgement...");
                    input = in.readLine();
                    System.out.println("\nClient:");

                    //Loops until end of SCP message
                    while (!input.equals("SCP END")) 
                    {
                        System.out.println(input);
                        input = in.readLine();
                    }
                    System.out.println(input);

                    //Break loop and return to waiting for client
                    System.out.println("\nAcknowledgment received. Now exiting...");
                    client.close();
                    break;
                }
            }

            //Runs when client sends disconnect
            //Sends acknowledge message to client and terminates connection
            System.out.println("\nClient requested disconnect, sending acknowledgment...");
            output = "";
            output += "SCP ACKNOWLEDGE\n";
            output += "SCP END";
            System.out.println("\nServer:");
            System.out.println(output);
            out.println(output);

            client.close();
        }
    }
}