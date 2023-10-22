package com.cpen321.tunematch;

import static com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.cpen321.tunematch.MainActivity;
import com.cpen321.tunematch.R;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class LoginActivity extends AppCompatActivity {
    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "https://localhost:3000";
    private static final String TAG = "LoginActivity";
    private static final String CLIENT_ID = "0dcb406f508a4845b32a1342a91a71af";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder("0dcb406f508a4845b32a1342a91a71af", AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            Log.d(TAG, "here is the type of the response: " + response.getType());

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
//                    print the token
                    System.out.println(response.getAccessToken());
                    Log.d(TAG, "onActivityResult: " + response.getAccessToken());
                    Log.d(TAG, "it is supposed to work bruh");
                    // Send to main activity
                    String auth_token = response.getAccessToken();
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("auth_token", auth_token);
                    startActivity(intent);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.d(TAG, "onActivityResult: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
}
