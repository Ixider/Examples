package bit.cghill.glennsp1.orienteer.PrefsManager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by S. Glenn on 19-May-15.
 */
public class PrefsManager {

    private String sPrefsFileName = "filename";
    private String sUsername = "username";

    private Context mContext;

    public PrefsManager(Context context) {
        mContext = context;
    }


    public String getUsername() {
        SharedPreferences settings = mContext.getSharedPreferences(sPrefsFileName, 0);

        return settings.getString(sUsername, "");
    }

    public void Saveusername(String username) {
        SharedPreferences settings = mContext.getSharedPreferences(sPrefsFileName, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(sUsername, username);

        editor.apply();
    }
}
