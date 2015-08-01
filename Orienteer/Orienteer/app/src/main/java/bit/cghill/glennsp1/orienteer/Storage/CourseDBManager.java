package bit.cghill.glennsp1.orienteer.Storage;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import bit.cghill.glennsp1.orienteer.Models.Course;

public class CourseDBManager {
    SQLiteDatabase courseDB;
    Context context;

    public CourseDBManager(Context mainContext){
        context = mainContext;
        courseDB = context.openOrCreateDatabase("courseDB", context.MODE_PRIVATE, null);

        String createtblCourse = "CREATE TABLE IF NOT EXISTS tblCourse(courseID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)";
        String createtblPoint = "CREATE TABLE IF NOT EXISTS tblPoint(pointID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "latitude TEXT NOT NULL, longitude TEXT NOT NULL, courseID INTEGER NOT NULL)";

        courseDB.execSQL(createtblCourse);
        courseDB.execSQL(createtblPoint);
        courseDB.close();
    }

    public void insertCourse(Course newCourse){
        courseDB = context.openOrCreateDatabase("courseDB", context.MODE_PRIVATE, null);

        String name = newCourse.getName();

        String insertCourseQuery = "INSERT INTO tblCourse VALUES(null,'" + name + "')";
        courseDB.execSQL(insertCourseQuery);

        //Make query and execute to get all records
        String selectQuery = "SELECT * FROM tblCourse";
        Cursor recordSet = courseDB.rawQuery(selectQuery, null);

        recordSet.moveToLast();

        int courseIDIndex = recordSet.getColumnIndex("courseID");
        int lastCourseID = recordSet.getInt(courseIDIndex);

        ArrayList<LatLng> allPoints = newCourse.getLocations();

        for(int i=0; i < allPoints.size(); i++){
            LatLng curr = allPoints.get(i);
            String lat = String.valueOf(curr.latitude);
            String lng = String.valueOf(curr.longitude);

            String insertNewPointQuery = "INSERT INTO tblPoint VALUES(null,'" + lat + "','" + lng + "','" + lastCourseID  + "')";

            courseDB.execSQL(insertNewPointQuery);
        }

        courseDB.close();
    }

    public ArrayList<Course> getAllCourses(){
        courseDB = context.openOrCreateDatabase("courseDB", context.MODE_PRIVATE, null);

        ArrayList<Course> allCourses = new ArrayList<Course>();

        String selectQuery = "SELECT * FROM tblCourse";
        Cursor courseRecordSet = courseDB.rawQuery(selectQuery, null);

        //Set up to loop
        courseRecordSet.moveToFirst();

        int courseRecordCount = courseRecordSet.getCount();

        selectQuery = "SELECT * from tblPoint";
        Cursor pointRecordSet = courseDB.rawQuery(selectQuery, null);
        courseRecordSet.moveToFirst();

        int pointRecordCount = pointRecordSet.getCount();

        for(int i=0; i < courseRecordCount; i++)
        {
            ArrayList<LatLng> currCoursesPoints = new ArrayList<LatLng>();

            int idIndex = courseRecordSet.getColumnIndex("courseID");
            int courseID = courseRecordSet.getInt(idIndex);

            int nameIndex = courseRecordSet.getColumnIndex("name");
            String name = courseRecordSet.getString(nameIndex);

            pointRecordSet.moveToFirst();

            for(int j=0; j < pointRecordCount; j++)
            {
                int courseIDIndex = pointRecordSet.getColumnIndex("courseID");
                int foreignKey = pointRecordSet.getInt(courseIDIndex);

                if(foreignKey == courseID)
                {
                    int latIndex = pointRecordSet.getColumnIndex("latitude");
                    String lat = pointRecordSet.getString(latIndex);

                    int lngIndex = pointRecordSet.getColumnIndex("longitude");
                    String lng = pointRecordSet.getString(lngIndex);

                    LatLng newPoint = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

                    currCoursesPoints.add(newPoint);
                }

                pointRecordSet.moveToNext();
            }

            Course currCourse = new Course(name, currCoursesPoints);
            allCourses.add(currCourse);
            courseRecordSet.moveToNext();
        }


        courseDB.close();
        return allCourses;
    }

    public void removeCourse(String courseNameToRemove){
        courseDB = context.openOrCreateDatabase("courseDB", context.MODE_PRIVATE, null);

        String selectQuery = "SELECT * FROM tblCourse";
        Cursor courseRecordSet = courseDB.rawQuery(selectQuery, null);

        //Set up to loop
        courseRecordSet.moveToFirst();

        int courseRecordCount = courseRecordSet.getCount();

        selectQuery = "SELECT * from tblPoint";
        Cursor pointRecordSet = courseDB.rawQuery(selectQuery, null);
        courseRecordSet.moveToFirst();

        int pointRecordCount = pointRecordSet.getCount();

        for(int i=0; i < courseRecordCount; i++)
        {
            int idIndex = courseRecordSet.getColumnIndex("courseID");
            int courseID = courseRecordSet.getInt(idIndex);

            int nameIndex = courseRecordSet.getColumnIndex("name");
            String name = courseRecordSet.getString(nameIndex);

            pointRecordSet.moveToFirst();

            if(courseNameToRemove.equals(name))
            {
                for (int j = 0; j < pointRecordCount; j++) {
                    int courseIDIndex = pointRecordSet.getColumnIndex("courseID");
                    int foreignKey = pointRecordSet.getInt(courseIDIndex);

                    //Points belong to course to remove
                    if (foreignKey == courseID) {
                        int pointIDIndex = pointRecordSet.getColumnIndex("pointID");
                        int pointID = pointRecordSet.getInt(pointIDIndex);

                        String deletePointQuery = "DELETE FROM tblPoint WHERE pointID=" + pointID;

                        courseDB.execSQL(deletePointQuery);
                    }
                    pointRecordSet.moveToNext();
                }
                courseRecordSet.moveToNext();
                String deleteCourseQuery = "DELETE FROM tblCourse WHERE courseID=" + courseID;
                courseDB.execSQL(deleteCourseQuery);
            }
            else
            {
                courseRecordSet.moveToNext();
            }
        }
        courseDB.close();
    }
}
