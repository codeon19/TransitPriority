package samuelbreck.transitpriority;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 * This Activity runs the java for the main_spat.xml file and handles the bluetooth state changes
 */
public class MainActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;

    // Layout Views
    private TextView mTitle;
    private ImageView mImageRed;
    private ImageView mImageYellow;
    private ImageView mImageGreen;
    private TextView mTime;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private AndroidLocomateMessagingServer mChatService = null;

    // Kill Signal
    private boolean requestSignal;

    // Reference to the Request Fragment
    private Request_Fragment request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTime = (TextView) findViewById(R.id.timer);
        mImageRed = (ImageView) findViewById(R.id.red);
        mImageGreen = (ImageView) findViewById(R.id.green);
        mImageYellow = (ImageView) findViewById(R.id.yellow);

        mImageRed.setImageResource(R.drawable.off);
        mImageYellow.setImageResource(R.drawable.off);
        mImageGreen.setImageResource(R.drawable.off);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ensureDiscoverable();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        request = (Request_Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_Request);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Ensure Discoverable", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                ensureDiscoverable();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == AndroidLocomateMessagingServer.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new AndroidLocomateMessagingServer(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null)
            mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case AndroidLocomateMessagingServer.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            break;
                        case AndroidLocomateMessagingServer.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case AndroidLocomateMessagingServer.STATE_LISTEN:
                        case AndroidLocomateMessagingServer.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:

                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    try {
                        String rStr, yStr, gStr;
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        // string format: %0.1f %0.1f %0.1f, red_time yellow_time green_time
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        // set up inputstream from the string
                        InputStream in = new ByteArrayInputStream(readMessage.getBytes());

                        Scanner sc = new Scanner(in);
                        // read the remaining times as strings
                        rStr = sc.next();

                        if(sc.hasNext()) {
                            yStr = sc.next();
                            gStr = sc.next();

                            // if one of values of the string is not 0.0, display the phase.
                            if (!rStr.equals("0.0")) {
                                // set image
                                mImageRed.setImageResource(R.drawable.red);
                                mImageYellow.setImageResource(R.drawable.off);
                                mImageGreen.setImageResource(R.drawable.off);
                                // set text to remaining time
                                mTime.setText(rStr.substring(0, 1));
                            } else if (!yStr.equals("0.0")) {
                                mImageRed.setImageResource(R.drawable.off);
                                mImageYellow.setImageResource(R.drawable.yellow);
                                mImageGreen.setImageResource(R.drawable.off);
                                mTime.setText(yStr.substring(0, 1));
                            } else if (!gStr.equals("0.0")) {
                                mImageRed.setImageResource(R.drawable.off);
                                mImageYellow.setImageResource(R.drawable.off);
                                mImageGreen.setImageResource(R.drawable.green);
                                mTime.setText(gStr.substring(0, 1));
                            }
                        }
                        else {

                            if(rStr == "granted") {
                                request.result(true);
                            }
                            else if (rStr == "declined") {
                                request.result(false);
                            }

                        }

                    }catch (Exception e){

                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // This is the function that is called when the user would like to send out a request to the OBU
    public boolean requestPriority(String message) {

        // Check that we're actually connected before trying anything

        /*
        if (mChatService.getState() != AndroidLocomateMessagingServer.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return false;
        }

         alertRequest();
         */


        // Sends the string request
        String priorityRequest = message;
        byte[] out = priorityRequest.getBytes();
        mChatService.write(out);

        return true;
    }

    // Warning call to see if the user would like to send a request
    public void alertRequest() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Send Priority Request?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}
