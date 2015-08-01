package bit.cghill.glennsp1.orienteer.Models;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;


public class Course {
    private ArrayList<LatLng> locations;
    private String name;

    //Make a blank course
    public Course(){
        name = "No Name";
        locations = new ArrayList<LatLng>();
    }

    //Make course with existing locations
    public Course(String startName, ArrayList<LatLng> startLocations){
        name = startName;
        locations = startLocations;
    }

    //Add point into course
    public void addPoint(LatLng newPoint){
        locations.add(newPoint);
    }

    //returns list of latlngs
    public ArrayList<LatLng> getLocations(){
        return locations;
    }

    public String getName(){
        return name;
    }

    //Remove a point from the list
    public void removePoint(int index){
        locations.remove(index);
    }

    //Returns number of items in list
    public int getSize(){
        return locations.size();
    }

    public void setName(String newName){
        name = newName;
    }
}
