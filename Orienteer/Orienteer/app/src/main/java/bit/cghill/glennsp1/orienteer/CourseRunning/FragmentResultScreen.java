package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;

import bit.cghill.glennsp1.orienteer.R;

public class FragmentResultScreen extends Fragment {

    private Button btnHome;
    private ListView playerResultList;

    private ArrayList<String> playerTimes;
    private ArrayAdapter<String> mArrayAdapter;
    private Socket mSocket;
    private JsonParser mJsonParser;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_screen, container, false);
        init(v);
        return v;
    }

    private void init(View v){
        btnHome = (Button)v.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new returnHome());
        mJsonParser = new JsonParser();

        mSocket = ((ActivityRunCourse)getActivity()).getSocket();
        mSocket.on("other user finished", onOtherUserFinished);
        playerResultList = (ListView)v.findViewById(R.id.playerResultList);

        fillPlayerResults();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.off("other user finished", onOtherUserFinished);
    }

    private void fillPlayerResults(){
        Bundle b = getArguments();
        ArrayList<String> data = b.getStringArrayList("currently finished users");


        playerTimes = mJsonParser.unpackTimes(data);

        mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, playerTimes);
        playerResultList.setAdapter(mArrayAdapter);
    }


    //Hanlder to return to main activity
    public class returnHome implements OnClickListener{
        @Override
        public void onClick(View v) {
            getActivity().finish();
        }
    }

    private Emitter.Listener onOtherUserFinished = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String obj = (String) args[0];

                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(obj);

                    playerTimes.addAll(mJsonParser.unpackTimes(temp));
                    mArrayAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}
