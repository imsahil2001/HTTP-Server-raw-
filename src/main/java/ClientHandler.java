import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ClientHandler extends Thread {
    public static final String HTTP_OK_Response = "HTTP/1.1 200 OK\r\n\r\n";
    public static final String HTTP_NotFound_Response = "HTTP/1.1 404 Not Found\r\n\r\n";
    public static final String USER_AGENT = "User-Agent:";
    public Socket clientSocket;
    public String[] args;

    public ClientHandler(Socket socket, String[] args) {
        this.clientSocket = socket;
        this.args = args;
    }

    public ClientHandler(){}

    @Override
    public void run() {
        OutputStream outputStream;
        HashMap<String, String> headerData;
        String urlPath;
        String userAgent;
        try {
            outputStream = clientSocket.getOutputStream();
            headerData = headerDataFromRequest(clientSocket.getInputStream());
            urlPath = headerData.get("GET");
            userAgent = headerData.get(USER_AGENT);

            if (URLS.ifContains(urlPath) != null && urlPath.contains(URLS.ECHO_PAGE.getUrl())) {
                String endpoint = urlPath.split("/")[2];
                endpoint = sanitize(endpoint);
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + endpoint.length() + "\r\n\r\n" + endpoint;
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            } else if (URLS.ifContains(urlPath) != null && urlPath.contains(URLS.USER_AGENT.getUrl())) {
                userAgent = sanitize(userAgent);
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + userAgent.length() + "\r\n\r\n" + userAgent;
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            } else if (URLS.ifContains(urlPath) != null && urlPath.contains(URLS.FILES.getUrl())
                    && args.length == 2 && args[0].equals("--directory")){

                String fileName = urlPath.split("/")[2];
                File file = new File(args[1], fileName);
                if(file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));

                    String line;
                    StringBuilder fileData = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        fileData.append(line);
                    }
                    reader.close();

                    String response = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " + fileData.length() + "\r\n\r\n" + fileData.toString();
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                }else
                    outputStream.write(HTTP_NotFound_Response.getBytes(StandardCharsets.UTF_8));
            } else if (URLS.checkUrl(urlPath) != null)
                outputStream.write(HTTP_OK_Response.getBytes(StandardCharsets.UTF_8));
            else
                outputStream.write(HTTP_NotFound_Response.getBytes(StandardCharsets.UTF_8));

            outputStream.flush();
            clientSocket.close();
        } catch (NullPointerException nullEx) {
            System.out.println("Not received thorough request data");
        }catch (Exception ex) {
            System.out.println("Exception occured while parsing client request"+ ex);
        }
    }


    /*
  Extacting and reading the header data of each request
 */
    private HashMap<String, String> headerDataFromRequest(InputStream inputStream) {
        String rawheaderData = "";
        String[] headerDataInParts = null;
        try {
            int c;
            Reader reader = new InputStreamReader(inputStream);
            while ((c = reader.read()) != -1) {
                System.out.print((char) c);
                rawheaderData += (char) c;
                if (rawheaderData.contains("\r\n\r\n"))
                    break;
            }
            //Reading the headers of a request and extracting the method, url-path & http version
            headerDataInParts = rawheaderData.split("\n");

            HashMap<String, String> HeaderData = new HashMap<>();
            for (int i = 0; i < headerDataInParts.length; i++) {
                if (headerDataInParts[i].length() > 1) {
                    HeaderData.put(
                            headerDataInParts[i].split(" ")[0],
                            headerDataInParts[i].split(" ")[1]
                    );
                }
            }

            if (HeaderData.size() > 0)
                return HeaderData;
            else throw new NullPointerException("Error while parsing header data from request");
        } catch (NullPointerException nullEx) {
            System.out.println("Exception occurred :: " + nullEx);
        } catch (Exception ex) {
            System.out.println("Exception occurred :: " + ex);
        }
        return null;
    }

    /*
      Set of pre-existing hardcoded urls that supported by server
     */
    public enum URLS {
        ECHO_PAGE("/echo"),
        FILES("/files"),
        HOME_PAGE("/"),
        HOME_PAGE_1(""),
        USER_AGENT("/user-agent");
        private String url;

        URLS(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public static URLS checkUrl(String url) {
            for (URLS value : values()) {
                if (value.getUrl().equals(url)) {
                    return value;
                }
            }
            return null;
        }

        public static URLS ifContains(String targetUrl) {
            for (URLS value : values()) {
                if (targetUrl.contains(value.getUrl())) {
                    return value;
                }
            }
            return null;
        }
    }

    private String sanitize(String target) {
        return target.replaceAll("[\r\n]", "");
    }
}
