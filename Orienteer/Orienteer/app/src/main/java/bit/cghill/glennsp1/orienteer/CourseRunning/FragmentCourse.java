package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bit.cghill.glennsp1.orienteer.Models.User;
import bit.cghill.glennsp1.orienteer.R;

/**
 * Created by S. Glenn on 19-May-15.
 */


public class FragmentCourse extends Fragment implements CourseManager.OnFinishedCourseLisenter {

    private static final int CIRCLE_DIAMETER = 2;
    private static final int ZOOM_ON_ENTRY = 16;
    private static final int MIN_LOCATION_REQUEST_TIME = 5000;
    private static final int MIN_DISTANCE_CHANGE = 0;

    private GoogleMap mMap;
    private Socket mSocket;
    private List<User> mUserList;
    private JsonParser mJsonParser;
    private ActivityRunCourse mActivity;
    private LocationManager mLocationManager;
    private CourseManager mCourseManager;
    private ArrayList<LatLng> mCourseLocations;
    private TextView mTimerDisplay;
    private Button mBtnResults;


    //TODO tightly coupled fragment and activity : pass username in with args remove get username calls because it rely's on a single activity not modular
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_running_course_layout, container, false);
        init(v);
        return v;
    }

    private void init(View v) {
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment fragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        fragment.getMapAsync(onMapReadyCallback);

        mTimerDisplay = (TextView)v.findViewById(R.id.timerBox);
        mBtnResults = (Button)v.findViewById(R.id.view_results_button);

        mUserList = new ArrayList<>();
        mJsonParser = new JsonParser();
        mCourseLocations = new ArrayList<>();

        Bundle b = getArguments();
        String str = b.getString("course");

        JsonParser parser = new JsonParser();
        mCourseLocations = parser.buildCourse(str);

        mActivity = (ActivityRunCourse)getActivity();

        mSocket = mActivity.getSocket();

        //Application events
        mSocket.on("position changed", onPositionedChanged);  //Start listening to map events
        mSocket.on("player joined map", onPlayerJoinedMap);   //Handles are at the bottom of the class
        mSocket.on("user left", onUserLeft);
        mSocket.on("other user finished", onOtherUserFinished);

        //Server Events
        mSocket.on(Socket.EVENT_RECONNECT, onReconnect);
        mSocket.on(Socket.EVENT_RECONNECTING, onReconnecting);
        mSocket.on(Socket.EVENT_RECONNECT_FAILED, onReconnectionFailed);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        //Request regular locational updates
        mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(getProvider(), MIN_LOCATION_REQUEST_TIME, MIN_DISTANCE_CHANGE, locationListener);
    }//End init

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(locationListener);
        //Stop listening to map events
        mSocket.off("position changed", onPositionedChanged);
        mSocket.off("player joined map", onPlayerJoinedMap);
        mSocket.off("user left", onUserLeft);
        mSocket.off("other user finished", onOtherUserFinished);

        mSocket.off(Socket.EVENT_RECONNECT, onReconnect);
        mSocket.off(Socket.EVENT_RECONNECTING, onReconnecting);
        mSocket.off(Socket.EVENT_RECONNECT_FAILED, onReconnectionFailed);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);

    }//End onDestroy

    //Sets up map once its ready
    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            LatLng latlng = getPhonePosition();
            //Dramatic zoom entrance
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, ZOOM_ON_ENTRY));

            mCourseManager = new CourseManager(getActivity(), mMap, mTimerDisplay, FragmentCourse.this);
            mCourseManager.drawMap(mCourseLocations);

            User user = new User();
            user.Username = mActivity.getUsername();
            user.Location = latlng;
            user.RoomName = mActivity.getRoomName();
            createUser(user);

            //Tells other users that this user has joined the map
            mSocket.emit("map joined", mJsonParser.parseUser(user));
        }
    };//End onMapReadCallBack

    @Deprecated
    private void moveCamera(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private String getProvider() {
        Criteria defaultCriteria = new Criteria();
        return mLocationManager.getBestProvider(defaultCriteria, false);
    }

    @Nullable
    private LatLng getPhonePosition()  {
        Location currentLocation = mLocationManager.getLastKnownLocation(getProvider());
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        return latLng;
    }

    //Updates user positions on the map
    private void updateUser(User data) {

        User user = findUser(data.Username);
        if(user != null) {
            user.Location = data.Location;
            user.Marker.setPosition(data.Location);
            user.Circle.setCenter(data.Location);
        }
        else {
            // if user is not recognised create one
            createUser(data);
        }
    }//End updateUser

    //PRE: Requires User to have a username and location before use
    //Saves user information received from another user and adds its presence to the map
    private void createUser(User data) {
        data.Color = getRandomColor();

        Circle circle = generateCircle(data);
        Marker marker = generateMarker(data);

        User newUser = new User();
        newUser.Circle = circle;
        newUser.Marker = marker;
        newUser.Location = data.Location;
        newUser.Username = data.Username;

        //Removes old version if some still have not disconnected yet
        removeUser(newUser.Username);
        mUserList.add(newUser);

    }//End createUser
    //POST: User's presence (marker, circle) is added to map, and information added to list of users (location, username)

    //Creates and adds a marker to the map
    private Marker generateMarker(User data) {
        IconGenerator iconGenerator = new IconGenerator(mActivity);
        Bitmap bitmap = iconGenerator.makeIcon(data.Username);

        // Get back the mutable marker so it can be removed later
        return mMap.addMarker(new MarkerOptions().position(data.Location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
    }

    //Creates and adds a circle to the map
    private Circle generateCircle(User data) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(data.Location);
        circleOptions.fillColor(data.Color);
        circleOptions.radius(CIRCLE_DIAMETER); // In meters

        // Get back the mutable Circle
        return mMap.addCircle(circleOptions);
    }

    private int getRandomColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        return color;
    }

    //Retrieve user if it exists
    @Nullable
    private User findUser(String username) {
        User user = null;

        for(User u : mUserList) {
            if(u.Username.equals(username)) {
                user = u;
            }
        }

        return user;
    }//End findUser

    //Removes marker and circle identifying a user from the map
    private void removeUser(String username) {

        // Clean user list of all entities with the name, reconnection some times clutters the list with not yet disconnected sockets
        for(int i = mUserList.size() - 1; i >= 0 ; i--) {
            User u = mUserList.get(i);
            if(u.Username.equals(username)) {
                u.Marker.remove();
                u.Circle.remove();
                mUserList.remove(i);
            }
        }

    }//End removeUser

    //LOCATION LISTENER
    //Listens for changes in the devices position
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            User data = new User();
            data.Username = mActivity.getUsername();
            data.Location = getPhonePosition();

            mCourseManager.locationChanged(data.Location);

            updateUser(data);
            mSocket.emit("position change", mJsonParser.parseUser(data));
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
    };//End locationListener

    //SERVER LISTENERS

    //Receives positional data from other users when they move
    private Emitter.Listener onPositionedChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    User user = mJsonParser.buildUser(data);
                    updateUser(user);
                }
            });
        }
    };//End onPositionChanged

    //Handles a new player joining the map
    private Emitter.Listener onPlayerJoinedMap = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    User user = mJsonParser.buildUser(data);

                    if(user != null) {
                        createUser(user);
                    }

                }
            });
        }
    };//End onPlayerJoinedMap

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;

                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }

                    removeUser(username);
                }
            });
        }
    };

    private Emitter.Listener onOtherUserFinished = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String obj = (String) args[0];
                    mCourseManager.addFinishedPlayer(obj);

                }
            });
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO ?add user back to the map when disconnected
                    Toast.makeText(getActivity(), "Reconnected", Toast.LENGTH_LONG).show();

                    User user = new User();
                    user.Location = getPhonePosition();
                    user.Username = mActivity.getUsername();
                    user.RoomName = mActivity.getRoomName();

                    mSocket.emit("log", user.Username + " Reconnected");

                    updateUser(user);
                    mSocket.emit("add user", user.Username);
                    mSocket.emit("join room", user.RoomName);
                    mSocket.emit("map joined", mJsonParser.parseUser(user));
                }
            });
        }
    };

    private Emitter.Listener onReconnecting = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String x = args[0].toString();
                    Toast.makeText(mActivity, "Attempting to reconnect: #" + x, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onReconnectionFailed = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "Reconnection failed, Disconnected", Toast.LENGTH_LONG).show();
                    removeUser(mActivity.getUsername());
                    mSocket.disconnect();
                    mActivity.finish();
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSocket.emit("user left", mActivity.getUsername());
                    removeUser(mActivity.getUsername());
                }
            });
        }
    };

    View.OnClickListener onViewResultsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ArrayList<String> finishedPlayers = mCourseManager.getFinishedPlayers();

            Bundle b = new Bundle();
            b.putStringArrayList("currently finished users", finishedPlayers);

            Fragment frag = new FragmentResultScreen();
            frag.setArguments(b);
            mActivity.startFragment(frag);
        }
    };

    @Override
    public void onFishedCourse(ArrayList<String> times) {
        JsonParser jsonParser = new JsonParser();
        String s = jsonParser.buildTime(mActivity.getUsername(), times);

        mCourseManager.addFinishedPlayer(s);
        mSocket.emit("finished", s);

        mBtnResults.setVisibility(View.VISIBLE);
        mBtnResults.setEnabled(true);
        mBtnResults.setOnClickListener(onViewResultsListener);
    }
}
 // geo fix 170.503942 -45.873855 #1 octagon
 // geo fix 170.501635 -45.876376 #2 dowling street
//  geo fix 170.551192 -45.837739

// geo fix 170.502606 -45.872709 #1
// geo fix 170.501716 -45.874767 #2 in octogon tri course
// geo fix 170.505535 -45.874636 #3 in octogon tri course



