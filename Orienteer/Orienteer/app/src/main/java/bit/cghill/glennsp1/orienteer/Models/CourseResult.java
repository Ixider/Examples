package bit.cghill.glennsp1.orienteer.Models;

import java.util.ArrayList;

public class CourseResult {

    private double[] times;
    private double totalTimeTaken;

    private ArrayList<String>  mDeviceTimes;
    private ArrayList<String> mFinishedPlayersTimes;

    public CourseResult(int numTimes){
        times = new double[numTimes];

        mDeviceTimes =  new ArrayList<>();
        mFinishedPlayersTimes = new ArrayList<>();

        totalTimeTaken = 0;
    }

    public void addFInishedPlayersTime(String time) {
        mFinishedPlayersTimes.add(time);
    }
    public ArrayList<String> getFinishedPlayers() { return mFinishedPlayersTimes; }

    public void addPlayerTime(String time) {
        mDeviceTimes.add(time);
    }

    public ArrayList<String> getDeviceTimes() { return mDeviceTimes; }

    public void setTime(int pointIndex, double time){
        times[pointIndex] = time;
    }

    public double[] getTimes(){
        return times;
    }

    public void setTimeTaken(double timeTaken){
        totalTimeTaken = timeTaken;
    }

    public double getTimeTaken(){
        return totalTimeTaken;
    }
}
