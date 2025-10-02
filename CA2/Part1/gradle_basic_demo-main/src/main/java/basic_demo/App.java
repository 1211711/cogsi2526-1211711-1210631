/*
 * Author: Luís Nogueira (lmn@isep.ipp.pt)
 * Created on: July 2024
 * 
 * Description:
 * A simple app that launches a chat room server and two clients
 */

package basic_demo;

/**
 */
public class App {
    public String getGreeting() {
        return "\nWelcome to \"Multi-User Chat Application\"!\n";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());

        System.out.println("You can run the server by entering the following command:\n");
        System.out.println("java -cp build/libs/basic_demo-0.1.0.jar basic_demo.ChatServerApp <port number>");

        System.out.println("\nYou can run the client by entering the following command:\n");
        System.out.println("java -cp build/libs/basic_demo-0.1.0.jar basic_demo.ChatServerApp <server IP> <server port number>");

        System.out.println("\nTry this application by first running the server and then several clients");

    }
}
