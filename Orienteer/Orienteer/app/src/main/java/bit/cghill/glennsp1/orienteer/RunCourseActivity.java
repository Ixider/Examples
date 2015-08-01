package bit.cghill.glennsp1.orienteer;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import bit.cghill.glennsp1.orienteer.Models.Course;
import bit.cghill.glennsp1.orienteer.Storage.CourseDBManager;


public class RunCourseActivity extends AppCompatActivity implements OnMapReadyCallback {
    LocationManager locationManager;
    Criteria defaultCriteria;
    MapFragment mapFragment;
    GoogleMap map;
    ArrayList<Marker> markerList; // user list
    String providerName;

    //Variables
    CourseDBManager dbManager;
    Course currCourse; // arraylist of latlng
    ArrayList<String> pointInfo;
    Timer courseTimer;
    Double timeTaken;
    TextView timerBox;
    ArrayAdapter pointArrayAdapter;// List view adapter for checkpoints and time taken

    // deprecated
    Button btnStart;

    boolean[] completedPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_course);

        init();
    }//End onCreate

    //Initialize everything
    public void init(){
        dbManager = new CourseDBManager(getBaseContext());
        markerList = new ArrayList<Marker>();
        pointInfo = new ArrayList<String>();
        pointArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pointInfo);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new startRace());
        timerBox = (TextView)findViewById(R.id.timerBox);

        ListView list = (ListView)findViewById(R.id.listView);
        list.setAdapter(pointArrayAdapter);

        //Get name of course from extras
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            String courseName = extras.getString("courseName");

            //Get all courses
            ArrayList<Course> allCourses = dbManager.getAllCourses();

            //Find the selected course and store it
            for(int i=0; i < allCourses.size(); i++)
            {
                Course c = allCourses.get(i);

                String currCourseName = c.getName();

                if(courseName.equals(currCourseName))
                {
                    currCourse = c;
                }
            }
        }


        //Set up location service
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        defaultCriteria = new Criteria();
        providerName = locationManager.getBestProvider(defaultCriteria, false);

        //Get map and set onMapReady to trigger
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }//End init

    //Method called when map has loaded
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Set map to global so other methods can use it
        map = googleMap;

        //Move map to current player location
        Location currentLocation = locationManager.getLastKnownLocation(providerName);

        //If currentlocation then move map to there, if not default to Dunedin
        if (currentLocation != null)
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 16));
        }
        else
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-45.874036, 170.503566), 16));
        }

        //Load up saved map
        ArrayList<LatLng> locations = currCourse.getLocations();
        completedPoints = new boolean[locations.size()];

        //Add markers onto map and load up and set up list view
        for(int i=0; i < locations.size(); i++)
        {
            LatLng currLatlng = locations.get(i);
            markerList.add(map.addMarker(new MarkerOptions()
                    .position(currLatlng)
                    .title("Position " + (i + 1))));

            pointInfo.add("Position " + (i + 1) + ": Not completed");
            pointArrayAdapter.notifyDataSetChanged();

            completedPoints[i] = false;
        }

        //Add players initial location
        markerList.add(map.addMarker(new MarkerOptions()
                .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .title("You")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

    }//End onMapReady

    //Click handler for start race button
    public class startRace implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Start checking for location updates
            locationManager.requestLocationUpdates(providerName, 5000, 5, new customLocationListener());


            //Disable start button
            btnStart.setEnabled(false);

            timeTaken = 0.00;

            //Start timer
            courseTimer = new Timer();
            courseTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerMethod();
                }
            }, 0, 10);

        }
    }//End startRace

    //Method called by the timer, can only interact with timer thread
    public void timerMethod(){
        //Call thread to use UI
        runOnUiThread(Timer_Tick);
    }

    //Thread to interact with UI and update time
    private Runnable Timer_Tick = new Runnable() {
        @Override
        public void run() {
            timeTaken += 0.01;

            //New time in 2 decimal places
            String time = String.format("%.2f", timeTaken) + "s";

            timerBox.setText(time);
        }
    };

    //Custom class for handling gps change
    public class customLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            int playerMarkerIndex = markerList.size() - 1;

            //Remove marker from map
            markerList.get(playerMarkerIndex).remove();

            //Remover marker from list
            markerList.remove(playerMarkerIndex);

            //Make camera follow player
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

            //Add players new position onto the map
            markerList.add(map.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));

            checkIfOnPoint(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }//End customLocationListener

    //Checks if player is currently ontop of a point and deals with it
    public void checkIfOnPoint(Location currLocation){

        //Loop over all markers to check against
        for(int i=0; i < markerList.size(); i++)
        {
            LatLng currMarker = markerList.get(i).getPosition();
            float[] result = new float[1];

            //Check distance between player and points. Distance stored in result in meters
            Location.distanceBetween(currLocation.getLatitude(), currLocation.getLongitude(),
                    currMarker.latitude, currMarker.longitude, result);

            //Check if player was close to any marker. Check for 40 meters because of gps inaccuracy
            if(result[0] < 40 && i != markerList.size() - 1)
            {
                //At marker
                String newPointInfo = "Point " + (i + 1) + ": Completed. T:" + String.format("%.2f", timeTaken);
                pointInfo.set(i, newPointInfo);
                pointArrayAdapter.notifyDataSetChanged();

                //Remove completed marker from map
                //markerList.get(i).remove();
                markerList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                completedPoints[i] = true;

                checkForWin();
            }
        }
    }//End checkIfOnPoint

    public void checkForWin(){
        boolean allPointsDone = true;

        //loop see if all points are true for completed
        for(int i=0; i < completedPoints.length; i++)
        {
            if(!completedPoints[i])
            {
                allPointsDone = false;
            }
        }

        //Stop timer and game
        if(allPointsDone)
        {
            courseTimer.cancel();

            locationManager.removeUpdates(new customLocationListener());
            Toast.makeText(getBaseContext(), "Completed in " + String.format("%.2f", timeTaken) + "seconds", Toast.LENGTH_LONG).show();
        }
    }//End checkForWin
}//End RunCourseActivity
