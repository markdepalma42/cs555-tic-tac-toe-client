package clarkson.ee408.tictactoev4.socket;

/**
 * Models all client requests sent to the server in the TicTacToe game system.
 * This class serves as the standard communication format between client and server,
 * encapsulating the request type and associated data for all game operations.
 * <p>
 * Clients must create Request objects to communicate with the server, and the server
 * always expects Request objects when receiving client communications. The request
 * type determines the operation to be performed, while the data field contains
 * serialized objects needed for the specific operation.
 */
public class Request {

    /**
     * The type of client request, determining which operation the server should perform.
     * Each request type corresponds to specific game functionality and determines how
     * the data field should be interpreted.
     */
    private RequestType type;

    /**
     * A string representation of serialized data sent by the client. The content and format
     * depend on the request type. Can contain serialized objects of String, Integer, or User classes.
     * For some request types, this field may be null when no additional data is required.
     */
    private String data;

    /**
     * Default constructor that creates a Request with null type and data.
     * Used for initialization before setting specific request parameters.
     */
    public Request() {
        this(null, null);
    }

    /**
     * Parameterized constructor that creates a Request with specific type and data.
     *
     * @param type the type of request being made
     * @param data the serialized data associated with the request, or null if not needed
     */
    public Request(RequestType type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the type of this request.
     *
     * @return the RequestType indicating what operation this request represents
     */
    public RequestType getType() {
        return type;
    }

    /**
     * Returns the serialized data associated with this request.
     *
     * @return the serialized data string, or null if no data is associated
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the type of this request.
     *
     * @param type the RequestType to set for this request
     */
    public void setType(RequestType type) {
        this.type = type;
    }

    /**
     * Sets the serialized data for this request.
     *
     * @param data the serialized data string to associate with this request
     */
    public void setData(String data) {
        this.data = data;
    }
}
