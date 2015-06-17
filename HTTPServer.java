/**
 * Simple HTTP Server App on TCP.
 * Creates a new Server thread for each client request.
 * This implementation was primarily modeled off of the
 * Professor's multi-threaded TCPServer implementation.
 *
 * Paul Ankenman
 * CS3700 Summer 2015 Homework 2
 */

import java.net.*;
import java.io.*;

public class HTTPServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverTCPSocket = null;
        boolean listening = true;

        System.out.println("Starting Server now...");
        try {
            serverTCPSocket = new ServerSocket(7777);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 7777.");
            System.exit(-1);
        }

        System.out.println("Server is ready for requests.");
        while (listening){
            new HTTPServerThread(serverTCPSocket.accept()).start();
        }

        serverTCPSocket.close();
    }
}