package bit.cghill.glennsp1.orienteer.MenuScreens;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.input.InputManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;

import bit.cghill.glennsp1.orienteer.Models.Course;
import bit.cghill.glennsp1.orienteer.R;
import bit.cghill.glennsp1.orienteer.Storage.CourseDBManager;


public class FragmentCreateCourse extends Fragment implements OnMapReadyCallback {

    //Variables
    GoogleMap map;
    ArrayAdapter pointArrayAdapter;
    ArrayList<String> pointInfo;
    Course newCourse;
    ArrayList<Marker> markerList;
    int selectedMarkerIndex;
    Button btnRemove;
    EditText nameBox;
    CourseDBManager dbManager;
    ListView pointList;

    OnResultListener mOnResultListener;
    public interface OnResultListener { void onResult(); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_create_course, container, false);
        init(v);
        return v;
    }

    //Initalizing variables
    private void init(View v){
        dbManager = new CourseDBManager(getActivity());
        newCourse = new Course();
        pointInfo = new ArrayList<String>();
        pointArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, pointInfo);
        markerList = new ArrayList<Marker>();

        //Get map and set onMapReady to trigger
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pointList = (ListView)v.findViewById(R.id.pointList);
        pointList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        pointList.setAdapter(pointArrayAdapter);
        pointList.setOnItemClickListener(new selectmarker());

        nameBox = (EditText)v.findViewById(R.id.nameBox);

        Button btnCreateCourse = (Button)v.findViewById(R.id.btnCreateCourse);
        btnCreateCourse.setOnClickListener(new createCourse());

        btnRemove = (Button)v.findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(new removeMarker());
        btnRemove.setEnabled(false);
    }//End init

    public boolean checkForUniqueName(String name){
        ArrayList<Course> allCourses = dbManager.getAllCourses();

        boolean canUseName = true;

        for(Course curr: allCourses){
            String currName = curr.getName();

            if(currName.equals(name))
                canUseName = false;
        }

        return canUseName;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Get instance of map for use in other methods
        map = googleMap;
        map.setOnMapClickListener(new onMapClick());

        //Get location and set map to current location
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria defaultCriteria = new Criteria();
        String providerName = locationManager.getBestProvider(defaultCriteria, false);

        Location currentLocation = locationManager.getLastKnownLocation(providerName);

        //If currentlocation then move map to there, if not default to Dunedin
        if (currentLocation != null)
        {
            LatLng currLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 16));

            IconGenerator iconGenerator = new IconGenerator(getActivity());
            Bitmap bitmap = iconGenerator.makeIcon("You");

            // Get back the mutable marker so it can be removed later
            map.addMarker(new MarkerOptions().position(currLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(currLatLng);
            circleOptions.fillColor(25);
            circleOptions.radius(3); // In meters

            map.addCircle(circleOptions);

        }
        else
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-45.874036, 170.503566), 16));
        }

    }//End onMapReady

    //Handler for when map is clicked
    public class onMapClick implements  OnMapClickListener{

        //Adds a marker onto map and stores in helpful places
        @Override
        public void onMapClick(LatLng latLng) {
            //Add point to course
            newCourse.addPoint(latLng);
            int pointNumber = newCourse.getSize();

            //Add display feedback
            pointArrayAdapter.add("Position " + pointNumber);

            //Add marker onto map
            markerList.add(map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Position " + pointNumber)));

            //Update display
            pointArrayAdapter.notifyDataSetChanged();

            //Scroll list to bottom
            pointList.setSelection(pointArrayAdapter.getCount() - 1);
        }
    }//End onMapClick

    //Button hanlder for when finished
    public class createCourse implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            String newName = nameBox.getText().toString();

            boolean usableName = checkForUniqueName(newName);

            if(usableName)
            {
                newCourse.setName(newName);

                dbManager.insertCourse(newCourse);

                //Send back to main page
                Toast.makeText(getActivity(), "Course saved", Toast.LENGTH_LONG).show();

                mOnResultListener.onResult();
            }
            else
            {
                Toast.makeText(getActivity(), "Name already taken", Toast.LENGTH_LONG).show();
            }

        }//End onClick
    }//End createCourse

    //Handler for listview click, stores position of clicked item
    public class selectmarker implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedMarkerIndex = position;
                btnRemove.setEnabled(true);
        }
    }//end selectmarker

    //Handler for remove button. Removes listview clicked item from map and lists
    public class removeMarker implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //Remove from map
            markerList.get(selectedMarkerIndex).remove();

            //Remove from lists
            markerList.remove(selectedMarkerIndex);

            //Remove from the course
            newCourse.removePoint(selectedMarkerIndex);

            //Disable button again
            btnRemove.setEnabled(false);

            renameAfterRemove();


            pointArrayAdapter.notifyDataSetChanged();
        }
    }//end removeMarker

    public void renameAfterRemove(){
        pointArrayAdapter.clear();

        for(int i=0; i < markerList.size(); i++)
        {
            int newPos = i + 1;

            markerList.get(i).setTitle("Position " + newPos);

            pointArrayAdapter.add("Position " + newPos);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mOnResultListener = (OnResultListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnResultListener");
        }
    }//end onAttach

    public void onDestroyView() {
        super.onDestroyView();
        try {
            FragmentManager fm = getActivity().getFragmentManager();
            Fragment fragment = (fm.findFragmentById(R.id.map));
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
        catch(Exception e)
        {
            Log.wtf("Error", "Error destroying fragment");
        }
    }
}//end CreateCourseActivity
