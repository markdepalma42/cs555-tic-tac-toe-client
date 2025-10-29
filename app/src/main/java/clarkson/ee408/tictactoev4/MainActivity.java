package clarkson.ee408.tictactoev4;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends AppCompatActivity {

    private static final int STARTING_PLAYER_NUMBER = 1;

    private TicTacToe tttGame;
    private Button[][] buttons;
    private TextView status;
    private Gson gson;
    private boolean shouldRequestMove = false;
    private int myPlayerNumber;

    // ADD SocketClient instance
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tttGame = new TicTacToe(STARTING_PLAYER_NUMBER);
        this.gson = new GsonBuilder().serializeNulls().create();

        // Initialize SocketClient
        socketClient = SocketClient.getInstance();

        // TODO: Initialize myPlayerNumber based on server assignment
        // This should come from your socket connection/login
        myPlayerNumber = 1; /

        buildGuiByCode();
        updateTurnStatus();
    }

    private void requestMove() {
        if (!shouldRequestMove) {
            return; // Only request moves when it's our turn
        }

        // Create Request object with type REQUEST_MOVE
        Request request = new Request();
        request.setType(RequestType.REQUEST_MOVE);
        // You might want to include game state or player info
        request.setData(""); // Add any necessary data

        // Use SocketClient to send request in networkIO thread
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                Response response = socketClient.sendRequest(request);

                // Process response in main thread
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                        // Parse the move from response
                        Move move = gson.fromJson(response.getData(), Move.class);
                        if (move != null && isValidMove(move)) {
                            // Utilize update() function to add changes to the board
                            update(move.getRow(), move.getCol());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("MainActivity", "Error requesting move", e);
            }
        });
    }

    private boolean isValidMove(Move move) {
        return move != null &&
                move.getRow() >= 0 && move.getRow() < TicTacToe.SIDE &&
                move.getCol() >= 0 && move.getCol() < TicTacToe.SIDE &&
                tttGame.getBoard()[move.getRow()][move.getCol()] == 0;
    }

    private boolean isMyTurn() {
        return tttGame.getCurrentPlayer() == myPlayerNumber;;
    }

    private void updateTurnStatus() {
        runOnUiThread(() -> {
            if (isMyTurn()) {
                status.setText("Your Turn");
                shouldRequestMove = true;
                enableButtons(true);
                requestMove();
            } else {
                status.setText("Waiting for Opponent");
                shouldRequestMove = false;
                enableButtons(false);
            }
        });
    }

    public void buildGuiByCode() {
        // Get width of the screen
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x / TicTacToe.SIDE;

        // Create the layout manager as a GridLayout
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(TicTacToe.SIDE);
        gridLayout.setRowCount(TicTacToe.SIDE + 2);

        // Create the buttons and add them to gridLayout
        buttons = new Button[TicTacToe.SIDE][TicTacToe.SIDE];
        ButtonHandler bh = new ButtonHandler();

        gridLayout.setUseDefaultMargins(true);

        for (int row = 0; row < TicTacToe.SIDE; row++) {
            for (int col = 0; col < TicTacToe.SIDE; col++) {
                buttons[row][col] = new Button(this);
                buttons[row][col].setTextSize((int) (w * .2));
                buttons[row][col].setOnClickListener(bh);
                GridLayout.LayoutParams bParams = new GridLayout.LayoutParams();

                bParams.topMargin = 0;
                bParams.bottomMargin = 10;
                bParams.leftMargin = 0;
                bParams.rightMargin = 10;
                bParams.width = w - 10;
                bParams.height = w - 10;
                buttons[row][col].setLayoutParams(bParams);
                gridLayout.addView(buttons[row][col]);
            }
        }

        // set up layout parameters of 4th row of gridLayout
        status = new TextView(this);
        GridLayout.Spec rowSpec = GridLayout.spec(TicTacToe.SIDE, 2);
        GridLayout.Spec columnSpec = GridLayout.spec(0, TicTacToe.SIDE);
        GridLayout.LayoutParams lpStatus
                = new GridLayout.LayoutParams(rowSpec, columnSpec);
        status.setLayoutParams(lpStatus);

        // set up status' characteristics
        status.setWidth(TicTacToe.SIDE * w);
        status.setHeight(w);
        status.setGravity(Gravity.CENTER);
        status.setBackgroundColor(Color.GREEN);
        status.setTextSize((int) (w * .15));
        status.setText(tttGame.result());

        gridLayout.addView(status);

        // Set gridLayout as the View of this Activity
        setContentView(gridLayout);
    }

    public void update(int row, int col) {
        int play = tttGame.play(row, col);
        if (play == 1)
            buttons[row][col].setText("X");
        else if (play == 2)
            buttons[row][col].setText("O");
        if (tttGame.isGameOver()) {
            status.setBackgroundColor(Color.RED);
            enableButtons(false);
            status.setText(tttGame.result());
            showNewGameDialog();    // offer to play again
        }
        updateTurnStatus();
    }

    public void enableButtons(boolean enabled) {
        for (int row = 0; row < TicTacToe.SIDE; row++)
            for (int col = 0; col < TicTacToe.SIDE; col++)
                buttons[row][col].setEnabled(enabled);
    }

    public void resetButtons() {
        for (int row = 0; row < TicTacToe.SIDE; row++)
            for (int col = 0; col < TicTacToe.SIDE; col++)
                buttons[row][col].setText("");
    }

    public void showNewGameDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("This is fun");
        alert.setMessage("Play again?");
        PlayDialog playAgain = new PlayDialog();
        alert.setPositiveButton("YES", playAgain);
        alert.setNegativeButton("NO", playAgain);
        alert.show();
    }

    private class ButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            Log.d("button clicked", "button clicked");

            for (int row = 0; row < TicTacToe.SIDE; row++)
                for (int column = 0; column < TicTacToe.SIDE; column++)
                    if (v == buttons[row][column])
                        update(row, column);
        }
    }

    private class PlayDialog implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int id) {
            if (id == -1) /* YES button */ {
                tttGame.resetGame();
                enableButtons(true);
                resetButtons();
                status.setBackgroundColor(Color.GREEN);
                status.setText(tttGame.result());
                updateTurnStatus();
            } else if (id == -2) // NO button
                MainActivity.this.finish();
        }
    }
}