package clarkson.ee408.tictactoev4;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import clarkson.ee408.tictactoev4.client.AppExecutors;
import clarkson.ee408.tictactoev4.client.SocketClient;
import clarkson.ee408.tictactoev4.model.User;
import clarkson.ee408.tictactoev4.socket.Request;
import clarkson.ee408.tictactoev4.socket.RequestType;
import clarkson.ee408.tictactoev4.socket.Response;
import clarkson.ee408.tictactoev4.socket.ResponseStatus;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private EditText displayNameField;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Getting Inputs
        Button registerButton = findViewById(R.id.buttonRegister);
        Button loginButton = findViewById(R.id.buttonLogin);
        usernameField = findViewById(R.id.editTextUsername);
        passwordField = findViewById(R.id.editTextPassword);
        confirmPasswordField = findViewById(R.id.editTextConfirmPassword);
        displayNameField = findViewById(R.id.editTextDisplayName);

        // Initialize Gson with null serialization option
        gson = new GsonBuilder().serializeNulls().create();

        //Adding Handlers
        registerButton.setOnClickListener(v -> handleRegister());
        loginButton.setOnClickListener(v -> goBackLogin());
    }

    /**
     * Process registration input and pass it to {@link #submitRegistration(User)}
     */
    public void handleRegister() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String displayName = displayNameField.getText().toString();

        // verify that all fields are not empty before proceeding
        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank() || displayName.isBlank()) {
            Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // verify that password is the same as confirm password
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, password, displayName, false);
        submitRegistration(user);
    }

    /**
     * Sends REGISTER request to the server
     * @param user the User to register
     */
    void submitRegistration(User user) {
        // Send a REGISTER request to the server in a background thread
        AppExecutors.getInstance().networkIO().execute(()  -> {
            try {
                // serialize the user object to JSON
                String userJson = gson.toJson(user);

                // create a REGISTER request with the user data
                Request request = new Request(RequestType.REGISTER, userJson);

                // send the request to the server
                Response response = SocketClient.getInstance().sendRequest(request, Response.class);

                if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                    // if success response, call goBackLogin()
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration was successful", Toast.LENGTH_SHORT).show();
                        goBackLogin();
                    });
                } else {
                    // toast the error message
                    String errorMessage = (response != null && response.getMessage() != null)
                            ? response.getMessage()
                            : "Registration failed";
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() -> {
                    Toast.makeText(RegisterActivity.this, "Error registering user", Toast.LENGTH_SHORT).show();
                });
                Log.e("MainActivity", "Error registering user", e);
            }
        });
    }

    /**
     * Change the activity to LoginActivity
     */
    private void goBackLogin() {
        //close activity, it will automatically go back to its parent (i.e,. LoginActivity)
        finish();
    }

}