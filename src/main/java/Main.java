import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
       serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");

       /*
        To write data to client when it requests to server
        OutputStream needed to be used to write the whole httpresponse set along with headers and body and write it to clientSocket
        */

       OutputStream outputStream = clientSocket.getOutputStream();
       outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));

       outputStream.flush();
       clientSocket.close();
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
