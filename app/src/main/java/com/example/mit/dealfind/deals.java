package com.example.mit.dealfind;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class deals extends AppCompatActivity {
    JSONParser jParser = new JSONParser();
    String url_all_deals = "http://couponfind.in/get_deal.php";
    String url_Hotel_details = "http://couponfind.in/get_hoteldetails.php";
    JSONArray deals = null, hotels = null;
    ProgressDialog pDialog;
    JSONObject jsonDeals, jsonHotels;
    LinearLayout mainLayout;
    String contactnumber;
    double mylat,mylong;
    String deal_id[],strhotel[],imageurl[],itemname[],rate[],strdetails[],address[],category[],hotelcontact[];
    double distance[],lat[],lng[];
    ImageView ord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deals);

        Bundle bundle = getIntent().getExtras();
        contactnumber = bundle.getString("contactnumber");
        mylat=bundle.getDouble("mylat");
        mylong=bundle.getDouble("mylong");
        Log.d("First intent values :",""+mylat+","+mylong);
       // Toast.makeText(getApplicationContext(),contactnumber,Toast.LENGTH_SHORT).show();
        //final Location myLocation=new Location("myLocation");

        mainLayout = (LinearLayout) findViewById(R.id.alldeals);
        ord=(ImageView)findViewById(R.id.orders);


        new LoadAllDeals().execute();

        ord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(deals.this,account.class);
                intent.putExtra("contactnumber",contactnumber);
                intent.putExtra("mylat",mylat);
                intent.putExtra("mylong",mylong);
                startActivity(intent);
            }
        });
    }

    public double calcDistance(double lat1,double lon1,double lat2,double lon2)
    {
        double x,a2,b2;
        a2=Math.pow((lat2-lat1),2);
        b2=Math.pow((lon2-lon1),2);

        x=Math.pow(a2+b2,0.5);

        return x;
    }
    AlertDialog alert;
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

                            closeApp();

                        }
                    })
                    .setNegativeButton("No",null);
            alert = builder.create();
            alert.show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void closeApp()
    {
        alert.dismiss();
        //finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void createLayoutNew(final int i)
    {
        deals.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    RelativeLayout rl=new RelativeLayout(getApplicationContext());
                    RelativeLayout.LayoutParams rlparam=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,450);
                    rlparam.setMargins(100,100,100,100);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 450);
                    params.setMargins(0,10,0,10);

                    rl.setLayoutParams(params);



                    TextView item=new TextView(getApplicationContext());
                    Log.d("Item",itemname[i]);
                    item.setText(itemname[i]);
                    RelativeLayout.LayoutParams itemparam=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    item.setTextSize(16);
                    itemparam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    itemparam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    itemparam.setMargins(75,0,0,15);
                    item.setLayoutParams(itemparam);
                    item.setTextColor(Color.WHITE);

                    ImageView icon=new ImageView(getApplicationContext());
                    RelativeLayout.LayoutParams iconparam=new RelativeLayout.LayoutParams(50,50);
                    iconparam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    iconparam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    iconparam.setMargins(10,0,0,15);
                    icon.setLayoutParams(iconparam);
                    icon.setImageResource(R.drawable.foodcat);
                    icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    TextView ratetxt=new TextView(getApplicationContext());
                    ratetxt.setText("Rs. "+rate[i]);
                    RelativeLayout.LayoutParams rateparam=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ratetxt.setTextSize(16);
                    rateparam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    rateparam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rateparam.setMargins(0,0,15,15);
                    ratetxt.setLayoutParams(rateparam);
                    ratetxt.setTextColor(Color.WHITE);

                    View grad=new View(getApplicationContext());
                    RelativeLayout.LayoutParams vuparam=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
                    vuparam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    grad.setLayoutParams(vuparam);
                    grad.setBackgroundResource(R.drawable.gradient);

                    ImageView img=new ImageView(getApplicationContext());
                    img.setImageResource(R.drawable.abc);
                    new ImageLoadTask(imageurl[i], img).execute();
                    RelativeLayout.LayoutParams imgparam=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    imgparam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    img.setLayoutParams(imgparam);


                    rl.addView(img);

                    rl.addView(grad);
                    rl.addView(item);
                    rl.addView(ratetxt);
                    rl.addView(icon);

                    rl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent it=new Intent(deals.this,viewDeal.class);
                            it.putExtra("deal_id",deal_id[i]);
                            it.putExtra("imageurl",imageurl[i]);
                            it.putExtra("hotelname",strhotel[i]);
                            it.putExtra("rate",rate[i]);
                            it.putExtra("address",address[i]);
                            it.putExtra("itemname",itemname[i]);
                            it.putExtra("details",strdetails[i]);
                            it.putExtra("contactnumber",contactnumber);
                            it.putExtra("category",category[i]);
                            it.putExtra("distance",""+distance[i]);
                            it.putExtra("hotelcontact",hotelcontact[i]);
                            it.putExtra("lat",lat[i]);
                            it.putExtra("lng",lng[i]);
                            it.putExtra("mylat",mylat);
                            it.putExtra("mylong",mylong);
                            startActivity(it);
                        }
                    });
                    mainLayout.addView(rl);

                }catch(Exception e){
                    Log.d("Exception in new",""+e);
                }
            }
        });
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            Log.d("loading image ","task");
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
                Log.d("kuch toh gadbad hai","daya");
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

    class LoadAllDeals extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(deals.this);
            pDialog.setMessage("Loading deals. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            jsonDeals = jParser.makeHttpRequest(url_all_deals, "GET", param);
            jsonHotels = jParser.makeHttpRequest(url_Hotel_details, "GET", param);
            try {
                if (jsonDeals.getInt("success") == 1 && jsonHotels.getInt("success")==1) {
                    deals = jsonDeals.getJSONArray("deals");
                    hotels = jsonHotels.getJSONArray("hotels");
                    distance=new double[deals.length()];
                    deal_id=new String[deals.length()];
                    strhotel=new String[deals.length()];
                    imageurl=new String[deals.length()];
                    itemname=new String[deals.length()];
                    rate=new String[deals.length()];
                    address=new String[deals.length()];
                    strdetails=new String[deals.length()];
                    category=new String[deals.length()];
                    hotelcontact=new String[deals.length()];
                    lat=new double[deals.length()];
                    lng=new double[deals.length()];
                    for (int i = 0; i < deals.length(); i++) {
                        JSONObject cdeals = deals.getJSONObject(i);
                        Log.d("Iterating json:",""+i);
                        deal_id[i]=cdeals.getString("deal_id");
                        strhotel[i]=cdeals.getString("hotelname");
                        JSONObject chotels=null;
                        for(int j=0;j<hotels.length();j++)
                        {
                            chotels= hotels.getJSONObject(j);
                            if(chotels.getString("hotelname").equals(strhotel[i]))
                            {
                                Log.d("hotelname found",strhotel[i]);
                                break;
                            }
                        }
                        hotelcontact[i]=chotels.getString("contact");
                        imageurl[i]=chotels.getString("imageurl");
                        itemname[i]=cdeals.getString("itemname");
                        rate[i]=cdeals.getString("rate");
                        strdetails[i]=cdeals.getString("details");
                        category[i]=cdeals.getString("category");
                        address[i]=chotels.getString("address");
                        lat[i]=(double)(Double.parseDouble(chotels.getString("lat")));
                        lng[i]=(double)(Double.parseDouble(chotels.getString("long")));
                        distance[i]=calcDistance(mylat,mylong,lat[i],lng[i]);
                        Log.d("distance cal: ",""+distance[i]);
                    }
                    for(int i=0;i<deals.length()-1;i++)
                    {
                        for(int j=0;j<deals.length()-i-1;j++)
                        {
                            Log.d(""+i,""+j);
                            if(distance[j]>distance[j+1])
                            {
                                String temp=null;
                                temp=deal_id[j];
                                deal_id[j]=deal_id[j+1];
                                deal_id[j+1]=temp;

                                temp=strhotel[j];
                                strhotel[j]=strhotel[j+1];
                                strhotel[j+1]=temp;
                                Log.d(""+strhotel[j],""+strhotel[j+1]);
                                temp=imageurl[j];
                                imageurl[j]=imageurl[j+1];
                                imageurl[j+1]=temp;

                                temp=itemname[j];
                                itemname[j]=itemname[j+1];
                                itemname[j+1]=temp;

                                temp=hotelcontact[j];
                                hotelcontact[j]=hotelcontact[j+1];
                                hotelcontact[j+1]=temp;

                                temp=rate[j];
                                rate[j]=rate[j+1];
                                rate[j+1]=temp;

                                temp=strdetails[j];
                                strdetails[j]=strdetails[j+1];
                                strdetails[j+1]=temp;

                                temp=address[j];
                                address[j]=address[j+1];
                                address[j+1]=temp;

                                temp=category[j];
                                category[j]=category[j+1];
                                category[j+1]=temp;

                                double t;
                                t=distance[j];
                                distance[j]=distance[j+1];
                                distance[j+1]=t;

                                t=lat[j];
                                lat[j]=lat[j+1];
                                lat[j+1]=t;

                                t=lng[j];
                                lng[j]=lng[j+1];
                                lng[j+1]=t;
                            }
                        }
                    }
                    for(int i=0;i<deals.length();i++)
                    {
                        distance[i]=calcDistance(mylat,mylong,lat[i],lng[i]);
                        Log.d("Final dist ",""+distance[i]);
                    }

                    for(int i=0;i<deals.length()-1;i++)
                    {
                        for(int j=0;j<deals.length()-i-1;j++)
                        {
                            Log.d(""+i,""+j);
                            if(distance[j]>distance[j+1])
                            {
                                String temp=null;
                                temp=deal_id[j];
                                deal_id[j]=deal_id[j+1];
                                deal_id[j+1]=temp;

                                temp=strhotel[j];
                                strhotel[j]=strhotel[j+1];
                                strhotel[j+1]=temp;
                                Log.d(""+strhotel[j],""+strhotel[j+1]);
                                temp=imageurl[j];
                                imageurl[j]=imageurl[j+1];
                                imageurl[j+1]=temp;

                                temp=itemname[j];
                                itemname[j]=itemname[j+1];
                                itemname[j+1]=temp;

                                temp=hotelcontact[j];
                                hotelcontact[j]=hotelcontact[j+1];
                                hotelcontact[j+1]=temp;

                                temp=rate[j];
                                rate[j]=rate[j+1];
                                rate[j+1]=temp;

                                temp=strdetails[j];
                                strdetails[j]=strdetails[j+1];
                                strdetails[j+1]=temp;

                                temp=address[j];
                                address[j]=address[j+1];
                                address[j+1]=temp;

                                temp=category[j];
                                category[j]=category[j+1];
                                category[j+1]=temp;

                                double t;
                                t=distance[j];
                                distance[j]=distance[j+1];
                                distance[j+1]=t;

                                t=lat[j];
                                lat[j]=lat[j+1];
                                lat[j+1]=t;

                                t=lng[j];
                                lng[j]=lng[j+1];
                                lng[j+1]=t;
                            }
                        }
                    }
                    for(int i=0;i<deals.length();i++)
                    {
                        createLayoutNew(i);
                    }
                }
            } catch (Exception e) {
                Log.d("Error in LoadAll", "" + e);
            }
            onPostExecute();
            return null;
        }

        protected void onPostExecute()
        {
            pDialog.dismiss();
        }
    }
}
