package clarkson.ee408.tictactoev4;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import clarkson.ee408.tictactoev4.model.Event;
import clarkson.ee408.tictactoev4.model.User;
import clarkson.ee408.tictactoev4.socket.PairingResponse;
import clarkson.ee408.tictactoev4.socket.SocketClient;

public class PairingActivity extends AppCompatActivity {

    private final String TAG = "PAIRING";

    private Gson gson;

    private TextView noAvailableUsersText;
    private RecyclerView recyclerView;
    private AvailableUsersAdapter adapter;

    private Handler handler;
    private Runnable refresh;

    private boolean shouldUpdatePairing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        Log.e(TAG, "App is now created");

        //Setup Gson with null serialization option
        gson = new GsonBuilder().serializeNulls().create();

        //Setting the username text
        TextView usernameText = findViewById(R.id.text_username);
        //Set the usernameText to the username passed from LoginActivity (i.e from Intent)
        String username = getIntent().getStringExtra("username");
        usernameText.setText(username);

        //Getting UI Elements
        noAvailableUsersText = findViewById(R.id.text_no_available_users);
        recyclerView = findViewById(R.id.recycler_view_available_users);

        //Setting up recycler view adapter
        adapter = new AvailableUsersAdapter(this, this::sendGameInvitation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateAvailableUsers(null);

        handler = new Handler();
        refresh = () -> {
            //Call getPairingUpdate if shouldUpdatePairing is true
            if (shouldUpdatePairing) {
                getPairingUpdate();
            }
            handler.postDelayed(refresh, 1000);
        };
        handler.post(refresh);
    }

    /**
     * Send UPDATE_PAIRING request to the server
     */
    private void getPairingUpdate() {
        //Send an UPDATE_PAIRING request to the server. If SUCCESS call handlePairingUpdate(). Else, Toast the error
        SocketClient.getInstance().updatePairing(response -> {
            if (response.getStatus().equals("SUCCESS")) {
                PairingResponse pairing = gson.fromJson(response.getMessage(), PairingResponse.class);
                runOnUiThread(() -> handlePairingUpdate(pairing));
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Handle the PairingResponse received from the server
     */
    private void handlePairingUpdate(PairingResponse response) {

        //Handle availableUsers by calling updateAvailableUsers()
        updateAvailableUsers(response.getAvailableUsers());

        //Handle invitationResponse. First by sending acknowledgement
        Event invitationResponse = response.getInvitationResponse();
        if (invitationResponse != null) {

            // acknowledge it
            sendAcknowledgement(invitationResponse);

            //If ACCEPTED → Toast + beginGame()
            if (invitationResponse.getStatus().equals("ACCEPTED")) {
                Toast.makeText(this, invitationResponse.getSender() + " accepted your request!", Toast.LENGTH_SHORT).show();
                beginGame(invitationResponse, 1);

            } else if (invitationResponse.getStatus().equals("DECLINED")) {
                //If DECLINED → Toast message
                Toast.makeText(this, invitationResponse.getSender() + " declined your request.", Toast.LENGTH_SHORT).show();
            }
        }

        //Handle invitation by calling createRespondAlertDialog()
        if (response.getInvitation() != null) {
            createRespondAlertDialog(response.getInvitation());
        }
    }

    /**
     * Updates the list of available users
     */
    public void updateAvailableUsers(List<User> availableUsers) {
        adapter.setUsers(availableUsers);

        if (adapter.getItemCount() <= 0) {
            //Show noAvailableUsersText and hide recyclerView
            noAvailableUsersText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            //Hide noAvailableUsersText and show recyclerView
            noAvailableUsersText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sends game invitation
     */
    private void sendGameInvitation(User userOpponent) {
        //SEND_INVITATION request → Toast success or error
        SocketClient.getInstance().sendInvitation(userOpponent, response -> {
            runOnUiThread(() -> {
                if (response.getStatus().equals("SUCCESS")) {
                    Toast.makeText(this, "Invitation sent to " + userOpponent.getUsername(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * ACKNOWLEDGE_RESPONSE
     */
    private void sendAcknowledgement(Event invitationResponse) {
        //Send ACKNOWLEDGE_RESPONSE request to server
        SocketClient.getInstance().sendAcknowledge(invitationResponse, r -> {
        });
    }

    /**
     * Create incoming invitation dialog
     */
    private void createRespondAlertDialog(Event invitation) {

        //Set shouldUpdatePairing to false
        shouldUpdatePairing = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Game Invitation");
        builder.setMessage(invitation.getSender() + " has Requested to Play with You");
        builder.setPositiveButton("Accept", (dialogInterface, i) -> acceptInvitation(invitation));
        builder.setNegativeButton("Decline", (dialogInterface, i) -> declineInvitation(invitation));
        builder.show();
    }

    /**
     * Accept invitation
     */
    private void acceptInvitation(Event invitation) {
        //Send ACCEPT_INVITATION. If SUCCESS beginGame(player=2)
        SocketClient.getInstance().acceptInvitation(invitation, response -> {
            runOnUiThread(() -> {
                if (response.getStatus().equals("SUCCESS")) {
                    beginGame(invitation, 2);
                } else {
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Decline invitation
     */
    private void declineInvitation(Event invitation) {
        //DECLINE_INVITATION → Toast or error
        SocketClient.getInstance().declineInvitation(invitation, response -> {
            runOnUiThread(() -> {
                if (response.getStatus().equals("SUCCESS")) {
                    Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        //Set shouldUpdatePairing to true after sending decline
        shouldUpdatePairing = true;
    }

    /**
     * Begin the game in MainActivity
     */
    private void beginGame(Event pairing, int player) {

        //Set shouldUpdatePairing to false
        shouldUpdatePairing = false;

        //Start MainActivity and pass player
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("player", player);
        intent.putExtra("pairing", gson.toJson(pairing));
        startActivity(intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Set shouldUpdatePairing to true
        shouldUpdatePairing = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        shouldUpdatePairing = false;

        //Logout by calling close() of SocketClient
        SocketClient.getInstance().close();
    }
}