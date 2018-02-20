package com.example.mit.dealfind;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class paid extends AppCompatActivity {
    String deal_id,contactnumber,qrInputText,no_deals,itemname;
    JSONParser jsonParser=new JSONParser();
    String url_add_order="http://couponfind.in/add_order.php";
    Boolean updation=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid);
        Bundle bundle = getIntent().getExtras();
        deal_id = bundle.getString("deal_id");
        contactnumber=bundle.getString("contactnumber");
        no_deals=bundle.getString("no_deals");
        itemname=bundle.getString("itemname");
        updation=bundle.getBoolean("updation");
        qrInputText=coder(deal_id,contactnumber);
        if(updation)
        {
            new addOrder().execute();
            new SendConfirmationMessage().execute();
        }

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                150);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            ImageView myImage = (ImageView) findViewById(R.id.qrcode);
            myImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        TextView description=(TextView)findViewById(R.id.desc);
        description.setText(no_deals+" deal(s) of "+itemname+" confirmed.");
        TextView txt=(TextView)findViewById(R.id.code);
        txt.setText(qrInputText);

    }

    public String coder(String deal_id,String contactnumber){
        String qr="DLFD";
        for(int i=0;i<10;i+=2)
        {
            qr=qr+contactnumber.charAt(i);
            if(i%3==0)
                qr+=deal_id.charAt(0);
        }
        return qr;
    }

    public class addOrder extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... param) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("number", contactnumber));
            params.add(new BasicNameValuePair("deal_id",deal_id));
            params.add(new BasicNameValuePair("no_deals",no_deals));
            params.add(new BasicNameValuePair("uid",qrInputText));
            JSONObject json = jsonParser.makeHttpRequest(url_add_order,
                    "POST", params);
            return null;
        }
    }

    public class SendConfirmationMessage extends AsyncTask<String,String,String>
    {

        @Override
        protected String doInBackground(String... params) {

            String authkey = "132727AshR9z6QU9Dg58416307";
            String mobiles = "91"+contactnumber;
            String senderId = "DLFIND";
            String message = "Payment Confirmed for "+no_deals+" deals of "+itemname+" Your unique id is "+qrInputText+".";
            String route="4";
            URLConnection myURLConnection=null;
            URL myURL=null;
            BufferedReader reader=null;
            String encoded_message= URLEncoder.encode(message);
            String mainUrl="https://control.msg91.com/api/sendhttp.php?";

            StringBuilder sbPostData= new StringBuilder(mainUrl);
            sbPostData.append("authkey="+authkey);
            sbPostData.append("&mobiles="+mobiles);
            sbPostData.append("&message="+encoded_message);
            sbPostData.append("&route="+route);
            sbPostData.append("&sender="+senderId);

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

}
