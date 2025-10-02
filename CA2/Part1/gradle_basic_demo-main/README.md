Gradle Basic Demo
===================

This is a demo application that implements a basic multithreaded chat room server.

The server supports several simultaneous clients through multithreading. When a client connects the server requests a screen name, and keeps requesting a name until a unique one is received. After a client submits a unique name, the server acknowledges it. Then all messages from that client will be broadcast to all other clients that have submitted a unique screen name. A simple "chat protocol" is used for managing a user's registration/leaving and message broadcast.


Prerequisites
-------------

 * Java JDK 17
 * Apache Log4J 2
 * Gradle 8.9 (if you do not use the gradle wrapper in the project)
   

Build
-----

To build a .jar file with the application:

    % ./gradlew build 

Run the server
--------------

Open a terminal and execute the following command from the project's root directory:

    % java -cp build/libs/basic_demo-0.1.0.jar basic_demo.ChatServerApp <server port>

Substitute <server port> by a valid por number, e.g. 59001

Run a client
------------

Open another terminal and execute the following gradle task from the project's root directory:

    % ./gradlew runClient

The above task assumes the chat server's IP is "localhost" and its port is "59001". If you whish to use other parameters please edit the runClient task in the "build.gradle" file in the project's root directory.

To run several clients, you just need to open more terminals and repeat the invocation of the runClient gradle task