public class HermesChunkResponse {
    public int chunk;
    public String greet;
    public byte[] imageBytes;

    public HermesChunkResponse(int chunk, String greet, byte[] imageBytes) {
        this.chunk = chunk;
        this.greet = greet;
        this.imageBytes = imageBytes;
    }

}
