/**
 * Client app for sending basic HTTP requests over TCP and receiving responses.
 * If the request is properly formed, this saves the body of the Server's
 * response to a file in the current directory.
 *
 * End of Response Header is detected with a single empty new line.
 * Contents of response header are assumed to be the main line, date and server.
 * End of message is detected with 4 empty new lines.
 *
 * DNS or IP can be included in the command line when running this software,
 * but is not necessary as the class will request such information initially
 * upon startup.
 *
 * Paul Ankenman
 * CS3700 Summer 2015 Homework 2
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.String;

public class HTTPClient {
    public static void main(String[] args) throws IOException {

        String dns;
        boolean cont = true;
        int endOfMessageDetector = 0;
        Socket tcpSocket = null;
        PrintWriter socketOut = null;
        BufferedReader socketIn = null;
        Scanner scanner = new Scanner(System.in);

        List<String> ERROR_CODES = new ArrayList<String>(Arrays.asList("400", "404"));

        if (args.length != 1) {
            System.out.println("DNS name/IP of the HTTP server: ");
            dns = scanner.next();
        } else {
            dns = args[0];
        }

        try {
            long beginSocketTime = System.currentTimeMillis();
            tcpSocket = new Socket(dns, 7777);
            long endSocketTime = System.currentTimeMillis();
            System.out.println("\nRTT of establishing TCP connection: " + (endSocketTime - beginSocketTime) + "ms\n");

            socketOut = new PrintWriter(tcpSocket.getOutputStream(), true);
            socketIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Invalid Host: " + dns);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: "  + dns);
            System.exit(1);
        }

        while (cont) {
            String response = "filler text";

            System.out.println("HTTP Method Type: ");
            String method = scanner.next();

            System.out.println("Name of htm file: ");
            String file = scanner.next();

            System.out.println("HTTP Version: ");
            String version = scanner.next();

            System.out.println("User-Agent: ");
            String agent = scanner.next();

            String request =
                    method + " /" + file + " HTTP/" + version + "\r\n" +
                    "Host: " + dns + "\r\n" +
                    "User-Agent: " + agent +
                    "\r\n";

            long startQueryTime = System.currentTimeMillis();
            socketOut.println(request);
            response = socketIn.readLine();
            long endQueryTime = System.currentTimeMillis();
            System.out.println("\nRTT of HTTP Request: " + (endQueryTime - startQueryTime) + "ms\n");

            while (response != null) {
                System.out.println(response);
                if (response != null) {
                    String[] responseArray = response.split(" ");
                    if (responseArray.length >= 2 && responseArray[1].equals("200")) {
                        //detect and print header
                        System.out.println(socketIn.readLine());
                        System.out.println(socketIn.readLine());
                        System.out.println(socketIn.readLine());

                        // begin write of body to file
                        printFile(file);

                        cont = askUserToContinue(scanner);
                        break;
                    } else {
                        System.out.println(socketIn.readLine());
                        System.out.println(socketIn.readLine());
                        System.out.println(socketIn.readLine());
                        cont = askUserToContinue(scanner);
                        break;
                    }
                } else {
                    System.out.println("Server replies nothing!");
                    break;
                }
            }
        }

        scanner.close();
        socketOut.close();
        socketIn.close();
        tcpSocket.close();
    }

    private static boolean askUserToContinue(Scanner scanner) {
        System.out.println("Continue? (yes/no): ");
        String resp = scanner.next();
        if (resp.toLowerCase().equals("yes") || resp.toLowerCase().equals("y")) {
            return true;
        }
        System.out.println("Terminating connection now.");
        return false;
    }

    private static void printFile(String file) {
        File newFile = new File(file);
        int endOfMessageDetector = 0;
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileWriter fw = new FileWriter(newFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            String body;

            while (endOfMessageDetector <= 4) {
                body = socketIn.readLine();
                bw.write(body + "\r\n");
                if (body.isEmpty())
                    endOfMessageDetector += 1;
                else
                    endOfMessageDetector = 0;
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            System.err.println("Failed to write message to file!");
            System.exit(1);
        }
    }
}