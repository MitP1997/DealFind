package com.example.mit.dealfind;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.payUMoney.sdk.PayUmoneySdkInitilizer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class account extends AppCompatActivity {
    String url_all_orders="http://couponfind.in/get_orderbynumber.php";
    String url_all_deals="http://couponfind.in/get_deal.php";
    String contactnumber;
    JSONParser jParser=new JSONParser();
    JSONObject jsonOrders,jsonDeals;
    JSONArray orders,deals;
    TextView all,unv;
    String[] deal_id,verification,no_deals,hotelname,itemname,details,category;
    LinearLayout mainLayout;
    Boolean alll=true,unver=false,zeroOdder=false;
    ProgressDialog pDialog;
    double mylat,mylong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acc);
        contactnumber=getIntent().getExtras().getString("contactnumber");
        mylat=getIntent().getExtras().getDouble("mylat");
        mylong=getIntent().getExtras().getDouble("mylong");
        new AllOrderByNumber().execute();
        all=(TextView)findViewById(R.id.all);
        unv=(TextView)findViewById(R.id.unv);
        mainLayout=(LinearLayout)findViewById(R.id.scroller);

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alll)
                {   int count=0;
                    alll=false;
                    unver=true;
                    all.setBackgroundResource(R.drawable.leftuncolored);
                    all.setTextColor(Color.parseColor("#4d2464"));
                    unv.setBackgroundResource(R.drawable.rightcolored);
                    unv.setTextColor(Color.WHITE);
                   // mainLayout.getFocusedChild().setVisibility(View.GONE);

                    if(!zeroOdder) {
                        for (int i = 0; i < orders.length(); i++) {
                            if (verification[i].equals("1")) {
                                LinearLayout ll = (LinearLayout) findViewById(i);
                                ll.setVisibility(View.GONE);
                                count++;
                            } else {
                                LinearLayout ll = (LinearLayout) findViewById(i);
                                ll.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                }
                else
                {
                    alll=true;
                    unver=false;
                    all.setBackgroundResource(R.drawable.leftcolored);
                    all.setTextColor(Color.WHITE);
                    unv.setBackgroundResource(R.drawable.rightuncolored);
                    unv.setTextColor(Color.parseColor("#4d2464"));

                    if(!zeroOdder) {
                        for (int i = 0; i < orders.length(); i++) {
                            LinearLayout ll = (LinearLayout) findViewById(i);
                            ll.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        unv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unver)
                {
                    alll=true;
                    unver=false;
                    all.setBackgroundResource(R.drawable.leftcolored);
                    all.setTextColor(Color.WHITE);
                    unv.setBackgroundResource(R.drawable.rightuncolored);
                    unv.setTextColor(Color.parseColor("#4d2464"));

                    if(!zeroOdder) {
                        for (int i = 0; i < orders.length(); i++) {
                            LinearLayout ll = (LinearLayout) findViewById(i);
                            ll.setVisibility(View.VISIBLE);
                        }
                    }

                }
                else
                {
                    alll=false;
                    unver=true;
                    all.setBackgroundResource(R.drawable.leftuncolored);
                    all.setTextColor(Color.parseColor("#4d2464"));
                    unv.setBackgroundResource(R.drawable.rightcolored);
                    unv.setTextColor(Color.WHITE);

                    if(!zeroOdder) {
                        for (int i = 0; i < orders.length(); i++) {
                            if (verification[i].equals("1")) {
                                LinearLayout ll = (LinearLayout) findViewById(i);
                                ll.setVisibility(View.GONE);
                            } else {
                                LinearLayout ll = (LinearLayout) findViewById(i);
                                ll.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        });



    }

    public void createZero()
    {
        account.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView txtview=new TextView(getApplicationContext());
                txtview.setText("No orders to be shown, Find Your Deal!");
                txtview.setTextColor(Color.parseColor("#f04d2464"));
                txtview.setTextSize(18);

                LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                para.setMargins(10, 5, 5, 0);

                txtview.setLayoutParams(para);

                mainLayout.addView(txtview);
                txtview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(account.this,deals.class);
                        intent.putExtra("contactnumber",contactnumber);
                        intent.putExtra("mylat",mylat);
                        intent.putExtra("mylong",mylong);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public void createLayout(final int i)
    {
        account.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LinearLayout ll = new LinearLayout(getApplicationContext());
                    ll.setOrientation(LinearLayout.VERTICAL);
                    TextView item = new TextView(getApplicationContext());
                    TextView hotel = new TextView(getApplicationContext());
                    View v = new View(getApplicationContext());
                    LinearLayout.LayoutParams vupar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    vupar.setMargins(5, 5, 5, 0);
                    v.setLayoutParams(vupar);
                    v.setBackgroundColor(Color.parseColor("#ff4d2464"));
                    LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    par.setMargins(10, 5, 5, 0);
                    item.setLayoutParams(par);
                    LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    para.setMargins(10, 5, 5, 0);
                    hotel.setLayoutParams(para);
                    ll.setBackgroundColor(Color.WHITE);
                    item.setTextColor(Color.parseColor("#ff4d2464"));
                    hotel.setTextColor(Color.parseColor("#f04d2464"));
                    item.setText(itemname[i]);
                    item.setTextSize(18);
                    hotel.setText(hotelname[i]);
                    hotel.setTextSize(12);
                    ll.setId(i);
                    ll.addView(item);
                    ll.addView(hotel);
                    ll.addView(v);

                    ll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(account.this, paid.class);
                            intent.putExtra("deal_id", deal_id[i]);
                            intent.putExtra("contactnumber", contactnumber);
                            intent.putExtra("no_deals", "" + no_deals[i]);
                            intent.putExtra("itemname", "" + itemname[i]);
                            intent.putExtra("updation", false);
                            startActivity(intent);
                        }
                    });

                    mainLayout.addView(ll);
                }catch (Exception e){

                    Log.d("Exception in creation :" ,""+e);
                }
            }
        });
    }

    public class AllOrderByNumber extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(account.this);
            pDialog.setMessage("Loading orders. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            jsonDeals=jParser.makeHttpRequest(url_all_deals,"GET",param);

            List<NameValuePair> p = new ArrayList<NameValuePair>();
            p.add(new BasicNameValuePair("number",contactnumber));
            // getting JSON string from URL
            jsonOrders = jParser.makeHttpRequest(url_all_orders, "GET", p);
            try{
                Log.d("Orders ",""+jsonOrders.getInt("success"));
                Log.d("Deals ",""+jsonDeals.getInt("success"));
                if(jsonOrders.getInt("success")==1 && jsonDeals.getInt("success")==1)
                {
                    Log.d("Inside ","if");
                    orders=jsonOrders.getJSONArray("orders");
                    Log.d("JSON orders","");
                    deals=jsonDeals.getJSONArray("deals");
                    deal_id=new String[orders.length()];
                    no_deals=new String[orders.length()];
                    verification=new String[orders.length()];
                    hotelname=new String[orders.length()];
                    itemname=new String[orders.length()];
                    details=new String[orders.length()];
                    category=new String[orders.length()];
                    for(int i=0;i<orders.length();i++)
                    {
                        JSONObject c=orders.getJSONObject(i);
                        deal_id[i]=c.getString("deal_id");
                        no_deals[i]=c.getString("no_deals");
                        verification[i]=c.getString("verification");
                        for(int j=0;j<deals.length();j++)
                        {
                            JSONObject d=deals.getJSONObject(j);
                            Log.d("Deal id: ",""+deal_id[i]);
                            if(d.getString("deal_id").equals(deal_id[i]))
                            {
                                hotelname[i]=d.getString("hotelname");
                                itemname[i]=d.getString("itemname");
                                details[i]=d.getString("details");
                                category[i]=d.getString("category");
                                Log.d("Deal id: ",""+deal_id[i]);
                                break;
                            }
                        }

                    }
                    for(int i=0;i<orders.length();i++)
                    {
                        createLayout(i);
                    }

                }
                else if(jsonOrders.getInt("success")==0)
                {
                    zeroOdder=true;
                    createZero();
                }
            }catch(Exception e){
                Log.d("Exception in async: ",""+e);
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
