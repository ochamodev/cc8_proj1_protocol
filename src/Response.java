public class Response {
    private String status;
    private String statusMessage;
    private String htmlContent;
    private String contentType;
    private String responseString;
    private byte[] responseBody;

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

    private Response(ResponseBuilder builder) {
        this.status = builder.status;
        this.statusMessage = builder.statusMessage;
        this.htmlContent = builder.htmlContent;
        this.contentType = builder.contentType;
        this.responseBody = builder.responseBody;
        this.responseString = builder.responseString;
    }

    public static class ResponseBuilder {
        private String status;
        private String statusMessage;
        private String htmlContent;
        private String contentType;
        private byte[] responseBody;
        private String responseString;
        private static String CRLF = "\r\n";

        public ResponseBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public ResponseBuilder setHtmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public ResponseBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ResponseBuilder setResponseBody(byte[] responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Response build() {
            StringBuilder builder = new StringBuilder();
            int contentLength = htmlContent.length();
            if (responseBody == null) {
                this.responseString = builder
                    .append(String.format("HTTP/1.1 %s %s", status, statusMessage))
                    .append("\n")
                    .append(String.format("Content-type: %s", contentType))
                    .append(CRLF)
                    .append(String.format("Content-length: %d", contentLength))
                    .append(CRLF)
                    .append(CRLF)
                    .append(htmlContent.trim())
                    .toString();
            } else {
                contentLength = responseBody.length;
                this.responseString = builder
                    .append(String.format("HTTP/1.1 %s %s", status, statusMessage))
                    .append("\n")
                    .append(String.format("Content-type: %s", contentType))
                    .append(CRLF)
                    .append(String.format("Content-length: %d", contentLength))
                    .append(CRLF)
                    .toString();
            }
            

            return new Response(this);
        }
    }

}
