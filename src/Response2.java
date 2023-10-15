import java.util.HashMap;
import java.util.Map;

public class Response2 {
    private String status;
    private String statusMessage;
    private String htmlContent;
    private String contentType;
    private String responseString;
    private byte[] responseBody;
    private Map<String, String> headers;

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getContentType() {
        return contentType;
    }

    public String getResponseString() {
        return responseString;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public long getResponseLength() {
        return this.getResponseLength();
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    private Response2(ResponseBuilder2 builder) {
        this.status = builder.status;
        this.statusMessage = builder.statusMessage;
        this.htmlContent = builder.htmlContent;
        this.contentType = builder.contentType;
        this.responseBody = builder.responseBody;
        this.responseString = builder.responseString;
        this.headers = builder.headers;
    }

    public static class ResponseBuilder2 {
        private String status;
        private String statusMessage;
        private String htmlContent;
        private String contentType;
        private byte[] responseBody;
        private String responseString;
        private long responseLength;
        private static String CRLF = "\r\n";
        private Map<String, String> headers = new HashMap<>();

        public ResponseBuilder2 setStatus(String status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder2 setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public ResponseBuilder2 setHtmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public ResponseBuilder2 setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ResponseBuilder2 setResponseBody(byte[] responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public ResponseBuilder2 setResponseLength(long responseLength) {
            this.responseLength = responseLength;
            return this;
        }

        public ResponseBuilder2 addHeader(String header, String headerValue) {
            this.headers.put(header, headerValue);
            return this;
        }

        public Response2 build() {
            StringBuilder builder = new StringBuilder();
            long contentLength = htmlContent.length();
            if (contentLength == 0) {
                contentLength = responseLength;
            }
            if (responseBody == null) {
                    builder
                    .append(String.format("HTTP/1.1 %s %s", status, statusMessage))
                    .append("\n");
                    for (Map.Entry<String, String> entry: headers.entrySet()) {
                        builder.append(String.format("%s: %s", entry.getKey(), entry.getValue()))
                        .append(CRLF);
                    }
                    this.responseString = builder
                    .append(CRLF)
                    .append(htmlContent.trim())
                    .toString();
            } else {
                contentLength = responseBody.length;
                    builder
                    .append(String.format("HTTP/1.1 %s %s", status, statusMessage))
                    .append("\n");
                    for (Map.Entry<String, String> entry: headers.entrySet()) {
                        builder.append(String.format("%s: %s", entry.getKey(), entry.getValue()))
                        .append(CRLF);
                    }
                    this.responseString = builder
                    .append(CRLF)
                    .toString();
            }
            

            return new Response2(this);
        }
    }

}
