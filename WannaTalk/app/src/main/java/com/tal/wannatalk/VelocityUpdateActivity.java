package com.tal.wannatalk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class VelocityUpdateActivity extends Activity {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private Location mCurrentLocation;
    private long mLastTimeSentNotificaiton;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getBaseContext(), "WannaTalk", Toast.LENGTH_LONG).show();
        setContentView(R.layout.velocity_updater);


        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Toast.makeText(getBaseContext(), "onLocationChanged::"+location.toString(), Toast.LENGTH_LONG).show();
                if (mCurrentLocation == null) {
                    mCurrentLocation = location;
                } else {
                    // Called when a new location is found by the network location provider.
                    makeUseOfNewLocation(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(getBaseContext(), "onStatusChanged::"+provider, Toast.LENGTH_LONG).show();
                Log.i("onStatusChanged::", provider);
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(getBaseContext(), "onProviderEnabled::"+provider, Toast.LENGTH_LONG).show();
                Log.i("onProviderEnabled::", provider);

            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(getBaseContext(), "onProviderDisabled::"+provider, Toast.LENGTH_LONG).show();
                Log.i("onProviderDisabled::", provider);
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 200, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 200, locationListener);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    private void makeUseOfNewLocation(Location location) {
        if (isDriving(location)) {
            //TODO: 1. update server
            SharedPreferences sharedPref = this.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String username = sharedPref.getString(getString(R.string.user_email), "empty");
            String phoneNumber = sharedPref.getString(getString(R.string.user_phonenumber), "000");

            userIsDrivingNotifyAll(getTimeMs(location), username);

            //2. show notificaiton bar
            if (mLastTimeSentNotificaiton - SystemClock.currentThreadTimeMillis() > 30*1000) {
                showNotificationBar();
            }
        }
        mCurrentLocation = location;
    }

    private void userIsDrivingNotifyAll(long timeMs, String username) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://www.google.com";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //mTextView.setText("Response is: "+ response.substring(0,500));
                        Toast.makeText(getBaseContext(), "WannaTalk::userIsDrivingNotifyAll::Response is:"+ response.substring(0,500), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Toast.makeText(getBaseContext(), "WannaTalk::userIsDrivingNotifyAll::Response is: That didn't work!", Toast.LENGTH_LONG).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void showNotificationBar() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.velocityupdater_notify_title))
                        .setContentText(getString(R.string.velocityupdater_notify_msg));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(111, mBuilder.build());

    }

    private boolean isDriving(Location location) {
        double speedKmH = 0;
        if (location.hasSpeed()) {
            speedKmH = (int) ((location.getSpeed() * 3600) / 1000);
        } else {
            speedKmH = calculateSpeed(location.getLatitude(), location.getLongitude(), location.getAltitude(), getTimeMs(location),
                    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mCurrentLocation.getAltitude(), getTimeMs(mCurrentLocation));
        }
        return speedKmH > 20;
    }

    private double calculateSpeed(double lat2, double lon2, double alt2, long time2, double lat1, double lon1, double alt1, long time1) {
        double d = distance(lat1, lat2, lon1, lon2, alt1, alt2);
        double t = time2 - time1;
        if (t > 0) {
            return d / t;
        }
        return 0;
    }


    public long getTimeMs(Location last) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return timeMsApi17(last);
        return timeMsApiPre17(last);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private long timeMsApi17(Location last) {
        return last.getElapsedRealtimeNanos();
    }

    private long timeMsApiPre17(Location last) {
        return last.getTime();
    }

    /*
 * Calculate distance between two points in latitude and longitude taking
 * into account height difference. If you are not interested in height
 * difference pass 0.0. Uses Haversine method as its base.
 *
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * el2 End altitude in meters
 * @returns Distance in Meters
 */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }




}
