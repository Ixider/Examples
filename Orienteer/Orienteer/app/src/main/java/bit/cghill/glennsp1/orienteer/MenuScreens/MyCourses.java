package bit.cghill.glennsp1.orienteer.MenuScreens;

import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import android.view.View.OnClickListener;
import bit.cghill.glennsp1.orienteer.CourseRunning.DialogRoomName;
import bit.cghill.glennsp1.orienteer.CourseRunning.JsonParser;
import bit.cghill.glennsp1.orienteer.Models.Course;
import bit.cghill.glennsp1.orienteer.R;
import bit.cghill.glennsp1.orienteer.RunCourseActivity;
import bit.cghill.glennsp1.orienteer.Storage.CourseDBManager;

public class MyCourses extends Fragment {

    ListView courseList;
    ArrayAdapter<String> courseListAdapter;
    ArrayList<String> courseStrings;
    CourseDBManager dbManager;
    ArrayList<Course> allCourses;
    Button btnRemove;
    Button btnHost;
    Button btnRunOffline;
    String clickedCourseName;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_my_courses, null);
        init(v);
        return v;
    }

    public void init(View v){
        dbManager = new CourseDBManager(getActivity());
        courseStrings = new ArrayList<String>();
        courseList = (ListView)v.findViewById(R.id.courseList);
        courseList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        courseListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, courseStrings);
        courseList.setAdapter(courseListAdapter);
        courseList.setOnItemClickListener(new onCourseClicked());

        btnRunOffline = (Button)v.findViewById(R.id.btnRunOffline);
        btnRunOffline.setOnClickListener(new runOffline());

        btnRemove = (Button)v.findViewById(R.id.btnRemoveCourse);
        btnHost = (Button)v.findViewById(R.id.btnHost);

        btnRemove.setOnClickListener(new removeCourse());
        btnHost.setOnClickListener(new hostCourse());


        btnRunOffline.setEnabled(false);
        btnHost.setEnabled(false);
        btnRemove.setEnabled(false);
        populateList();
    }

    public void populateList(){
        allCourses = dbManager.getAllCourses();
        courseListAdapter.clear();
        courseStrings.clear();

        //If no courses are present prompt user to create one
        if(allCourses.size() == 0)
        {
            courseListAdapter.add("No current courses. Try create one");
        }
        else //Display courses
        {
            for (Course curr : allCourses) {
                String courseName = curr.getName();
                courseListAdapter.add(courseName);
            }
        }

        courseListAdapter.notifyDataSetChanged();
    }

    public class removeCourse implements OnClickListener{
        @Override
        public void onClick(View view) {
            dbManager.removeCourse(clickedCourseName);
            populateList();
            btnRemove.setEnabled(false);
            btnHost.setEnabled(false);
            btnRunOffline.setEnabled(false);
        }
    }

    public class hostCourse implements OnClickListener{
        @Override
        public void onClick(View view) {
            DialogRoomName dialogRoomName = new DialogRoomName();

            Bundle b = new Bundle();
            b.putBoolean("is host", true);

            Course c = findCourse(clickedCourseName);
            JsonParser parser = new JsonParser();
            String course = parser.parseCourse(c.getLocations()).toString();
            b.putString("course", course);
            dialogRoomName.setArguments(b);

            dialogRoomName.show(getFragmentManager(), "room name dialog");

        }
    }

    public class runOffline implements OnClickListener{

        @Override
        public void onClick(View v) {
            Intent runCourseIntent = new Intent(getActivity(), RunCourseActivity.class);
            runCourseIntent.putExtra("courseName", clickedCourseName);

            startActivity(runCourseIntent);
        }
    }

    public class onCourseClicked implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            String courseName = (String)courseList.getItemAtPosition(position);

            if(!courseName.equals("No current courses. Try create one"))
            {
                clickedCourseName = courseName;

                btnHost.setEnabled(true);
                btnRemove.setEnabled(true);
                btnRunOffline.setEnabled(true);
            }
        }
    }

    public Course findCourse(String name){
        Course clickedCourse = null;

        for(int i = 0; i < allCourses.size(); i++){
            Course currCouse = allCourses.get(i);
            String currName = currCouse.getName();

            if(currName.equals(name)) {
                clickedCourse = currCouse;
            }
        }

        return clickedCourse;
    }
}
