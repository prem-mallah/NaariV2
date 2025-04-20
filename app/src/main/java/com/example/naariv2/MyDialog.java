package com.example.naariv2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;

import com.example.naariv2.Contacts.ContactModel;
import com.example.naariv2.Contacts.DbHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.example.naariv2.Contacts.ContactModel;
import com.example.naariv2.Contacts.DbHelper;

import java.util.List;

public class MyDialog extends AppCompatDialogFragment {
    public final static String TAG = MyDialog.class.getName();
    private final static long TIMEOUT = 20L;

    private long mCountDown = TIMEOUT;
    private final Handler mHandler = new Handler();
    // runs on main thread and decreases countdown by 1 second
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            AlertDialog dialog = (AlertDialog) requireDialog();
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setText(getString(R.string.dialog_ok_button, mCountDown));
            if (--mCountDown <= 0) {

                vibrate();


                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

                // use the PRIORITY_BALANCED_POWER_ACCURACY
                // so that the service doesn't use unnecessary power via GPS
                // it will only use GPS at this very moment
                if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // check if updating location is null or not
                        // here for both cases we will
                        // here we create different text messages
                        if (location != null) {

                            // get the SMSManager
                            SmsManager smsManager = SmsManager.getDefault();

                            // get the list of all the contacts in Database
                            DbHelper db = new DbHelper(getContext());
                            List<ContactModel> list = db.getAllContacts();

                            // send SMS to each contact
                            for (ContactModel c : list) {
                                String message = "Hey, " + c.getName() + " I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                            }
                        } else {
                            String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location.";
                            SmsManager smsManager = SmsManager.getDefault();
                            DbHelper db = new DbHelper(getContext().getApplicationContext());
                            List<ContactModel> list = db.getAllContacts();
                            for (ContactModel c : list) {
                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Check: ", "OnFailure");
                        String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location.";
                        SmsManager smsManager = SmsManager.getDefault();
                        DbHelper db = new DbHelper(getContext());
                        List<ContactModel> list = db.getAllContacts();
                        for (ContactModel c : list) {
                            smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                        }
                    }
                });

                Toast.makeText(getContext(), "Yes", Toast.LENGTH_SHORT).show();

//                dismiss();

            } else {
                mHandler.postDelayed(this, 1000L);

            }
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_title)
//                .setPositiveButton(R.string.dialog_cancel_button, (dialogInterface, i) -> Log.d(TAG, "Ok clicked"))
//                .setNegativeButton(getString(R.string.dialog_ok_button, mCountDown), null)
//                .create();


                .setPositiveButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), "cancel", Toast.LENGTH_SHORT).show();
                        dismiss();


                    }
                })


                .setNegativeButton(getString(R.string.dialog_ok_button, mCountDown), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), "Yes", Toast.LENGTH_SHORT).show();

                    }
                })

                .create();



    }

    @Override
    public void onStart() {
        super.onStart();
        mRunnable.run();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mRunnable);
    }

    public void vibrate() {

        final Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibEff;

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
            vibrator.vibrate(vibEff);
        } else {
            vibrator.vibrate(1000);
        }


    }



}
