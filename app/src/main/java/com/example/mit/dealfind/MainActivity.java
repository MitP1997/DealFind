package com.example.mit.dealfind;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase mydatabase;
    Cursor rs;
    Random rand;
    static Button button;
    Button resend;
    static EditText txt;
    String number=null;
    boolean existing=false;
    double mylat,mylong;
    TextView disp;
    JSONParser jParser= new JSONParser();
    String url_all_users="http://couponfind.in/get_users.php";
    String url_add_user="http://couponfind.in/add_user.php";
    JSONArray users = null;
    int otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mylat=getIntent().getExtras().getDouble("mylat");
        mylong=getIntent().getExtras().getDouble("mylong");
        mydatabase = openOrCreateDatabase("df",MODE_PRIVATE,null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS userdetails(number VARCHAR);");
        Boolean isFirstRun=getSharedPreferences("PREFERENCE6",MODE_PRIVATE).getBoolean("isfirstrun",true);
          if (!isFirstRun) {
            rs=mydatabase.rawQuery("select * from userdetails",null);
              rs.moveToLast();
              number=rs.getString(0);
              //finish();
                  Intent i = new Intent(MainActivity.this, deals.class);
                  i.putExtra("mylat",mylat);
                  i.putExtra("mylong",mylong);
                  i.putExtra("contactnumber", number);
                  startActivity(i);

      }

        button=(Button)findViewById(R.id.button);
        disp=(TextView)findViewById(R.id.text);
        resend=(Button)findViewById(R.id.resend);
        button.setOnClickListener(new View.OnClickListener() {
            int count=0;
            @Override
            public void onClick(View v) {
                count++;

                txt=(EditText)findViewById(R.id.editTextId);
                if(count==1){
                    if(txt.getText().toString().equals(null) || txt.getText().toString().equals(""))
                    {
                        txt.setError("Please enter number");
                        count--;
                    }
                    else {
                        number=txt.getText().toString();
                        disp.setText("Please enter the OTP that you received");
                        txt.setText("");
                        button.setText("Login");
                        otp=calcotp();
                        Toast.makeText(getApplicationContext(),""+otp,Toast.LENGTH_LONG).show();
                        new sendOtp(number,otp).execute();
                        resend.setEnabled(false);
                        resend.setVisibility(View.VISIBLE);
                        firstsleep();
                    }

                }
                else{
                    if(txt.getText().toString().equals(""+otp))
                    {
                        mydatabase.execSQL("INSERT INTO userdetails(number) VALUES('"+number+"');");
                        new allUsers().execute();
                        Log.d("existing vl be checked","");
                        if(existing)
                        {
                            Log.d("existing checked in if","");
                            Toast.makeText(getApplicationContext(),"User Found",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.d("existingg is false","");
                            new addUser().execute();
                        }

                        ProgressDialog pDialog= new ProgressDialog(MainActivity.this);
                        pDialog.setMessage("Logging you in. Please wait...");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        new CountDownTimer(2000, 1000) {
                            public void onFinish() {
                                getSharedPreferences("PREFERENCE6",MODE_PRIVATE).edit().putBoolean("isfirstrun",false).commit();
                                //finish();
                                Intent i=new Intent(MainActivity.this, deals.class);
                                i.putExtra("contactnumber",number);
                                i.putExtra("mylat",mylat);
                                i.putExtra("mylong",mylong);
                                startActivity(i);
                            }

                            public void onTick(long millisUntilFinished) {
                                // millisUntilFinished    The amount of time until finished.
                            }
                        }.start();
                    }
                    else
                        txt.setError("Please recheck your OTP");
                }

            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            int count=0;
            @Override
            public void onClick(View v) {
                count++;
                if(count==1)
                {
                    resend.setEnabled(false);
                    resend.setText("Wait for 5 mins");
                    resendbtn1();

                }

                else if(count==2)
                {
                    new sendOtp(number,otp).execute();
                    resend.setEnabled(false);
                }
                else
                    Toast.makeText(getApplicationContext(),"Please wait while we send you the OTP",Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                            //android.os.Process.killProcess(android.os.Process.myPid());
                            //Process.killProcess(Process.myPid());
                            //return;
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("No",null);
            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void firstsleep()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(120000);
                }catch (Exception e){}
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            //resend.setEnabled(false);
                            /*for(int i=59;i>9;i--)
                            {
                                Thread.sleep(1000);
                                resend.setText("1:"+i);
                            }
                            for(int i=9;i>=0;i--)
                            {
                                Thread.sleep(1000);
                                resend.setText("1:0"+i);
                            }
                            for(int i=59;i>9;i--)
                            {
                                Thread.sleep(1000);
                                resend.setText("0:"+i);
                            }
                            for(int i=9;i>=0;i--)
                            {
                                Thread.sleep(1000);
                                resend.setText("0:"+i);
                            }*/
                            resend.setText("RESEND OTP");
                            resend.setEnabled(true);
                        } catch (Exception e) {
                            Log.d("Exception ",""+e);
                        }
                    }
                });
            }
        }).start();


    }
    public void resendbtn1()
    {
        new sendOtp(number,otp).execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(600000);
                }catch (Exception e){}
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        resend.setEnabled(true);
                        resend.setText("RESEND OTP");

                    }
                });
            }
        }).start();


    }

    public int calcotp()
    {
        rand=new Random();
        otp=rand.nextInt(10);
        if(otp==1)
            otp++;
        otp=otp*10+rand.nextInt(10);
        otp=otp*10+rand.nextInt(10);
        otp=otp*10+rand.nextInt(10);
        return otp;
    }

    class allUsers extends AsyncTask<String, String, String>
    {


        @Override
        protected String doInBackground(String... params) {
            Log.d("allUsers classssss","");
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            JSONObject json = jParser.makeHttpRequest(url_all_users, "GET", param);
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");
                Log.d("Will check status","");
                if (success == 1) {
                    Log.d("Successsssssssssssss",""+success);
                    // products found
                    // Getting Array of Products
                    users = json.getJSONArray("users");

                    // looping through All Products
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject c = users.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString("user_id");
                        String exist = c.getString("number");
                        Log.d("number:",""+exist);
                        //Toast.makeText(getApplicationContext(),""+exist,Toast.LENGTH_SHORT).show();
                        if(number.equals(exist))
                        {
                            existing=true;
                            Log.d("existinggg is true now","and broken");
                            break;

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class addUser extends AsyncTask<String,String,String>
    {

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("number",number));
            JSONObject json = jParser.makeHttpRequest(url_add_user, "POST", param);

            return null;
        }
    }


    public static class IncomingSms extends BroadcastReceiver {

        EditText edit;
        Button btn;
        // Get the object of SmsManager
        final SmsManager sms = SmsManager.getDefault();

        public IncomingSms()
        {

        }
        public void onReceive(Context context, Intent intent) {

            // Retrieves a map of extended data from the intent.
            final Bundle bundle = intent.getExtras();

            try {

                if (bundle != null) {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    for (int i = 0; i < pdusObj.length; i++) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                        String senderNum = phoneNumber;
                        String message = currentMessage.getDisplayMessageBody();

                        Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
                        senderNum=senderNum.substring(3);

                        //new MainActivity().setter(message);
                        if(senderNum.equals("DLFIND"))
                        {
                            message=message.substring(31,35);
                            int duration = Toast.LENGTH_LONG;
                            /*Toast toast = Toast.makeText(context,
                                    "senderNum: "+ senderNum + ", message: " + message, duration);*/
                            //toast.show();
                            txt.setText(message);
                            button.performClick();
                            //int duration = Toast.LENGTH_LONG;
                            //Toast toast = Toast.makeText(context,
                            //      "senderNum: "+ senderNum + ", message: " + message, duration);
                            //toast.show();

                        }

                        // Show Alert

                    } // end for loop
                } // bundle is null

            } catch (Exception e) {
                Log.e("SmsReceiver", "Exception smsReceiver" +e);

            }
        }
    }

}

