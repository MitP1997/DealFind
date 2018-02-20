package com.example.mit.dealfind;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {
    double mylat=0,mylong=0;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Boolean access=false,found=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        i.putExtra("mylat",mylat);
        i.putExtra("mylong",mylong);
        startActivity(i);
        locationListener = new LocationListener() {
            int count=0;
            @Override
            public void onLocationChanged(Location location) {
                count++;
                if(count==1)
                {
                    mylat=location.getLatitude();
                    mylong=location.getLongitude();
                    found=true;
                    //finish();
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    i.putExtra("mylat",mylat);
                    i.putExtra("mylong",mylong);
                    startActivity(i);
                   // Toast.makeText(getApplication(),"Lat: "+location.getLatitude()+"Long: "+location.getLongitude(),Toast.LENGTH_LONG).show();
                }
                //txtview.append(":\n"+location.getLatitude()+"\t"+location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                    access=true;
            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET
                },10);
                return;
            }
        }else
        {
            configureButton();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(access)
                {
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    i.putExtra("mylat",mylat);
                    i.putExtra("mylong",mylong);
                    startActivity(i);
                }
                else
                {
                    while(!access)
                    {}
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    i.putExtra("mylat",mylat);
                    i.putExtra("mylong",mylong);
                    startActivity(i);
                }

            }
        }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent=new Intent(SplashScreen.this,SplashScreen.class);
        startActivity(intent);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        //txtview.setText("requestingPermission");
        switch(requestCode){
            case 10:
                if(grantResults.length>=0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }

    private void configureButton() {
        locationManager.requestLocationUpdates("network",1000,0,locationListener);

    }
}
