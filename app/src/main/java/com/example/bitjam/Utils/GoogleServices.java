package com.example.bitjam.Utils;

import android.content.Context;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * Methods/fields in this class cannot be static, due to memory leaks.
 * Expose all methods with a new instance instead.
 */
public class GoogleServices {
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    /**
     * @return a singleton of {@link GoogleSignInOptions.Builder}
     */
    public GoogleSignInOptions getGso() {
        if (gso == null) {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestId()
                    .build();
        }
        return gso;
    }

    /**
     * @param context The current view's context
     * @param gso     an instance of {@link GoogleSignInOptions.Builder}
     * @return A singleton of {@link GoogleSignInClient}
     */
    public GoogleSignInClient getGsc(Context context, GoogleSignInOptions gso) {
        if (gsc == null) {
            gsc = GoogleSignIn.getClient(context, gso);
        }
        return gsc;
    }
}
