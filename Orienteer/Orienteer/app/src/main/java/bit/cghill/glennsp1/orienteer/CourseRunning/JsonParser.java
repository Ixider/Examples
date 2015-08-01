package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import bit.cghill.glennsp1.orienteer.Models.User;

/**
 * Created by S. Glenn on 24-May-15.
 */
public class JsonParser {


    // COURSE INFORMATION

    public ArrayList<LatLng> buildCourse(String obj) {
        ArrayList<LatLng> course = new ArrayList<>();

        try {
            JSONObject object = new JSONObject(obj);
            JSONArray data = (JSONArray) object.get("course");

            for(int i = 0; i < data.length(); i++) {
                JSONObject location = (JSONObject) data.get(i);

                double lat = Double.valueOf(location.getString("lat"));
                double lng = Double.valueOf(location.getString("lng"));

                LatLng latLng = new LatLng(lat, lng);
                course.add(latLng);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return course;
    }

    @Nullable
    public JSONObject parseCourse(ArrayList<LatLng> course) {

        JSONObject data = new JSONObject();

        try {
            JSONArray locations = new JSONArray();

            for(LatLng latLng : course) {
                JSONObject location = new JSONObject();
                location.put("lat", latLng.latitude);
                location.put("lng", latLng.longitude);

                locations.put(location);
            }

            data.put("course", locations);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


    // USER INFORMATION
    @Nullable
    public User buildUser(JSONObject obj) {

        User user = null;

        try {
            JSONObject data = (JSONObject) obj.get("data");

            String username = data.getString("username");
            String lat = data.getString("lat");
            String lng = data.getString("lng");

            LatLng latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

            user = new User();
            user.Location = latLng;
            user.Username = username;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    //Creates a information object of the mUserList data for sending to other users
    @Nullable
    public JSONObject parseUser(User user) {

        JSONObject userJson = null;

        try {
            userJson = new JSONObject();

            userJson.put("username", user.Username);
            userJson.put("lat", user.Location.latitude);
            userJson.put("lng", user.Location.longitude);
            userJson.put("room", user.RoomName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userJson;
    }

    public String buildTime(String username, ArrayList<String> str) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);

            JSONArray jsonArray = new JSONArray();
            for(String s : str) {
                jsonArray.put(s);
            }

            jsonObject.put("times", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    public ArrayList<String> unpackTimes(ArrayList<String> times) {

        ArrayList<String> resultsList = new ArrayList<>();

        for(int i = 0; i < times.size(); i++) {

            try {
                JSONObject obj = new JSONObject(times.get(i));

                String username = obj.getString("username");
                resultsList.add(username);

                JSONArray array = obj.getJSONArray("times");
                for(int x = 0; x < array.length(); x++ ) {
                    String s = "Position: " + String.valueOf(x + 1) + " - " + array.get(i).toString();
                    resultsList.add(s);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return resultsList;
    }
}
