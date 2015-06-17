/**
 * Server Thread for Simple HTTP implementation.
 * Supports requests with the command "GET" only,
 * Other command types are currently unsupported and
 * response is a 400 Bad Request error. If the specified file
 * does not exist, the response is a 404 Not Found error.
 * If the command is "GET" and the file is located, the
 * response is a 200 OK and contains the requested file
 * in the body of the response.
 *
 * End of Header is marked with a single empty new line.
 * End of file is marked with 4 empty new lines.
 *
 * Paul Ankenman
 * CS3700 Summer 2015 Homework 2
 */

import java.net.*;
import java.io.*;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public class HTTPServerThread extends Thread {
    private Socket clientTCPSocket = null;

    public HTTPServerThread(Socket socket) {
        super("HTTPServerThread");
        clientTCPSocket = socket;
    }

    public void run() {

        try {
            System.out.println("New Connection Request");
            PrintWriter cSocketOut = new PrintWriter(clientTCPSocket.getOutputStream(), true);
            BufferedReader cSocketIn = new BufferedReader(
                    new InputStreamReader(
                            clientTCPSocket.getInputStream()));

            String request, response = "EMPTY";


            while ((request = cSocketIn.readLine()) != null) {

                System.out.println(request);
                String requestLine[] = new String[10];
                if (!request.isEmpty()) {
                    requestLine = request.split(" ");
                } else {
                    cSocketOut.println(response);
                }

                if (requestLine.length == 3) {
                    String filePath = requestLine[1].substring(1);
                    if (!requestLine[0].equals("GET")) {
                        // “400 Bad Request” case
                        response = generateHeader(requestLine[2], "400 Bad Request");
                    } else if (requestLine[0].equals("GET") && (new File(filePath).isFile())) {
                        // "200 OK" case
                        response = generateHeader(requestLine[2], "200 OK") +
                                "\r\n" +
                                readFile(filePath) +
                                "\r\n\r\n\r\n\r\n";
                    } else {
                        // “404 Not Found” case
                        response = generateHeader(requestLine[2], "404 Not Found");
                    }
                }
            }

            cSocketOut.close();
            cSocketIn.close();
            clientTCPSocket.close();
            System.out.println("Client Connection Terminated.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateHeader(String version, String message) {
        return version + " " + message + "\r\n" +
                "Date: " + getDate() + "\r\n" +
                "Server: " + getServerInfo() + "\r\n";
    }

    private String getServerInfo() {
        Properties prop = System.getProperties();
        return prop.get("os.name") + "/" + prop.get("os.version");
    }

    private String getDate() {
        Calendar cal = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf.setCalendar(cal);
        return sdf.format(cal.getTime());
    }

    private String readFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while((line = reader.readLine()) != null ) {
            stringBuilder.append(line);
            stringBuilder.append("\r\n");
        }
        return stringBuilder.toString();
    }
}