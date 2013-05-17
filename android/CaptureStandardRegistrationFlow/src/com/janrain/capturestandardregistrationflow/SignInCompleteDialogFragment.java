/**
 * 
 */
package com.janrain.capturestandardregistrationflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @author mitch
 * 
 */
public class SignInCompleteDialogFragment extends DialogFragment {

    public interface NoticeDialogListener {
        public void onDialogDismiss(DialogFragment dialogFragment);
    }

    private NoticeDialogListener mListener;
    private final String mMessage;

    /**
     * 
     */
    public SignInCompleteDialogFragment(final String message) {
        mMessage = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // super.onCreateDialog(savedInstanceState);

        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setTitle(R.string.signin_complete)
                .setNeutralButton(R.string.dismiss,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // dismiss the dialog.
                            }
                        }).setMessage(mMessage);
        final Dialog dialog = builder.create();
        // Create the AlertDialog object and return it
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the
            // host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.DialogFragment#onDismiss(android.content.
     * DialogInterface)
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onDialogDismiss(this);
    }
}
