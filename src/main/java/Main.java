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
           ClientHandler clientHandler = new ClientHandler(clientSocket);
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


  /*
    Extacting and reading the header data of each request
   */
  private static HashMap<String, String> headerDataFromRequest(InputStream inputStream){
      String rawheaderData = "";
      String[] headerDataInParts = null;
      try{
          int c;
          Reader reader = new InputStreamReader(inputStream);
          while((c = reader.read()) != -1){
              System.out.print((char)c);
              rawheaderData += (char)c;
              if(rawheaderData.contains("\r\n\r\n"))
                  break;
          }
          //Reading the headers of a request and extracting the method, url-path & http version
          headerDataInParts = rawheaderData.split("\n");

          HashMap<String, String> HeaderData = new HashMap<>();
          for(int i = 0; i < headerDataInParts.length; i++){
              if(headerDataInParts[i].length() > 1){
                  HeaderData.put(
                          headerDataInParts[i].split(" ")[0],
                          headerDataInParts[i].split(" ")[1]
                  );
              }
          }

          if(HeaderData.size() > 0)
              return HeaderData;
          else throw new NullPointerException("Error while parsing header data from request");
      }catch(NullPointerException nullEx){
          System.out.println("Exception occurred :: " + nullEx);
      }catch(Exception ex){
          System.out.println("Exception occurred :: " + ex);
      }
      return null;
  }

}
