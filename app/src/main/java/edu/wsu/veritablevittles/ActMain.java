//==========================================================================
//
// Application: Veritable Vittles
// Activity:    Activity Main
// Course:      CSC 4330
// Homework:    4
// Author:      MD Bakhtiar R Akhib
// Date:        04/15/2021
// Description: This is an android app for Veritable Vittles that lets user reserve
//              tables for their party and are able to view the wait time.
//===========================================================================
package edu.wsu.veritablevittles;

//Importing packages
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;

//---------------------------------
// class ActMain
//---------------------------------
public class ActMain extends AppCompatActivity
    implements TextToSpeech.OnInitListener
{
    //-------------------------------------------
    // Constants and variables
    //-------------------------------------------
    private Toolbar tbrMain;
    private Timer timer;

    private EditText txtViewPartyName;
    private EditText txtViewNumPeople;
    private EditText txtViewOccasion;
    private static EditText txtViewWaitTime;
    private static TextView txtViewReservationReady;
    private Spinner spSeatingTypes;
    private Button btnSubmitReservation;
    private Button btnSpeak;
    private String message;
    private TextToSpeech vs;
    private float pitch = 1.0f;
    private float rate = 1.0f;

    ArrayList<String> alSeatingTypes = new ArrayList<String>();

    public static final String STRING_VALUE_txtViewPartyName = "Party Name View Shared Preference";
    public static final String STRING_VALUE_txtViewNumPeople = "Num of People View Shared Preference";
    public static final int INTEGER_VALUE_spSeatingTypes = 0;
    public static final String STRING_VALUE_txtViewOccasion = "Occasion View Shared Preference";

    public final String SEATING_TYPES_FILE = "SeatingTypesData.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laymain);

        //Getting the ids by value and storing them into objects
        txtViewPartyName = findViewById(R.id.txtViewPartyName);
        txtViewNumPeople = findViewById(R.id.txtViewNumPeople);
        txtViewOccasion = findViewById(R.id.txtViewOccasion);
        txtViewWaitTime = findViewById(R.id.txtViewWaitTime);
        spSeatingTypes = findViewById(R.id.spSeatingTypes);
        btnSubmitReservation = findViewById(R.id.btnSubmitReservation);
        btnSpeak = findViewById(R.id.btnSpeak);
        txtViewReservationReady = findViewById(R.id.txtViewReservationReady);

        //Initializing the Text Views according to the requirement
        txtViewWaitTime.setText("0");
        btnSpeak.setEnabled(false);
        txtViewReservationReady.setVisibility(View.INVISIBLE);

        //Adding a custom toolbar along with image
        tbrMain = findViewById(R.id.tbrMain);
        setSupportActionBar(tbrMain);
        tbrMain.setNavigationIcon(R.mipmap.ic_launcher_veritablevittles);

        //Calling the write file to write the seating types into the file
        writeInternalFile();

        //Calling the read file to read from the file and initialize the seating type spinner
        readInternalFile();
    }

    //----------------------------------------------------------------
    // onStartClicked
    //----------------------------------------------------------------
    public void onSubmitReservationClicked(View v)
    {
        //Get the current button text
        String ButtonText = btnSubmitReservation.getText().toString();

        // Checking if the button says submit
        if(ButtonText.equals("Submit")){
            Random r = new Random();
            int low = 20;
            int high = 41;
            // Generate a random number between 20 and 40
            int randomMinute = r.nextInt(high - low) + low;

            // Assign the random number to the wait minute
            txtViewWaitTime.setText(String.valueOf(randomMinute));

            // Cancel timer if  exists
            if (timer != null) timer.cancel();

            // Create and start timer
            timer = new Timer();
            timer.schedule(
                    new Task(),
                    Shared.Data.TIMER_TASK_DELAY * 1000,
                    Shared.Data.TIMER_TASK_THREAD_PAUSE * 1000);

            // Set the button to cancel
            btnSubmitReservation.setText("Cancel");

            // Enable the speak button
            btnSpeak.setEnabled(true);

            // Hide the reservation ready text view
            txtViewReservationReady.setVisibility(View.INVISIBLE);

            // Declare variables
            SharedPreferences preferences;
            SharedPreferences.Editor editor;

            // Save value in shared preferences
            preferences = getPreferences(MODE_PRIVATE);
            editor = preferences.edit();
            editor.putString(STRING_VALUE_txtViewPartyName, txtViewPartyName.getText().toString());
            editor.putString(STRING_VALUE_txtViewNumPeople, txtViewNumPeople.getText().toString());
            editor.putInt(String.valueOf(INTEGER_VALUE_spSeatingTypes), spSeatingTypes.getSelectedItemPosition());
            editor.putString(STRING_VALUE_txtViewOccasion, txtViewOccasion.getText().toString());
            editor.commit();

            // Show message
            Toast.makeText(getApplicationContext(),
                    "The reservation has been submitted", Toast.LENGTH_LONG)
                    .show();
        }
        else{
            // Cancel timer if  exists
            if (timer != null) timer.cancel();
            timer = null;

            // Reset the wait time to 0
            txtViewWaitTime.setText("0");

            // Change the text on the cancel button to submit
            btnSubmitReservation.setText("Submit");

            // Disable the speak button
            btnSpeak.setEnabled(false);

            // Hide the reservation ready text view
            txtViewReservationReady.setVisibility(View.INVISIBLE);
        }
    }

    //----------------------------------------------------------------
    // onRecallClicked
    //----------------------------------------------------------------
    public void onRecallReservationClicked(View v)
    {
        //Setting dialog box to ask if the user wants to recall or no
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Are you sure you want to recall?");

        //IF the user decides to reset this will be executed
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            //onClick function resets every field to previous
            public void onClick(DialogInterface dialog, int which) {
                // Declare variables
                SharedPreferences preferences;

                // Retrieve value from shared preferences
                preferences = getPreferences(MODE_PRIVATE);
                txtViewPartyName.setText(preferences.getString(STRING_VALUE_txtViewPartyName, ""));
                txtViewNumPeople.setText(preferences.getString(STRING_VALUE_txtViewNumPeople, ""));
                spSeatingTypes.setSelection(preferences.getInt(String.valueOf(INTEGER_VALUE_spSeatingTypes), 0));
                txtViewOccasion.setText(preferences.getString(STRING_VALUE_txtViewOccasion, ""));

                // Cancel timer if  exists
                if (timer != null) timer.cancel();
                timer = null;

                // Reset the wait time to 0
                txtViewWaitTime.setText("0");

                // Change the text on the cancel button to submit
                btnSubmitReservation.setText("Submit");

                // Hide the reservation ready text view
                txtViewReservationReady.setVisibility(View.INVISIBLE);

                // Disable the speak button
                btnSpeak.setEnabled(false);

                //Toast message to show that recall was executed
                Toast.makeText(getApplicationContext(),"All the fields have been retrieved!", Toast.LENGTH_SHORT).show();
            }
        });

        //If the user selects no this will be executed
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing just show a toast message that nothing was changed
                Toast.makeText(getApplicationContext(),"Nothing changed!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    //----------------------------------------------------------------
    // onResetClicked
    //----------------------------------------------------------------
    public void onResetClicked(View v){
        //Setting dialog box to ask if the user wants to reset or no
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Are you sure you want to reset all the fields?");
        builder.setMessage("If you do, you would have to start from the beginning again");

        //IF the user decides to reset this will be executed
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            //onClick function resets every field to previous
            public void onClick(DialogInterface dialog, int which) {
                txtViewPartyName.setText("");
                txtViewNumPeople.setText("");
                txtViewOccasion.setText("");
                spSeatingTypes.setSelection(0);

                // Cancel timer if  exists
                if (timer != null) timer.cancel();
                timer = null;

                // Reset the wait time to 0
                txtViewWaitTime.setText("0");

                // Change the text on the cancel button to submit
                btnSubmitReservation.setText("Submit");

                // Hide the reservation ready text view
                txtViewReservationReady.setVisibility(View.INVISIBLE);

                // Disable the speak button
                btnSpeak.setEnabled(false);

                //Toast message to show that reset was executed
                Toast.makeText(getApplicationContext(),"All the fields have been reset!", Toast.LENGTH_SHORT).show();
            }
        });

        //If the user selects no this will be executed
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing just show a toast message that nothing was changed
                Toast.makeText(getApplicationContext(),"Nothing changed!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    //----------------------------------------------------------------
    // onSpeakClicked
    //----------------------------------------------------------------
    public void onSpeak(View v)
    {
        // Checking if the wait time is 1
        if(txtViewWaitTime.getText().toString() == "1"){
            message = "Your wait time is 1 minute.";
        }
        else{
            message = "Your wait time is " + txtViewWaitTime.getText().toString() + " minutes.";
        }
        vs = new TextToSpeech(this, this);

    }

    //----------------------------------------------------------------
    // onInit
    // Implemented from interface TextToSpeech.OnInitListener
    //----------------------------------------------------------------
    @Override
    public void onInit(int status){
        if (vs != null)
        {
            vs.setPitch(pitch);
            vs.setSpeechRate(rate);
            vs.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null);
            vs = null;
        }
    }

    //----------------------------------------------------------------
    // writeInternalFile
    // ----------------------------------------------------------------
    public void writeInternalFile()
    {
        // Declare variables
        FileOutputStream fileOut = null;
        PrintStream streamOut = null;
        Random rand = new Random();

        // Attempt to write internal file
        try
        {
            // Open output file
            fileOut =
                    openFileOutput(SEATING_TYPES_FILE, MODE_PRIVATE);
            streamOut = new PrintStream(fileOut);

            // Write lines to output file
            streamOut.println("Interior booth");
            streamOut.println("Interior table");
            streamOut.println("Patio");
            streamOut.println("Private Room");
            streamOut.println("Rooftop");
            streamOut.println("Sidewalk");
            streamOut.println("Window table");

            // Close output files
            fileOut.close();
            streamOut.close();

            // Show success message
            Toast.makeText(getApplicationContext(),
                    "7 line(s) written to input file '" +
                            SEATING_TYPES_FILE + "'.",
                    Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            Toast.makeText(getApplicationContext(),
                    "Error creating or writing to input file '" +
                            SEATING_TYPES_FILE + "'.",
                    Toast.LENGTH_LONG).show();
        }
    }

    //----------------------------------------------------------------
    // readInternalFile
    //----------------------------------------------------------------
    public void readInternalFile()
    {

        // Declare variables
        FileInputStream fileIn = null;
        Scanner streamIn = null;
        int lineCount = 0;
        String line;

        // Attempt to read internal file
        try
        {
            // Open input file
            fileIn = openFileInput(SEATING_TYPES_FILE);
            streamIn = new Scanner(fileIn);

            // Loop to read lines from input file
            lineCount = 0;
            while (streamIn.hasNextLine())
            {
                line = streamIn.nextLine();
                alSeatingTypes.add(line);
                lineCount = lineCount + 1;
                System.out.println(line);
            }

            // Define spinner adapter
            ArrayAdapter<String> spAdapterSeatingTypes =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_spinner_item,
                            alSeatingTypes);
            spAdapterSeatingTypes.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            spSeatingTypes.setAdapter(spAdapterSeatingTypes);

            // Define spinner listener
            spSeatingTypes.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> adapterView, View view, int i, long l)
                    {
                        System.out.println(String.valueOf(spSeatingTypes.getSelectedItem()));
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView)
                    {

                    }
                });

            // Close input file
            fileIn.close();

            // Show success message
            Toast.makeText(getApplicationContext(),
                    "Success reading",
                    Toast.LENGTH_LONG).show();

        }
        catch (IOException e)
        {
            // Show success message
            Toast.makeText(getApplicationContext(),
                    "Error reading file",
                    Toast.LENGTH_LONG).show();
        }

    }

    //----------------------------------------------------------------
    // timerTaskHandler
    //----------------------------------------------------------------
    public static Handler timerTaskHandler =
            new Handler(Looper.getMainLooper())
            {

                //------------------------------------------------------------
                // handleMessage
                //------------------------------------------------------------
                @Override
                public void handleMessage(Message msg)
                {
                    Random r = new Random();
                    int low = 1;
                    int high = 4; //Excluding 4 so between 1 - 3
                    int random = r.nextInt(high - low) + low;
                    int txtViewWaitTimeValue = Integer.parseInt(txtViewWaitTime.getText().toString());
                    int decreaseWaitTimeValue =  0;
                    if (txtViewWaitTimeValue > random){
                        decreaseWaitTimeValue =  txtViewWaitTimeValue - random;
                    }
                    txtViewWaitTime.setText(String.valueOf(decreaseWaitTimeValue));
                    if(decreaseWaitTimeValue == 0){
                        txtViewReservationReady.setVisibility(View.VISIBLE);
                    }
                }

            };
}