class sendOtp extends AsyncTask<String, String, String>
{
    String number=null;
    int otp;
    public sendOtp(String no,int otpval)
    {
        number=no;
        otp=otpval;
    }
    @Override
    protected String doInBackground(String... params) {

        String authkey = "132727AshR9z6QU9Dg58416307";
        String mobiles = "91"+number;
        String senderId = "DLFIND";
        String message = "Your OTP for DealFind login is "+otp+".";
        String route="4";
        URLConnection myURLConnection=null;
        URL myURL=null;
        BufferedReader reader=null;
        String encoded_message= URLEncoder.encode(message);

//Send SMS API
        String mainUrl="https://control.msg91.com/api/sendhttp.php?";

//Prepare parameter string
        StringBuilder sbPostData= new StringBuilder(mainUrl);
        sbPostData.append("authkey="+authkey);
        sbPostData.append("&mobiles="+mobiles);
        sbPostData.append("&message="+encoded_message);
        sbPostData.append("&route="+route);
        sbPostData.append("&sender="+senderId);

//final string
        mainUrl = sbPostData.toString();
        try
        {
            //prepare connection
            myURL = new URL(mainUrl);
            myURLConnection = myURL.openConnection();
            myURLConnection.connect();
            reader= new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));

            //reading response
            String response;
            while ((response = reader.readLine()) != null)
                //print response
                Log.d("RESPONSE", ""+response);

            //finally close connection
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return null;
    }
}