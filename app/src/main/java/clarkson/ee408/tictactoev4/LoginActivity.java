package clarkson.ee408.tictactoev4;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import clarkson.ee408.tictactoev4.model.User;

import android.content.Intent;
import android.widget.Toast;
import com.google.gson.GsonBuilder;
import clarkson.ee408.tictactoev4.socket.PairingResponse;
import clarkson.ee408.tictactoev4.socket.SocketClient;
import clarkson.ee408.tictactoev4.PairingActivity;
import clarkson.ee408.tictactoev4.RegisterActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Getting UI elements
        Button loginButton = findViewById(R.id.buttonLogin);
        Button registerButton = findViewById(R.id.buttonRegister);
        usernameField = findViewById(R.id.editTextUsername);
        passwordField = findViewById(R.id.editTextPassword);

        // Initialize Gson with null serialization option
        gson = new GsonBuilder().serializeNulls().create();

        //Adding Handlers
        loginButton.setOnClickListener(view -> handleLogin());
        registerButton.setOnClickListener(view -> gotoRegister());
    }

    /**
     * Process login input and pass it to {@link #submitLogin(User)}
     */
    public void handleLogin() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        // Verify that all fields are not empty before proceeding. Toast with the error message
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create User object with username and password and call submitLogin()
        User user = new User(username, password);
        submitLogin(user);
    }
    /**
     * Sends a LOGIN request to the server
     * @param user User object to login
     */
    public void submitLogin(User user) {
        // Send a LOGIN request, If SUCCESS response, call gotoPairing(), else, Toast the error message from sever
        String jsonRequest = gson.toJson(user);

        // Send LOGIN request through the socket client (provided in project)
        SocketClient.getInstance().sendRequest("LOGIN", jsonRequest, responseJson -> {
            // Parse server response
            PairingResponse response = gson.fromJson(responseJson, PairingResponse.class);

            runOnUiThread(() -> {
                if (response.getStatus().equals("SUCCESS")) {
                    gotoPairing(user.getUsername());
                } else {
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Switch the page to {@link PairingActivity}
     * @param username the data to send
     */
    public void gotoPairing(String username) {
        // Start PairingActivity and pass the username
        Intent intent = new Intent(this, PairingActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    /**
     * Switch the page to {@link RegisterActivity}
     */
    public void gotoRegister() {
        //Start RegisterActivity
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}