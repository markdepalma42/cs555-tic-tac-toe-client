package clarkson.ee408.tictactoev4.model;

/**
 * Enumeration defining the possible states of a game event throughout its lifecycle.
 * Each status corresponds to specific client request types that trigger state transitions.
 */
public enum EventStatus {
    /**
     * Initial status set when a client sends a SEND_INVITATION request.
     * Represents an invitation that has been sent and is awaiting response.
     */
    PENDING,

    /**
     * Status set when a client sends a DECLINE_INVITATION request.
     * Represents an invitation that has been declined by the opponent.
     */
    DECLINED,

    /**
     * Status set when a client sends an ACCEPT_INVITATION request.
     * Represents an invitation that has been accepted by the opponent.
     */
    ACCEPTED,

    /**
     * Status set when a client sends an ACKNOWLEDGE_RESPONSE request.
     * Represents a game that is currently in progress between the players.
     */
    PLAYING,

    /**
     * Status set when a client sends a request indicating game completion.
     * Represents a game that has been completed normally with a winner.
     */
    COMPLETED,

    /**
     * Status set when a game is terminated before normal completion.
     * Represents a game that was aborted prematurely.
     */
    ABORTED
}
