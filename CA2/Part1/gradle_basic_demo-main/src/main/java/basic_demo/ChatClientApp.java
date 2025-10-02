/*
 * Author: Luís Nogueira (lmn@isep.ipp.pt)
 * Created on: July 2024
 * 
 * Description:
 * A simple app that launches a chat client that connects to a chat room server. 
*/

package basic_demo;

public class ChatClientApp {

    
    public static void main(String[] args) {
        
        if (args.length != 2) {
            System.err.println("Pass the server IP and Port as command line arguments");
            return;
        }

        int serverPort = Integer.parseInt(args[1]);
        ChatClient client = new ChatClient(args[0], serverPort);

        Thread t = new Thread(client,"Chat client thread");
        t.start();
     }
    
}