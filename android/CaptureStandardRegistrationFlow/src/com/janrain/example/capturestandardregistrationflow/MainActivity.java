/*
 *
 *  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  *  Copyright (c) 2013, Janrain, Inc.
 *  *
 *  *  All rights reserved.
 *  *
 *  *  Redistribution and use in source and binary forms, with or without modification,
 *  *  are permitted provided that the following conditions are met:
 *  *
 *  *  * Redistributions of source code must retain the above copyright notice, this
 *  *    list of conditions and the following disclaimer.
 *  *  * Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation and/or
 *  *    other materials provided with the distribution.
 *  *  * Neither the name of the Janrain, Inc. nor the names of its
 *  *    contributors may be used to endorse or promote products derived from this
 *  *    software without specific prior written permission.
 *  *
 *  *
 *  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 */
package com.janrain.example.capturestandardregistrationflow;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity  {
    private CaptureLoginSuccessResponseHandler mResponseHandler = new CaptureLoginSuccessResponseHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button signInButton = (Button) findViewById(R.id.sign_in_button);
        final Button editProfileButton = (Button) findViewById(R.id.edit_profile_button);

        signInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("url", WebViewActivity.SIGN_IN_URL);
                startActivity(intent);
            }
        });

        editProfileButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("url", WebViewActivity.EDIT_PROFILE_URL);
                startActivity(intent);
            }
        });

        CaptureStandardRegistrationFlowDemo appState = (CaptureStandardRegistrationFlowDemo) getApplication();
        appState.getCaptureLoginSuccessEventSubject().addObserver(mResponseHandler);
    }
}
