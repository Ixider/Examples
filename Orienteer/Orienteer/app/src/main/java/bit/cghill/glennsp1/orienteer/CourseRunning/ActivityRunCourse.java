package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.net.URISyntaxException;
import bit.cghill.glennsp1.orienteer.PrefsManager.PrefsManager;
import bit.cghill.glennsp1.orienteer.R;

/**
 * Created by S. Glenn on 14-May-15.
 */

//PRE: Must be supplied with if user is host or not
//TODO system relies on everyone having a unique username, this is not enforced.
public class ActivityRunCourse extends AppCompatActivity implements FragmentLobby.StartClickListener {

    private Socket mSocket;
    private String mCourse;
    private String mUsername;
    private String mRoomName;
    private String mServerAddress;
    private boolean mIsHost;


    public String getUsername() { return mUsername; }
    public Socket getSocket() { return  mSocket; }
    public String getRoomName() { return mRoomName; }
    public boolean getIsHost() { return mIsHost; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_course_layout);

        init();
    }

    private void init() {
        mServerAddress = getResources().getString(R.string.ServerAddress);

        mSocket = createSocket();
        mSocket.connect();

        Bundle b = getIntent().getExtras();
        mRoomName = b.getString("room name");
        mIsHost = b.getBoolean("is host");

        mSocket.emit("join room", mRoomName);

        if(mIsHost) {
            mCourse = b.getString("course");
            mSocket.emit("set course", mCourse);
        }

        loginUser();
    }//End init

    //looks for username in preferences if they haven't set it, ask them to.
    private void loginUser() {
        PrefsManager prefsManager = new PrefsManager(getApplicationContext());
        mUsername = prefsManager.getUsername();
        if(mUsername.equals("")) {
            buildLoginDialog();
        } else {
            Fragment lobbyFragment = new FragmentLobby();
            startFragment(lobbyFragment);
        }
    }//End loginUser

    //Lobby retrieves course and passes it back to pass onto the map
    @Override
    public void onStartClickCallback(String course) {
        Fragment fragment = new FragmentCourse();
        Bundle b = new Bundle();

        if(mIsHost) {
            b.putString("course", mCourse);
        } else {
            b.putString("course", course);
        }

        fragment.setArguments(b);
        startFragment(fragment);
    }//End onStartClickCallBack

    //Disconnects the socket from the server
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    //Creates a new socket to talk to the server
    public Socket createSocket () {
        IO.Options options = new IO.Options();
        options.timeout = 5000;
        options.reconnectionAttempts = 5;

        Socket socket;
        try {
            socket = IO.socket(mServerAddress, options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return socket;
    }//End createSocket

    //Starts a given fragment
    public void startFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.running_fragment_container, fragment);
        ft.commit();
    }//End startFragment

    //Provides the user a input dialog to set a username if not set
    private void buildLoginDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Enter username:");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setHint("enter a unique username");
        alert.setView(input);


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String username = input.getText().toString();
                PrefsManager prefsManager = new PrefsManager(getApplicationContext());
                prefsManager.Saveusername(username);
                loginUser();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });

        alert.show();
    }//End buildLoginDialog
}
