package me.franciscoigor.locationevents;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOCATION_PERMISSION = 1001;
    ImageView mapView;
    TextView text;
    HashMap<String,Drawable> webCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webCache = new HashMap<String, Drawable>();

        text = findViewById(R.id.main_text);
        mapView = findViewById(R.id.mapview);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            text.setText("Permission requested");
        } else {
            text.setText("Permission granted");
            LocationRequest locationRequest= LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


            FusedLocationProviderClient flpClient =
                    LocationServices.getFusedLocationProviderClient(this);

            LocationCallback callback=new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    for (final Location location : locationResult.getLocations()) {
                        text.setText(text.getText().toString()+"\n"+locationDesc(location));
                        Thread t=new Thread(new Runnable() {
                            @Override
                            public void run() {
                                setMapImage(location);
                            }
                        });
                        t.start();
                    }

                }
            };
            flpClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper());
            flpClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location==null) return;

                    text.setText(locationDesc(location));

                }
            });

        }

    }

    private void setMapImage(Location loc){
        String mapsUrl="https://maps.googleapis.com/maps/api/staticmap?center=LATITUDE%2c%20LONGITUDE&markers=color:red%7Clabel:o%7CLATITUDE%2c%20LONGITUDE&zoom=18&size=400x400";
        final String url = mapsUrl
                .replace("LATITUDE", String.valueOf(Math.round(loc.getLatitude()*1000)/1000.0d))
                .replace("LONGITUDE", String.valueOf(Math.round(loc.getLongitude()*1000)/1000.0d));
        final Drawable d=loadImageFromWebOperations(url);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.setImageDrawable(d);
            }
        });

    }

    private String locationDesc(Location location){
        if (location==null){
            return "No location";
        }
        return String.format("%3.3f : %3.3f (%3.3f) Time:%s",
                (location.getLatitude()),
                (location.getLongitude()),
                (location.getAccuracy()),
                SimpleDateFormat.getTimeInstance().format(new Date(location.getTime()))
        );
    }


    public  Drawable loadImageFromWebOperations(String url) {
        Drawable d= webCache.get(url);
        if (d!=null){
            return d;
        }
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            d = Drawable.createFromStream(is, "image");
            webCache.put(url,d);
            return d;
        } catch (Exception e) {
            text.setText(text.getText().toString()+"\nError:"+e.getMessage()+":"+e.getStackTrace()[0].getLineNumber());
            e.printStackTrace();
            return null;
        }
    }

    //

}
