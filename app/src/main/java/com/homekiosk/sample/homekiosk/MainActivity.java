package com.homekiosk.sample.homekiosk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    String PROJECT_NUMBER = "344022689294";
    GoogleCloudMessaging gcm;
    String regid="";
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerPushNotification();
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);


    }



    private void speakOut(final String text) {

            tts= new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                  int result=  tts.setLanguage(Locale.getDefault());
//                    tts.setPitch(1.0f);int result = tts.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getApplicationContext(),"Voice not supported",Toast.LENGTH_LONG).show();
                    }
                    else {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }

            }
        });


    }

    public void getRegId()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    SharedPreferences preferences = getSharedPreferences("Registration Id",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Reg Id", regid);
                    editor.apply();

                    Log.e("GCM id", regid);

                }
                catch (IOException ex)
                {
                    Log.e("GCM id", "error "+ex);
//                    Constants.dialog=MyAlert.show(LoginActivity.this,"Sorry!","This mobile does not support this Application!",MyAlert.ERROR_TYPE);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                Log.e("GCM id", ""+regid);

            }
        }.execute(null, null, null);
    }

    public void registerPushNotification()
    {
        SharedPreferences preferences = getSharedPreferences("Registration Id", MODE_PRIVATE);
        if (preferences.getString("Reg Id","").equals("")) {

            getRegId();
        }
        else {
            Log.e("gcm",""+preferences.getString("Reg Id",""));
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
//                tvStatus.setText(intent.getStringExtra("SCAN_RESULT_FORMAT"));
//                Toast.makeText(getApplicationContext(), "" + intent.getStringExtra("SCAN_RESULT"), Toast.LENGTH_SHORT).show();
                DbHelper dbHelper=new DbHelper(MainActivity.this);
                boolean isavailable=false;
                List<String> keysavailable=new ArrayList<String>();
                List<String> expiries=new ArrayList<String>();
                keysavailable=dbHelper.getAllContacts();
                expiries=dbHelper.getAllexpiries();
                long expiry_date=0;
                for (int i=0;i<keysavailable.size();i++)
                {
                    Log.e("Result",keysavailable.get(i)+"__"+intent.getStringExtra("SCAN_RESULT"));
                    if (keysavailable.get(i).equalsIgnoreCase(""+intent.getStringExtra("SCAN_RESULT")))
                    {
                        isavailable=true;
                        expiry_date=Long.parseLong(expiries.get(i));
                    }
                }
                if (isavailable==true)
                {
                    if (expiry_date<=System.currentTimeMillis()) {
                        Toast.makeText(getApplicationContext(), "Authorized", Toast.LENGTH_SHORT).show();
                        speakOut("Authorized, And its my pleasure to welcome you on behalf of my owner");
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Your key has expired, Please contact the owner!"+expiry_date+":"+System.currentTimeMillis(), Toast.LENGTH_LONG).show();
                        Log.e(""+expiry_date,""+System.currentTimeMillis());
                        speakOut("Sorry, Your key has expired, Please contact the owner!");
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Authorization Denied",Toast.LENGTH_SHORT).show();
                    speakOut("Sorry, Authorization Denied, Please contact the owner!");
                }
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
        Intent intent1 = new Intent("com.google.zxing.client.android.SCAN");
        intent1.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent1, 0);

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
