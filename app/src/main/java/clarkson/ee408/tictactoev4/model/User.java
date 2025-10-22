package clarkson.ee408.tictactoev4.model;

/**
 * Models a user who plays the TicTacToe game. This class represents player accounts
 * used for registration, login, and tracking online status in the gaming system.
 * <p>
 * User objects are used for first-time registration and login processes. The username
 * attribute serves as the unique identifier for every player in the game system.
 * <p>
 * This model class maps directly to the database table 'User', with all class attributes
 * having corresponding table columns. User objects are serialized and exchanged between
 * client and server for authentication and status synchronization.
 */
public class User {

    /**
     * A string representation of the user's username, serving as the unique identifier
     * for the player in the game system. Maps to the 'username' column in the User table.
     */
    private String username;

    /**
     * A string representation of the user's password used for authentication.
     * Maps to the 'password' column in the User table.
     */
    private String password;

    /**
     * A string representation of the user's display name shown to other players.
     * Maps to the 'displayName' column in the User table.
     */
    private String displayName;

    /**
     * A boolean value indicating whether the user is currently online and available
     * for gameplay. Maps to the 'online' column in the User table.
     */
    private boolean online;

    /**
     * Default constructor that creates a User with empty/default values.
     * Used for initialization before populating with actual user data.
     */
    public User() {
        this(null, null, null, false);
    }

    /**
     * Creates a complete User with all attributes. Typically used when creating
     * new user accounts or when retrieving user data from the database.
     *
     * @param username the unique username that identifies the player
     * @param password the authentication password for the user account
     * @param displayName the name displayed to other players in the game
     * @param online indicates whether the user is currently online
     */
    public User(String username, String password, String displayName, boolean online) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.online = online;
    }

    /**
     * Returns the unique username that identifies this player in the system.
     *
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the authentication password for this user account.
     *
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the display name shown to other players in the game.
     *
     * @return the user's display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the online status indicating whether the user is currently available.
     *
     * @return true if the user is online, false otherwise
     */
    public boolean getOnline() {
        return online;
    }

    /**
     * Sets the unique username for this player. The username serves as the
     * primary identifier in the system and must be unique across all users.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the authentication password for this user account.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the display name that will be shown to other players in the game.
     *
     * @param displayName the display name to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Updates the online status of the user. This indicates whether the user
     * is currently connected and available for gameplay.
     *
     * @param online true to set the user as online, false for offline
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Compares this user to the specified object for equality. Two users are
     * considered equal if they have the same username, as this is the unique
     * identifier for players in the system.
     *
     * @param o the object to compare this User against
     * @return true if the given object represents a User with the same username
     */
    @Override
    public boolean equals(Object o) {
        //check for reference equality
        if (this == o) return true;
        //check for null and class type equality
        if (o == null || getClass() != o.getClass()) return false;
        //cast the object and compare the unique attribute
        User user = (User) o;
        return username.equals(user.username);
    }
}
