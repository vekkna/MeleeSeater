// App to randomly seat players of table top games.
// Stores players' names from session to session. Randomises player order.

package com.games.vekkna.meleeseater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    public final static String FILE = "file";

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String savedNames; // string of names saved to phone's memory
    ArrayList players, selectedPlayers; // lists of saved players' names and players currently selected
    Button addButton, assignButton, deleteButton;
    private MediaPlayer gong;

    @Override 
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gong = MediaPlayer.create(this, R.raw.gong);
        gong.start(); // sound effect when app opens

        prefs = getSharedPreferences(FILE, 0); // retrieve saved names, if any
        editor = prefs.edit();
        savedNames = prefs.getString("savedNames", "");

        if (savedNames.length() > 0) {
            players = new ArrayList<String>(Arrays.asList(savedNames.split(","))); // split the saved names string and populate a list
        } else {
            players = new ArrayList(); // if no previously saved names create empty list
        }

        selectedPlayers = new ArrayList(); // list of players selected by user

        addButton = (Button) findViewById(R.id.button); // used to save new players to the phone's memory
        assignButton = (Button) findViewById(R.id.button3); //used to select players to be randomised
        deleteButton = (Button) findViewById(R.id.button4); // used to delete saved players from the phone's memory

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButtonClick();
            }
        });

        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assignButtonClick();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteButtonClick();
            }
        });
    }

    private void addButtonClick() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this); // when the user presses the Add Player button a dialog pops up
        alert.setMessage("Enter the new player's name..."); // dialog's message
        final EditText input = new EditText(this); // where the use enters the player's name
        alert.setView(input);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) { // "OK" button adds input field's text to saved names string and saves that to phone's memory

                String inputName = input.getText().toString();

                if (inputName.length() > 0) {
                    players.add(inputName);
                    StringBuilder sb = new StringBuilder(savedNames);
                    sb.append(inputName + ",");
                    savedNames = sb.toString();
                    editor.putString("savedNames", savedNames);
                    editor.commit();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // "Cancel" button

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    private void assignButtonClick() { // Buttons that opens dialog to select players to randomise

        selectedPlayers.clear();
        final CharSequence[] p = new CharSequence[players.size()];
        final boolean[] pChecked = new boolean[p.length]; // array of booleans to record which saved players are ticked for randomisation
        for (int i = 0; i < players.size(); i++) {
            p[i] = players.get(i).toString();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (players.size() > 0) {
            builder.setTitle("Select the players present...");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) { // When the used pressed "OK"

                    for (int i = 0; i < p.length; i++) {
                        if (pChecked[i]) { // if a player's name has been ticked

                            selectedPlayers.add(p[i]); // add that name to the selected players list
                            pChecked[i] = false; // untick that name
                        }
                    }
                    if (selectedPlayers.size() > 0) { // save the selection in case of repeated use
                        editor.putString("previousSelection", selectedPlayers.toString().trim());
                        editor.commit();
                        showResults();

                    }
                }
            })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

            builder.setNeutralButton("As before", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) { // Button that lets the user randomise the most recently randomised list of players
                    String s = prefs.getString("previousSelection", "").trim(); // retrieve the string of player names from the phone
                    if (s.length() > 0) {
                        s = s.replaceAll("\\[", "").replaceAll("\\]", ""); // replace the commas with spaces
                        selectedPlayers = new ArrayList<String>(Arrays.asList(s.split(", "))); // split the string at each space and add each name to selected players list

                        showResults();
                    } else {
                        builder.setTitle("No previous selection \nSelect the players present...");
                        builder.show();
                    }
                }
            });

            builder.setMultiChoiceItems(p, new boolean[p.length], new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) { // adds the saved players' names to the dialog, with boxes the user can select them with
                    pChecked[which] = isChecked;
                }
            });
        } else {
            builder.setTitle("First add players."); // if the user tries to randomise a non-existant list of players
            builder.setPositiveButton("Sorry", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        builder.show();
    }

    private void showResults() { // method to display list of randomised players

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

        Collections.shuffle(selectedPlayers); // shuffle the players the user has selected

        StringBuilder sb = new StringBuilder(); // create a string builder to add numbers in front of the players' names
        for (int i = 0; i < selectedPlayers.size(); i++) {
            sb.append(String.valueOf(i + 1) + ". " + selectedPlayers.get(i) + "\n");
        }
        builder1.setTitle("Player order is");
        String s = sb.toString();
        s = s.trim();
        builder1.setMessage(s);
        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        gong.stop();
        gong.start();
        builder1.show();
    }

    private void deleteButtonClick() { // button to open dialog that lets user delete players from list of saved names

        final CharSequence[] p = new CharSequence[players.size()]; // create char array of players
        final boolean[] pChecked = new boolean[p.length]; // array to record whether or not a player is selected
        for (int i = 0; i < players.size(); i++) {
            p[i] = players.get(i).toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (players.size() > 0) {
            builder.setTitle("Select the players to delete..."); // dialog shows user list of saved players and tick boxes to select them
            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) { // when the user presses the "Remove" button

                    for (int i = 0; i < p.length; i++) {
                        if (pChecked[i]) {  // if a name is selected

                            selectedPlayers.remove(p[i]); // remove the player from the list of selected players
                            players.remove(p[i]); // remove it from the list of names to save
                            savedNames = players.toString(); // convert the list of names to a single string that will be saved later
                            savedNames = savedNames.replaceAll("\\[", "").replaceAll("\\]", ""); // the new string will include the list's square brackets. This removes them.
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("savedNames", savedNames); // save the new list of players' names
                            editor.commit();
                            pChecked[i] = false; // untick the name
                        }
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.setMultiChoiceItems(p, new boolean[p.length], new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) { // Fills the dialog with the saved players' names a tick boxes to select them
                    pChecked[which] = isChecked;
                }
            });
        } else {
            builder.setTitle("No players saved."); // If the user tries to delete players when no players are saved
            builder.setPositiveButton("I see...", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        builder.show();
    }

    @Override
    public void onClick(View view) {
    }
}