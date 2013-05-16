package com.janrain.capturestandardregistrationflow;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
 
	private ResponseHandler mResponseHandler = new ResponseHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        final Button btnEditProfile = (Button) findViewById(R.id.btnEditProfile);

        btnSignIn.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);

		final CaptureStandardRegistrationFlow appState = (CaptureStandardRegistrationFlow) getApplication();
		final EventSubject eventSubject = appState.getEventSubject();
        eventSubject.addObserver(mResponseHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {

        case R.id.btnSignIn:
            intent = new Intent(this, SignInActivity.class);
            break;

        case R.id.btnEditProfile:
            intent = new Intent(this, EditProfileActivity.class);
            break;

        default:
            break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}

