package clarkson.ee408.tictactoev4.socket;

/**
 * Enumeration defining the possible status values for server responses.
 * These statuses indicate the overall outcome of client request processing.
 */
public enum ResponseStatus {

    /**
     * Indicates the client request was processed successfully without errors.
     * The requested operation completed as expected and the client can proceed normally.
     */
    SUCCESS,

    /**
     * Indicates the client request encountered an error during processing.
     * The operation could not be completed and the client should handle the failure appropriately.
     */
    FAILURE
}
