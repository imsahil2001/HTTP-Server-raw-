import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

       /*
        To write data to client when it requests to server
        OutputStream needed to be used to write the whole httpresponse set along with headers and body and write it to clientSocket
        */

//       OutputStream outputStream = clientSocket.getOutputStream();
//       HashMap<String, String> headerData = headerDataFromRequest(clientSocket.getInputStream());
//       String urlPath = headerData.get("GET");
//       String userAgent = headerData.get(USER_AGENT);
//
//         if (URLS.ifContains(urlPath) != null && urlPath.contains(URLS.ECHO_PAGE.getUrl())) {
//             String endpoint = urlPath.split("/")[2];
//             endpoint = sanitize(endpoint);
//             String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + endpoint.length() + "\r\n\r\n" + endpoint;
//             outputStream.write(response.getBytes(StandardCharsets.UTF_8));
//         } else if (URLS.ifContains(urlPath) != null && urlPath.contains(URLS.USER_AGENT.getUrl())) {
//             userAgent = sanitize(userAgent);
//             String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + userAgent.length() + "\r\n\r\n" + userAgent;
//             outputStream.write(response.getBytes(StandardCharsets.UTF_8));
//         } else if (URLS.checkUrl(urlPath) != null)
//             outputStream.write(HTTP_OK_Response.getBytes(StandardCharsets.UTF_8));
//         else
//             outputStream.write(HTTP_NotFound_Response.getBytes(StandardCharsets.UTF_8));
//
//       outputStream.flush();
//       clientSocket.close();
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

  /*
    Set of pre-existing hardcoded urls that supported by server
   */
  public enum URLS {
      ECHO_PAGE("/echo"),
      HOME_PAGE("/"),
      HOME_PAGE_1(""),
      USER_AGENT("/user-agent");
      private String url;
      URLS(String url) {
        this.url = url;
      }
      public String getUrl(){
          return url;
      }
      public static URLS checkUrl(String url){
          for (URLS value : values()) {
              if (value.getUrl().equals(url)) {
                  return value;
              }
          }
          return null;
      }

      public static URLS ifContains(String targetUrl){
          for (URLS value : values()) {
              if (targetUrl.contains(value.getUrl())){
                  return value;
              }
          }
          return null;
      }
  }

  private static String sanitize(String target){
      return target.replaceAll("[\r\n]","");
  }
}
