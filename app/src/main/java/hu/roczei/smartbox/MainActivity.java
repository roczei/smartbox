package hu.roczei.smartbox;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.widget.Button;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.app.PendingIntent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.EditText;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Geocoder;
import android.location.Address;
import android.widget.TextView;
import java.util.Locale;
import java.util.List;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MainActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener, LocationListener {
    private TextToSpeech tts;
    static EditText mText;
    private SpeechRecognizer sr;
    private static final String TAG = "MyStt3Activity";
    private LocationManager lm;
    private String phoneNumber;
    private TextView mobile_location;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (tts != null) {
                String message = intent.getStringExtra("message");
                mText.setText(message);
                phoneNumber = intent.getStringExtra("sender");
                ((EditText) findViewById(R.id.edit2)).setText(phoneNumber);

                if (message != null) {
                    if (!tts.isSpeaking()) {
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Button send = (Button) findViewById(R.id.send);

        tts = new TextToSpeech(this, this);
        findViewById(R.id.say).setOnClickListener(this);


        Button speakButton = (Button) findViewById(R.id.record);
        speakButton.setOnClickListener(this);

        mText = (EditText) findViewById(R.id.edit);

        Button nextButton = (Button) findViewById(R.id.next);
        nextButton.setOnClickListener(this);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());


        registerReceiver(broadcastReceiver, new IntentFilter("hu.roczei.smartbox.app.NEW_SMS"));

        // Listening to Login Screen link
        send.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Switching to Register screen

                phoneNumber = ((EditText) findViewById(R.id.edit2)).getText().toString();
                String messageText = ((EditText) findViewById(R.id.edit)).getText().toString();

                String SENT_SMS_ACTION = "SENT_SMS_ACTION";
                String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

                Intent sentintent = new Intent(SENT_SMS_ACTION);
                PendingIntent sentPendingintent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, sentintent, 0);

                Intent deliveryintent = new Intent(DELIVERED_SMS_ACTION);
                PendingIntent deliveredPendingintent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0,
                                deliveryintent, 0);

                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        switch (getResultCode()) {

                            case Activity.RESULT_OK:
                                Toast.makeText(getBaseContext(), getString(R.string.sms_sent),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

                                break;
                        }
                    }

                }, new IntentFilter(SENT_SMS_ACTION));

                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Toast.makeText(getBaseContext(), getString(R.string.sms_delivered),
                                Toast.LENGTH_SHORT).show();
                    }
                }, new IntentFilter(DELIVERED_SMS_ACTION));

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, messageText, sentPendingintent, deliveredPendingintent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getBaseContext(), getString(R.string.sms_not_sent),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mobile_location = (TextView) findViewById(R.id.location);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

    }


    @Override
    public void onLocationChanged(Location location) {


        if (location != null) {
            Geocoder gc = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            int maxNumOfResults = 5;
            try {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), maxNumOfResults);
            } catch (IOException e) {
                // e.printStackTrace();
            }

            String current_address = "";
            try {
                current_address = addresses.get(0).getCountryName().toString() + ", " + addresses.get(0).getAddressLine(2).toString() +
                        " " + addresses.get(0).getAddressLine(0).toString() + " " + addresses.get(0).getAddressLine(1).toString();
            } catch (Exception e) {
                // Log.d("Error", e.getMessage());
            }

            if (current_address.length() != 0) {
                mobile_location.setText(current_address);
            }

        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO le kellene kezelni
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.d(TAG, "error " + error);
            mText.setText("error " + error);
        }

        public void onResults(Bundle results) {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                Log.d(TAG, "result " + data.get(i));
                str = data.get(i).toString();
            }
            mText.setText(str);
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
        } else {
            tts = null;
            Toast.makeText(this, "Failed to initialize TTS engine.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (tts != null && v.getId() == R.id.say) {
            String text =
                    ((EditText) findViewById(R.id.edit)).getText().toString();
            if (text != null) {
                if (!tts.isSpeaking()) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            return;
        }

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = cm.getAllNetworkInfo();

        boolean notConnected = true;

        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                notConnected = false;
            }
        }

        if (notConnected) {
            Toast.makeText(getBaseContext(), getString(R.string.network_problem),
                    Toast.LENGTH_SHORT).show();
            return;
        }


        if (v.getId() == R.id.next) {
            Intent intent = new Intent(this, hu.roczei.smartbox.MapView.class);
            startActivity(intent);
            return;
        }

        if (v.getId() == R.id.record) {

            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                sr.startListening(intent);
            } catch (Exception e) {
                   // TODO
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}