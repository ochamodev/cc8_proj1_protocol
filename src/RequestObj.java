public record RequestObj(
    String method,
    String path,
    String httpVersion,
    String type,
    int hermesStep,
    boolean isHermesRequest
    ) {

}
