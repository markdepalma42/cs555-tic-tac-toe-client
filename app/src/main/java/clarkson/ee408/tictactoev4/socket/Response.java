package clarkson.ee408.tictactoev4.socket;

/**
 * Models all server responses to client requests in the TicTacToe game system.
 * This class serves as the standard response format from server to client,
 * indicating the success or failure of requested operations and providing
 * descriptive messages for user feedback.
 * <p>
 * The server creates Response objects to communicate back to the client, and
 * clients always expect Response objects (or their subclasses) when receiving
 * server communications. This base class is extended by specialized response
 * types like PairingResponse and GamingResponse for specific functionality.
 */
public class Response {
    /**
     * The status of the server response, indicating whether the requested operation
     * completed successfully or encountered an error. Determines how the client
     * should process the response and whether to proceed with subsequent operations.
     */
    private ResponseStatus status;

    /**
     * A descriptive message providing additional information about the response status.
     * Contains human-readable text that can be displayed to users to explain the
     * success or failure of their request, including error details when applicable.
     */
    private String message;

    /**
     * Default constructor that creates a Response with null status and message.
     * Used for initialization before setting specific response values.
     */
    public Response() {
        this(null, null);
    }

    /**
     * Parameterized constructor that creates a Response with specific status and message.
     *
     * @param status the response status indicating success or failure of the operation
     * @param message the descriptive message providing details about the response
     */
    public Response(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Returns the status of this response.
     *
     * @return the ResponseStatus indicating whether the operation succeeded or failed
     */
    public ResponseStatus getStatus() {
        return status;
    }

    /**
     * Returns the descriptive message for this response.
     *
     * @return the message providing details about the response outcome
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the status of this response.
     *
     * @param status the ResponseStatus to set for this response
     */
    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    /**
     * Sets the descriptive message for this response.
     *
     * @param message the descriptive message to set for this response
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
