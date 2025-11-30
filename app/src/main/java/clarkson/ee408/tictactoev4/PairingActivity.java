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

import clarkson.ee408.tictactoev4.client.AppExecutors;
import clarkson.ee408.tictactoev4.client.SocketClient;
import clarkson.ee408.tictactoev4.model.Event;
import clarkson.ee408.tictactoev4.model.EventStatus;
import clarkson.ee408.tictactoev4.model.User;
import clarkson.ee408.tictactoev4.socket.PairingResponse;
import clarkson.ee408.tictactoev4.socket.Request;
import clarkson.ee408.tictactoev4.socket.RequestType;

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
        // Create Request object with type UPDATE_PAIRING
        Request request = new Request();
        request.setType(RequestType.UPDATE_PAIRING);

        // TODO: Send an UPDATE_PAIRING request to the server. If SUCCESS call handlePairingUpdate(). Else, Toast the error
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                PairingResponse pr = socketClient.getInstance().sendRequest(request, PairingResponse.class);

                if (pr == null) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            Toast.makeText(this, "Pairing update failed.", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                AppExecutors.getInstance().mainThread().execute(() ->
                        handlePairingUpdate(pr)
                );

            } catch (Exception e) {
                Log.e(TAG, "Error updating pairing", e);
            }
        });
    }

    /**
     * Handle the PairingResponse received from the server
     * @param response PairingResponse from the server
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
            if (invitationResponse.getStatus() == EventStatus.ACCEPTED) {
                Toast.makeText(this, invitationResponse.getSender() + " accepted your request!", Toast.LENGTH_SHORT).show();
                beginGame(invitationResponse, 1);

            } else if (invitationResponse.getStatus() == EventStatus.DECLINED) {
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
     * @param availableUsers list of users that are available for pairing
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
     * Sends game invitation to an
     * @param userOpponent the User to send invitation to
     */
    private void sendGameInvitation(User userOpponent) {
        //Create request object with type SEND_INVITATION
        Request request = new Request();
        request.setType(RequestType.SEND_INVITATION);

        //SEND_INVITATION request if successful, Toast success or error
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                InvitationResponse ir = socketClient.getInstance().sendRequest(request, InvitationResponse.class);

                if (ir == null) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                    Toast.makeText(this, "Invitation sent to " + userOpponent.getUsername(), Toast.LENGTH_SHORT).show());
                } else {
                    AppExecutors.getInstance().mainThread().execute(() ->
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending invitation", e);
            }
        });
    }

    /**
     * Sends an ACKNOWLEDGE_RESPONSE request to the server
     * Tell server i have received accept or declined response from my opponent
     */
    private void sendAcknowledgement(Event invitationResponse) {

        if (invitationResponse == null) {
            Toast.makeText(this, "Invalid event.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request();
        request.setType(RequestType.ACKNOWLEDGE_RESPONSE);
        request.setData(String.valueOf(invitationResponse.getEventId()));

        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                Response response = SocketClient.getInstance()
                        .sendRequest(request, Response.class);

                if (response == null) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            Toast.makeText(this, "Acknowledge failed.", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            handleAcknowledgeResponse(invitationResponse)
                    );
                } else {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                Log.e(TAG, "Error acknowledging response", e);
            }
        });
    }

    /**
     * Create a dialog showing incoming invitation
     * @param invitation the Event of an invitation
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
     * Sends an ACCEPT_INVITATION to the server
     * @param invitation the Event invitation to accept
     */
    private void acceptInvitation(Event invitation) {

        if (invitation == null) {
            Toast.makeText(this, "Invalid invitation.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request();
        request.setType(RequestType.ACCEPT_INVITATION);
        request.setData(String.valueOf(invitation.getEventId()));

        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                Response response = SocketClient.getInstance()
                        .sendRequest(request, Response.class);

                if (response == null) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            Toast.makeText(this, "Accept invitation failed.", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            handleAcceptInvitation(invitation)
                    );
                } else {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                Log.e(TAG, "Error accepting invitation", e);
            }
        });
    }


    /**
     * Sends an DECLINE_INVITATION to the server
     * @param invitation the Event invitation to decline
     */
    private void declineInvitation(Event invitation) {

        if (invitation == null) {
            Toast.makeText(this, "Invalid invitation.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request();
        request.setType(RequestType.DECLINE_INVITATION);
        request.setData(String.valueOf(invitation.getEventId()));

        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                Response response = SocketClient.getInstance()
                        .sendRequest(request, Response.class);

                if (response == null) {
                    AppExecutors.getInstance().mainThread().execute(() ->
                            Toast.makeText(this, "Decline invitation failed.", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (response.getStatus() == ResponseStatus.SUCCESS) {
                        Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                        shouldUpdatePairing = true;   // required by TODO
                    } else {
                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error declining invitation", e);
            }
        });
    }



    /**
     *
     * @param pairing the Event of pairing
     * @param player either 1 or 2
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