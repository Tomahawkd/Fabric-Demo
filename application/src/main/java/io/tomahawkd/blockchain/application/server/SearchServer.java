package io.tomahawkd.blockchain.application.server;

import fi.iki.elonen.NanoHTTPD;
import io.tomahawkd.blockchain.application.user.TransactionHelper;
import io.tomahawkd.blockchain.application.utils.Asset;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchServer extends NanoHTTPD {

    private static SearchServer instance;
    public static final String MAIN_URL = "http://127.0.0.1:8080";
    private final String[] fileList = {"page.html", "index.css", "logo.png"};
    private static final Map<String, String> FILE_TYPE = new HashMap<>();

    static {
        FILE_TYPE.put("html", "text/html");
        FILE_TYPE.put("png", "image/png");
        FILE_TYPE.put("css", "text/css");
    }

    public static SearchServer getInstance() {
        if (instance == null) {
            instance = new SearchServer();
        }
        return instance;
    }

    public static void main(String[] args) throws IOException {
        SearchServer.getInstance().start();
    }

    public void start() throws IOException {
        super.start(SOCKET_READ_TIMEOUT, false);
    }

    private SearchServer() {
        super(8080);
    }

    public static Response newErrorResponse(Response.IStatus status) {
        return newFixedLengthResponse(status, MIME_PLAINTEXT, status.getDescription());
    }

    @Override
    public Response serve(IHTTPSession session) {
        String url = session.getUri().equals("/") ? "/page.html" : session.getUri();
        Method method = session.getMethod();

        //Resources
        if (method == Method.GET && Arrays.stream(fileList).map(e -> "/" + e).anyMatch(e -> e.equals(url))) {
            return newChunkedResponse(
                    Response.Status.OK,
                    FILE_TYPE.get(url.split("\\.")[1]),
                    this.getClass().getResourceAsStream(url));
        }

        // query
        if (method == Method.POST && url.equals("/query")) {
            try {
                session.parseBody(null);
            } catch (IOException e) {
                e.printStackTrace();
                return newErrorResponse(Response.Status.INTERNAL_ERROR);
            } catch (ResponseException e) {
                e.printStackTrace();
                return newFixedLengthResponse(e.getStatus(), MIME_PLAINTEXT, e.getMessage());
            }
            Map<String, List<String>> data = session.getParameters();
            List<String> uid = data.get("uid");
            if (uid == null || uid.isEmpty()) {
                return newErrorResponse(Response.Status.BAD_REQUEST);
            }
            try {
                Asset asset = TransactionHelper.INSTANCE.ReadAsset(uid.get(0));
                System.out.println(asset.toJson());
                return newFixedLengthResponse(Response.Status.OK, "application/json", asset.toJson());
            } catch (Exception exception) {
                if (exception.getMessage().contains("does not exist")) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json",
                            Asset.createErrorAsset().toJson());
                }
                exception.printStackTrace();
                return newErrorResponse(Response.Status.INTERNAL_ERROR);
            }
        }
        return newErrorResponse(Response.Status.NOT_FOUND);
    }
}
