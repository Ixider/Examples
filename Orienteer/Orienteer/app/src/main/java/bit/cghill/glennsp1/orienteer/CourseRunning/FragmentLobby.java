package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.EmbossMaskFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bit.cghill.glennsp1.orienteer.R;

/**
 * Created by S. Glenn on 19-May-15.
 */
public class FragmentLobby extends Fragment {

    private Socket mSocket;
    private ArrayAdapter mAdapter;
    private ActivityRunCourse mActivity;
    private String mCourse;
    private List<String> mUsernames;
    private Button sStartButton;

    public StartClickListener mStartClickListener;
    public interface StartClickListener { void onStartClickCallback(String course); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lobby_layout, container, false);

        init(v);

        return v;
    }

    private void init(View v) {
        mActivity = (ActivityRunCourse)getActivity();

        mUsernames = new ArrayList<>();
        mUsernames.add(mActivity.getUsername());

        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mUsernames);
        ListView playerList = (ListView)v.findViewById(R.id.playerList);
        playerList.setAdapter(mAdapter);

        sStartButton = (Button)v.findViewById(R.id.startButton);
        sStartButton.setOnClickListener(onStartClickListener);
        sStartButton.setEnabled(mActivity.getIsHost());
        mSocket = mActivity.getSocket();

        //Provides Handles for events happening within the lobby
        mSocket.on("user joined lobby", onUserJoinLobby);
        mSocket.on("login", onLogin);
        mSocket.on("user left", onUserLeft);
        mSocket.on("start", onStart);

        mSocket.on(Socket.EVENT_RECONNECT, onReconnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);

        // emit to others in the lobby the user has joined
        mSocket.emit("add user", mActivity.getUsername());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.off("user joined lobby", onUserJoinLobby);
        mSocket.off("login", onLogin);
        mSocket.off("user left", onUserLeft);
        mSocket.off("start", onStart);

        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_RECONNECT, onReconnect);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mStartClickListener = (StartClickListener)activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnStartClickListener");
        }
    }

    private void validateUserList(JSONArray data) throws JSONException {

        for (int i = 0; i < data.length(); i++) {
            String username = (String) data.get(i);
            if (!mUsernames.contains(username)) {
                mUsernames.add(username);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    View.OnClickListener onStartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSocket.emit("start course");
            mStartClickListener.onStartClickCallback(mCourse);
        }
    };

    //Adds newly joined user to the list of users displayed on screen
    private Emitter.Listener onUserJoinLobby = new Emitter.Listener() {
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

                    mUsernames.add(username);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    //Receives list of already connected users
    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray userList = (JSONArray) args[0];

                    try {
                        validateUserList(userList);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

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
                    mUsernames.remove(username);
                    mAdapter.notifyDataSetChanged();
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
                    Toast.makeText(mActivity, "Reconnected", Toast.LENGTH_LONG).show();
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
                }
            });
        }
    };

    private Emitter.Listener onStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject obj = (JSONObject) args[0];

                    try {
                        mCourse = obj.getString("course");

                        Toast.makeText(getActivity(), "started", Toast.LENGTH_LONG).show();
                        mStartClickListener.onStartClickCallback(mCourse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
