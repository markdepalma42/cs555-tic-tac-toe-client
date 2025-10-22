package clarkson.ee408.tictactoev4.socket;

/**
 * Enumeration defining all possible request types that clients can send to the server.
 * Each request type corresponds to specific game functionality and has defined data requirements.
 */
public enum RequestType {
    /**
     * Sent when a user wants to log into the game. Data contains serialized User object with username and password.
     * Server responds with standard Response indicating success/failure.
     */
    LOGIN,

    /**
     * Sent when a user wants to register for the first time. Data contains serialized User object with username, display name, and password.
     * Server responds with standard Response indicating success/failure.
     */
    REGISTER,

    /**
     * Sent periodically after login to request pairing updates (available players, invitations, responses). Data is null.
     * Server responds with PairingResponse containing all pairing updates.
     */
    UPDATE_PAIRING,

    /**
     * Sent when a player selects an opponent to play a game. Data contains serialized String with opponent's username.
     * Server creates Event with status PENDING and responds with standard Response.
     */
    SEND_INVITATION,

    /**
     * Sent when a player accepts a game invitation. Data contains serialized Integer with eventId of the invitation.
     * Server updates Event status from PENDING to ACCEPTED and responds with standard Response.
     */
    ACCEPT_INVITATION,

    /**
     * Sent when a player declines a game invitation. Data contains serialized Integer with eventId of the invitation.
     * Server updates Event status from PENDING to DECLINED and responds with standard Response.
     */
    DECLINE_INVITATION,

    /**
     * Sent as acknowledgment after receiving a game response to an invitation. Data contains serialized Integer with eventId.
     * Server updates Event status from ACCEPTED/DECLINED to PLAYING/ABORTED and responds with standard Response.
     */
    ACKNOWLEDGE_RESPONSE,

    /**
     * Sent periodically during gameplay to request the opponent's move. Data is null.
     * Server responds with GamingResponse containing opponent's move and game active status.
     */
    REQUEST_MOVE,

    /**
     * Sent during gameplay to submit a player's move. Data contains serialized Integer (0-8) representing the grid cell selected.
     * Server responds with standard Response indicating success/failure.
     */
    SEND_MOVE,

    /**
     * Sent when a user wants to abort an ongoing game. Data is null.
     * Server responds with standard Response indicating success/failure.
     */
    ABORT_GAME,

    /**
     * Sent when a game is over after receiving a final move. Data is null.
     * Server updates Event status from PLAYING to COMPLETED and responds with standard Response.
     */
    COMPLETE_GAME
}
