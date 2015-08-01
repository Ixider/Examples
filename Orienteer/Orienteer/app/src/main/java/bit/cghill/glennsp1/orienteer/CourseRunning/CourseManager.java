package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import bit.cghill.glennsp1.orienteer.Models.CourseResult;

/**
 * Created by S. Glenn on 10-Jun-15.
 */
public class CourseManager extends Activity {//Pretty sure this is pretty hackish
    private GoogleMap mMap;
    private boolean[] mCompletedPoints;
    private ArrayList<Marker> mCourseMarkers;
    private ArrayList<String> mPointInfo;
    private Context mContext;
    private Timer mCourseTimer;
    private Double mTimeTaken;
    private TextView mTimerBox;

    private CourseResult mCourseResults;
    private boolean finished;

    OnFinishedCourseLisenter mOnFinshedCourseListener;
    public interface OnFinishedCourseLisenter { void onFishedCourse(ArrayList<String> times); }

    public CourseManager(Context context, GoogleMap map, TextView timerDisplay, Fragment fragment) {
        mContext = context;
        mMap = map;
        mTimerBox = timerDisplay;

        mOnFinshedCourseListener = (OnFinishedCourseLisenter) fragment;
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init() {
        mCourseMarkers = new ArrayList<>();
        mPointInfo = new ArrayList<>();

        finished = false;

        mTimeTaken = 0.00;

        //Start timer
        mCourseTimer = new Timer();
        mCourseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerMethod();
            }
        }, 0, 10);
    }

    public void addFinishedPlayer(String str) {
        mCourseResults.addFInishedPlayersTime(str);
    }

    public ArrayList<String> getFinishedPlayers() {
        return mCourseResults.getFinishedPlayers();
    }

    public void locationChanged(LatLng location) {
        checkIfOnPoint(location);
    }

    public void drawMap(ArrayList<LatLng> locations) {
        mCompletedPoints = new boolean[locations.size()];
        mCourseResults = new CourseResult(locations.size());

        //Add markers onto map and load up and set up list view
        for(int i = 0; i < locations.size(); i++)
        {
            LatLng currLatlng = locations.get(i);
            mCourseMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(currLatlng)
                    .title("Position " + (i + 1))));

            mPointInfo.add("Position " + (i + 1) + ": Not completed");
            //pointArrayAdapter.notifyDataSetChanged(); TODO this is for the list might overlay or save

            mCompletedPoints[i] = false;
        }
    }

    //Method called by the timer, can only interact with timer thread
    public void timerMethod(){
        //Call thread to use UI
        this.runOnUiThread(Timer_Tick);
        //runOnUiThread(Timer_Tick); //TODO sort this out
    }


    //Thread to interact with UI and update time
    private Runnable Timer_Tick = new Runnable() {
        @Override
        public void run() {
            mTimeTaken += 0.01;

            //New time in 2 decimal places
            String time = getFormatedTime();

            mTimerBox.setText(time);
        }
    };

    private String getFormatedTime() {
        return String.format("%.2f", mTimeTaken) + "s";
    }

    //Checks if player is currently ontop of a point and deals with it
    public void checkIfOnPoint(LatLng currLocation){

        //Loop over all markers to check against
        for(int i=0; i < mCourseMarkers.size(); i++)
        {
            LatLng currMarker = mCourseMarkers.get(i).getPosition();
            float[] result = new float[1];

            //Check distance between player and points. Distance stored in result in meters
            Location.distanceBetween(currLocation.latitude, currLocation.longitude,
                    currMarker.latitude, currMarker.longitude, result);

            //Check if player was close to any marker. Check for 40 meters because of gps inaccuracy
            if(result[0] < 40)
            {
                //At marker
                String newPointInfo = "Point " + (i + 1) + ": Completed. T:" + String.format("%.2f", mTimeTaken);
                mPointInfo.set(i, newPointInfo);
                mCourseResults.setTime(i, mTimeTaken);

                mCourseMarkers.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                mCompletedPoints[i] = true;

                mCourseResults.addPlayerTime(getFormatedTime());

                checkForWin();
            }
        }
    }//End checkIfOnPoint

    public void checkForWin(){
        boolean allPointsDone = true;

        //loop see if all points are true for completed
        for(int i=0; i < mCompletedPoints.length; i++)
        {
            if(!mCompletedPoints[i])
            {
                allPointsDone = false;
            }
        }

        //Stop timer and game
        if(allPointsDone)
        {
            mCourseTimer.cancel();

            mCourseResults.setTimeTaken(mTimeTaken);

            //Stop spamming toast when finished the race
            if(!finished)
            {
                finished = true;
                Toast.makeText(mContext, "Completed in " + String.format("%.2f", mTimeTaken) + "seconds", Toast.LENGTH_LONG).show();
                mOnFinshedCourseListener.onFishedCourse(mCourseResults.getDeviceTimes());
            }
        }
    }//End checkForWin
}
