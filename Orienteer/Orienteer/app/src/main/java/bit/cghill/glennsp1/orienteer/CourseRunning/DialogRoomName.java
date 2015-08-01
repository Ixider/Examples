package bit.cghill.glennsp1.orienteer.CourseRunning;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by S. Glenn on 07-Jun-15.
 */
public class DialogRoomName extends DialogFragment {

    EditText roomNameInput;
    EditText passwordInput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getActivity());

        alert.setTitle("Enter Room Details:");

        Context context = getActivity();
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Set an EditText view to get user input
        roomNameInput = new EditText(context);
        roomNameInput.setHint("enter a room name");

        passwordInput = new EditText(context);
        passwordInput.setHint("enter a pass phrase");

        container.addView(roomNameInput);
        container.addView(passwordInput);

        alert.setView(container);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String roomName = roomNameInput.getText().toString();
                String password = passwordInput.getText().toString();

                if(validateInput(roomName, password)) {
                    Bundle b = new Bundle();

                    b.putString("room name", hashString(roomName + password));
                    b.putBoolean("is host", getArguments().getBoolean("is host"));
                    b.putString("course", getArguments().getString("course"));
                    Intent intent = new Intent(getActivity(), ActivityRunCourse.class);
                    intent.putExtras(b);

                    startActivity(intent);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        return alert.create();
    }

    //Checks user room input and password for length and illegal characters
    private boolean validateInput(String roomName, String password) {

        boolean valid = true;
        String errors = "";

        if(roomName.contains(" ") ) {
            valid = false;
            errors += "Room name cannot contain spaces \n";
        }
        if (password.contains(" ")) {
            valid = false;
            errors += "Password name cannot contain spaces \n";
        }
        if(roomName.length() < 3) {
            valid = false;
            errors += "Room name must be at least 3 characters \n";
        }
        if(roomName.length() < 3) {
            errors += "Password must be at least 3 characters \n";
        }

        if(!valid)
            toastError(errors);

        return valid;
    }//End validateInput

    //Provides user feedback about invalid room name and password inputs
    private void toastError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }//End toastError

    //Hashes the room name and password to create a unique room name
    public String hashString(String str)  {
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes());
            byte[] messageDigest = digest.digest();

            StringBuilder hexString = new StringBuilder();

            for (byte b : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }//End hashString
}
