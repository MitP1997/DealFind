package com.example.mit.dealfind;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.payUMoney.sdk.PayUmoneySdkInitilizer;
import com.payUMoney.sdk.SdkConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class viewDeal extends AppCompatActivity {
    String deal_id, imageurl, hotelname, rate, address, itemname, details, contactnumber, category, distance, hotelcontact;
    double lat,lng,mylat,mylong;
    TextView amount,viewOnMap;
    TextView no;
    TextView distKmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deal);
        Bundle bundle = getIntent().getExtras();
        deal_id = bundle.getString("deal_id");
        imageurl = bundle.getString("imageurl");
        hotelname = bundle.getString("hotelname");
        rate = bundle.getString("rate");
        address = bundle.getString("address");
        itemname = bundle.getString("itemname");
        details = bundle.getString("details");
        contactnumber = bundle.getString("contactnumber");
        category = bundle.getString("category");
        distance = bundle.getString("distance");
        hotelcontact = bundle.getString("hotelcontact");
        lat=bundle.getDouble("lat");
        lng=bundle.getDouble("lng");
        mylat=bundle.getDouble("mylat");
        mylong=bundle.getDouble("mylong");
        //distance=""+getDistanceInfo(lat,lng);
        new Dist().execute();
        ImageView img = (ImageView) findViewById(R.id.image);
        new ImageLoadTask(imageurl, img).execute();
        TextView hotel = (TextView) findViewById(R.id.hotel);
        TextView add = (TextView) findViewById(R.id.address);
        TextView det = (TextView) findViewById(R.id.details);
        TextView itemnameView = (TextView) findViewById(R.id.itemnamee);
        distKmView = (TextView) findViewById(R.id.distKm);
        ImageView catImage = (ImageView) findViewById(R.id.categoryimg);
        TextView call = (TextView) findViewById(R.id.callUs);
        amount = (TextView) findViewById(R.id.amount);
        viewOnMap=(TextView)findViewById(R.id.viewOnMap);
        ImageView buy = (ImageView) findViewById(R.id.buy);
        TextView cat = (TextView) findViewById(R.id.category);
        TextView btnminus = (TextView) findViewById(R.id.btnminus);
        TextView btnplus = (TextView) findViewById(R.id.btnplus);
        no = (TextView) findViewById(R.id.number);
        hotel.setText(hotelname);
        add.setText(address);
        det.setText(details);
        amount.setText("0");
        cat.setText(category);
        itemnameView.setText("" + itemname);

        if (category.equals("Food")) {
            catImage.setImageResource(R.drawable.catfoodblack);
        }

        viewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(viewDeal.this,MapsActivity.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("hotelname",hotelname);
                startActivity(intent);
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(viewDeal.this);
                builder.setMessage("Network Call Charges may apply.")
                        .setPositiveButton("Call", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                callIntent.setData(Uri.parse("tel:"+hotelcontact));
                                startActivity(callIntent);
                            }
                        })
                        .setNegativeButton("Cancel",null);
                AlertDialog alert = builder.create();
                alert.show();
                //Toast.makeText(getApplicationContext(),""+hotelcontact,Toast.LENGTH_LONG).show();

            }
        });

        btnminus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!no.getText().equals("0"))
                {
                    int x=Integer.parseInt(""+no.getText());
                    x--;

                    no.setText(""+x);
                    amount.setText(""+(Integer.parseInt(rate)*x));
                }
            }
        });

        btnplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x=Integer.parseInt(""+no.getText());
                x++;

                no.setText(""+x);
                amount.setText(""+(Integer.parseInt(rate)*x));
            }
        });
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (amount.getText().equals("0")) {
                    AlertDialog.Builder b = new AlertDialog.Builder(viewDeal.this);
                    b.setMessage("Please choose atleast one deal")
                            .setNegativeButton("Ok", null);
                    AlertDialog a=b.create();
                    a.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(viewDeal.this);
                    builder.setMessage("Confirm Purchase?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                    payumoneyinitiater();
                                }
                            })
                            .setNegativeButton("No", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        });
    }

    public void uiSetter()
    {
        viewDeal.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distKmView.setText("" + Math.round(Float.parseFloat(distance)*100.0)/100.0 + " Km");
            }
        });
    }

    public class Dist extends AsyncTask<String,String,String>
    {

        @Override
        protected String doInBackground(String... params) {
            distance=""+getDistanceInfo(lat,lng);
            uiSetter();
            return null;
        }
    }

    private double getDistanceInfo(double latTo, double lngTo) {
        StringBuilder stringBuilder = new StringBuilder();
        Double dist = 0.0;
        try {
            String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + mylat + "," + mylong + "&destination=" + latTo + "," + lngTo + "&mode=driving&sensor=false";

            HttpPost httppost = new HttpPost(url);

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            Log.d("ClientProtocol: ",""+e);
        } catch (IOException e) {
            Log.d("IOE ",""+e);
        }

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject = new JSONObject(stringBuilder.toString());

            JSONArray array = jsonObject.getJSONArray("routes");

            JSONObject routes = array.getJSONObject(0);

            JSONArray legs = routes.getJSONArray("legs");

            JSONObject steps = legs.getJSONObject(0);

            JSONObject distance = steps.getJSONObject("distance");

            Log.i("Distance", distance.toString());
            dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]","") );

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dist;
    }

    public void payumoneyinitiater()
    {
        PayUmoneySdkInitilizer.PaymentParam.Builder builder= new PayUmoneySdkInitilizer.PaymentParam.Builder();
        builder.setMerchantId("4938127");
        builder.setKey("UbizkwYZ");
        builder.setIsDebug(true);
        builder.setAmount(Double.parseDouble(""+amount.getText()));
        builder.setTnxId("DF"+System.currentTimeMillis());
        builder.setPhone(contactnumber);
        builder.setProductName(itemname);
        builder.setFirstName("Guest");
        builder.setEmail("mitvparekh9@yahoo.com");
        builder.setsUrl("https://www.PayUmoney.com/mobileapp/PayUmoney/success.php");
        builder.setfUrl("https://www.PayUmoney.com/mobileapp/PayUmoney/failure.php");
        builder.setUdf1("UDF1");
        builder.setUdf2("UDF2");
        builder.setUdf3("UDF3");
        builder.setUdf4("UDF4");
        builder.setUdf5("UDF5");

        PayUmoneySdkInitilizer.PaymentParam param=builder.build();

                /*PayUmoneySdkInitilizer.startPaymentActivityForResult(viewDeal.this,param){
                    param.setMerchantHash(hash);

                }*/

        calculateServerSideHashAndInitiatePayment(param);
    }
    private void calculateServerSideHashAndInitiatePayment(final PayUmoneySdkInitilizer.PaymentParam paymentParam) {

        // Replace your server side hash generator API URL
        String url = "https://test.payumoney.com/payment/op/calculateHashForTest";

        //Toast.makeText(this, "Please wait... Generating hash from server ... ", Toast.LENGTH_LONG).show();
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has(SdkConstants.STATUS)) {
                        String status = jsonObject.optString(SdkConstants.STATUS);
                        if (status != null || status.equals("1")) {

                            String hash = jsonObject.getString(SdkConstants.RESULT);
                            Log.i("app_activity", "Server calculated Hash :  " + hash);

                            paymentParam.setMerchantHash(hash);

                            PayUmoneySdkInitilizer.startPaymentActivityForResult(viewDeal.this, paymentParam);
                        } else {
                            Toast.makeText(viewDeal.this,
                                    jsonObject.getString(SdkConstants.RESULT),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof NoConnectionError) {
                    Toast.makeText(viewDeal.this,
                            viewDeal.this.getString(R.string.connect_to_internet),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(viewDeal.this,
                            error.getMessage(),
                            Toast.LENGTH_SHORT).show();

                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return paymentParam.getParams();
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PayUmoneySdkInitilizer.PAYU_SDK_PAYMENT_REQUEST_CODE) {

            /*if(data != null && data.hasExtra("result")){
              String responsePayUmoney = data.getStringExtra("result");
                if(SdkHelper.checkForValidString(responsePayUmoney))
                    showDialogMessage(responsePayUmoney);
            } else {
                showDialogMessage("Unable to get Status of Payment");
            }*/


            if (resultCode == RESULT_OK) {
                Log.d("Success - Payment ID : " ,""+ data.getStringExtra(SdkConstants.PAYMENT_ID));
                String paymentId = data.getStringExtra(SdkConstants.PAYMENT_ID);
                showDialogMessage("Payment Success Id : " + paymentId);


            } else if (resultCode == RESULT_CANCELED) {
                Log.d("failure","occurred");
                showDialogMessage("cancelled");
            } else if (resultCode == PayUmoneySdkInitilizer.RESULT_FAILED) {
                Log.i("app_activity", "failure");

                if (data != null) {
                    if (data.getStringExtra(SdkConstants.RESULT).equals("cancel")) {

                    } else {
                        showDialogMessage("failure");
                    }
                }
                //Write your code if there's no result
            } else if (resultCode == PayUmoneySdkInitilizer.RESULT_BACK) {
                Log.d("User returned without","login");
                //showDialogMessage("User returned without login");
                Intent i=new Intent(viewDeal.this,paid.class);
                i.putExtra("deal_id",deal_id);
                i.putExtra("contactnumber",contactnumber);
                i.putExtra("no_deals",""+no.getText());
                i.putExtra("itemname",""+itemname);
                i.putExtra("updation",true);
                startActivity(i);
            }
        }
    }

    private void showDialogMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Detailsss");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }
}
