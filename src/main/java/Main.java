import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 4221;
    public static final String HTTP_OK_Response = "HTTP/1.1 200 OK\r\n\r\n";
    public static final String HTTP_NotFound_Response = "HTTP/1.1 404 Not Found\r\n\r\n";
    public static final String USER_AGENT = "User-Agent:";
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
     ExecutorService executorService = Executors.newFixedThreadPool(10);
     ServerSocket serverSocket = null;

     try {
       serverSocket = new ServerSocket(PORT);
       serverSocket.setReuseAddress(true);

       while(true){
           Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
           System.out.println("accepted new connection");
           ClientHandler clientHandler = new ClientHandler(clientSocket, args);
           executorService.execute(clientHandler);
       }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }finally {
         try{
             serverSocket.close();
         }catch (Exception ex){

         }
     }
  }

}
