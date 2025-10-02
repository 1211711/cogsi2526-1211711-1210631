/*
 * Author: Luís Nogueira (lmn@isep.ipp.pt)
 * Created on: July 2024
 * 
 * Description:
 * A simple app that launches a multithreaded chat room server. 
*/

package basic_demo;

public class ChatServerApp{

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println("Pass the server port as the sole command line argument");
            return;
        }

        int serverPort = Integer.parseInt(args[0]);

        ChatServer chatServer = new ChatServer(serverPort);
        Thread t = new Thread(chatServer,"Chat Server Main Thread");        
        t.start();

        System.out.println("The chat server is running...");
    }

}