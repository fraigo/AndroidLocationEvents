package me.franciscoigor.locationevents;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOCATION_PERMISSION = 1001;
    ImageView mapView;
    TextView text;
    HashMap<String,Drawable> webCache;
    SeekBar seekBar;

    int mapZoom = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webCache = new HashMap<String, Drawable>();

        text = findViewById(R.id.main_text);
        mapView = findViewById(R.id.mapview);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mapZoom = progress+5;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            text.setText("Permission requested");
        } else {
            text.setText("Permission granted");


            getLocation();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION){
            System.out.println(Arrays.asList(permissions));
            System.out.println(Arrays.asList(grantResults));

        }
    }

    private void getLocation(){

        FusedLocationProviderClient flpClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest= LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
        try {
            flpClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper());
        } catch(SecurityException ex){
            ex.printStackTrace();
        }

    }

    private void setMapImage(Location loc){
        String mapsUrl="ENDPOINT?center=LATITUDE%2c%20LONGITUDE&markers=color:red%7Clabel:o%7CLATITUDE%2c%20LONGITUDE&zoom=ZOOM&size=400x400&maptype=MAPTYPE";
        final String url = mapsUrl
                .replace("ENDPOINT", "https://maps.googleapis.com/maps/api/staticmap")
                .replace("MAPTYPE", "satellite")
                .replace("ZOOM", String.valueOf(mapZoom))
                .replace("LATITUDE", String.valueOf(loc.getLatitude()))
                .replace("LONGITUDE", String.valueOf(loc.getLongitude()));
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
        return String.format("%3.5f : %3.5f (%3.3f) Time:%s",
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
