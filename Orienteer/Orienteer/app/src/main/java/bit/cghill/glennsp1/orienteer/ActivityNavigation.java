package bit.cghill.glennsp1.orienteer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import bit.cghill.glennsp1.orienteer.CourseRunning.DialogRoomName;
import bit.cghill.glennsp1.orienteer.MenuScreens.FragmentCreateCourse;
import bit.cghill.glennsp1.orienteer.MenuScreens.MyCourses;


public class ActivityNavigation extends AppCompatActivity implements FragmentCreateCourse.OnResultListener{

    private enum sFragmentType { CREATE_COURSE, LIST_COURSE, JOIN_COURSE, RUN_COURSE}

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_layout);
        init();
    }//End onCreate

    private void init() {
        configureActionBar();
        configureNavDrawer();

        startFragment(new MyCourses(), "my courses");
    }//End init

    //Enables icon button on the action bar
    private void configureActionBar() {
        ActionBar ab = getSupportActionBar();

        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }

    }//End configureActionBar

    //Provides handles for action bar drawer icon
    private void configureNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_nav_open, R.string.drawer_nav_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        configureDrawerListItems();
    }//End configureNavDrawer

    //Handles displaying List of nav items in the drawer
    private void configureDrawerListItems() {

        String[] nav_items = getResources().getStringArray(R.array.fragment_list);
        ListView nav_list = (ListView) findViewById(R.id.left_drawer);
        nav_list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nav_items));

        nav_list.setOnItemClickListener(onNavItemCLick);
    }

    // Adds fragment into view
    public void startFragment(Fragment fragment, String id) {
        if(fragment != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.contentFrame, fragment, id);
            ft.commit();
        }
    }//End startFragment


    //Responds to nav drawer list item input
    AdapterView.OnItemClickListener onNavItemCLick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Fragment fragment = null;


            sFragmentType fragmentType = sFragmentType.values()[i];

            switch (fragmentType) {
                case CREATE_COURSE:
                    FragmentManager fm = getFragmentManager();
                    Fragment frag = fm.findFragmentByTag("0");

                    if(frag != null) {
                        mDrawerLayout.closeDrawers();
                        return;
                    }else {
                        fragment = new FragmentCreateCourse();
                    }
                    break;
                case LIST_COURSE:
                    fragment = new MyCourses();
                    break;
                case JOIN_COURSE:
                    DialogRoomName dialogRoomName = new DialogRoomName();
                    Bundle b = new Bundle();
                    b.putBoolean("is host", false);
                    dialogRoomName.setArguments(b);

                    dialogRoomName.show(getFragmentManager(), "room name dialog");
                    return;
                case RUN_COURSE:
                    Intent runCourse = new Intent(getBaseContext(), RunCourseActivity.class);
                    startActivity(runCourse);
                    break;
                default:
                    Log.v("NAV", "Nav item on click switch defaulted");
                    break;
            }

            mDrawerLayout.closeDrawers();
            String str = Integer.toString(i);
            startFragment(fragment, str);
        }
    };//End onNavItemClick

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_navigation, menu);
        return true;
    }


    //TODO tidy up method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Handles when a new course has been created and the fragment is ready to be changed
    @Override
    public void onResult() {
        startFragment(new MyCourses(), "my courses");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }
}
