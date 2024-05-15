import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final int PORT = 4221;
    public static final String HTTP_OK_Response = "HTTP/1.1 200 OK\r\n\r\n";
    public static final String HTTP_NotFound_Response = "HTTP/1.1 404 Not Found\r\n\r\n";
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
       serverSocket = new ServerSocket(PORT);
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");

       /*
        To write data to client when it requests to server
        OutputStream needed to be used to write the whole httpresponse set along with headers and body and write it to clientSocket
        */

       OutputStream outputStream = clientSocket.getOutputStream();
       String urlPath = getHeaderData(clientSocket.getInputStream());

       if(URLS.checkUrl(urlPath) != null)
           outputStream.write(HTTP_OK_Response.getBytes(StandardCharsets.UTF_8));
       else if(URLS.ifContains(urlPath) != null && urlPath.contains(URLS.ECHO_PAGE.getUrl())) {
           String endpoint = URLS.ifContains(urlPath).getUrl().split("/")[1];
           String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + endpoint.length() + "\r\n\r\n" + endpoint;
           outputStream.write(response.getBytes(StandardCharsets.UTF_8));
       }
       else
           outputStream.write(HTTP_NotFound_Response.getBytes(StandardCharsets.UTF_8));

       outputStream.flush();
       clientSocket.close();
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }

  /*
    Extacting and reading the header data of each request
   */
  private static String getHeaderData(InputStream inputStream){
      String rawheaderData = "";
      String urlPath = null;
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
          urlPath = rawheaderData.split("\n")[0].split(" ")[1];

          if(!urlPath.isEmpty())
              return urlPath;
          else throw new NullPointerException("Didn't find the path in the request");
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
      HOME_PAGE_1("");
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
}
