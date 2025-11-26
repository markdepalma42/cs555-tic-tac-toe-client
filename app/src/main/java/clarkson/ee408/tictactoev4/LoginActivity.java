package clarkson.ee408.tictactoev4;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.Intent;
import android.widget.Toast;

import clarkson.ee408.tictactoev4.model.User;
import clarkson.ee408.tictactoev4.client.SocketClient;
import clarkson.ee408.tictactoev4.executor.AppExecutors;
import clarkson.ee408.tictactoev4.network.Request;
import clarkson.ee408.tictactoev4.network.RequestType;
import clarkson.ee408.tictactoev4.network.Response;
import clarkson.ee408.tictactoev4.network.ResponseStatus;

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
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        submitLogin(user);
    }

    /**
     * Sends a LOGIN request to the server
     * @param user User object to login
     */
    public void submitLogin(User user) {
        // Send a LOGIN request, If SUCCESS response, call gotoPairing(), else, Toast the error message from sever
        String serializedUser = gson.toJson(user);
        Request request = new Request(RequestType.LOGIN, serializedUser);

        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                Response response = SocketClient.getInstance().sendRequest(request, Response.class);

                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (response != null) {
                        if (response.getStatus() == ResponseStatus.SUCCESS) {
                            gotoPairing(user.getUsername());
                        } else {
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(this, "Error sending request", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Switch the page to {@link PairingActivity}
     * @param username the data to send
     */
    public void gotoPairing(String username) {
        Intent intent = new Intent(this, PairingActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    /**
     * Switch the page to {@link RegisterActivity}
     */
    public void gotoRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
