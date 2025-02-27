package com.vaagdevi.newsnevents;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class MainActivity<gso, mGoogleSignInClient> extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    String currentId;

    int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseref;


    EditText Emailid;
    EditText Passid;
    Button login;
    Button google;
    Button facebook;
    ImageButton register;
    TextView forgotpassid;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Emailid = (EditText) findViewById(R.id.ETemailId);
        Passid = (EditText) findViewById(R.id.ETpassId);
        login = (Button) findViewById(R.id.BTNlogin);
        register = (ImageButton) findViewById(R.id.IMGBTNregister);
        google = (Button) findViewById(R.id.BTNgoogle);
        forgotpassid = (TextView) findViewById(R.id.TVForgot);

        mAuth = FirebaseAuth.getInstance();
        databaseref = FirebaseDatabase.getInstance().getReference("Login Users");
        progressDialog = new ProgressDialog(MainActivity.this);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-2546283744340576~1317058396");

        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });*/

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        prepareAD();

        @SuppressLint("WrongViewCast") final AppCompatCheckBox checkBox = (AppCompatCheckBox) findViewById(R.id.show_hide_password);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    // show password
                    Passid.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // hide password
                    Passid.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setTitle("Validating");
                progressDialog.setMessage("Please wait...");
                checkConnection();

                progressDialog.show();

                String emailid = Emailid.getText().toString();
                final String passid = Passid.getText().toString();

                if (emailid.isEmpty() && passid.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
                } else if (emailid.isEmpty()) {
                    Emailid.setError("Provide Your Email");
                    Emailid.requestFocus();
                } else if (passid.isEmpty()) {
                    Passid.setError("Enter Your Password");
                    Passid.requestFocus();
                } else if (!(emailid.isEmpty() && passid.isEmpty())) {

                    mAuth.signInWithEmailAndPassword(emailid, passid)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if ((task.isSuccessful())) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(MainActivity.this, "Welcome to News n Events", Toast.LENGTH_SHORT).show();
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        startActivity(new Intent(MainActivity.this, Dashboard.class));
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Unable to Login", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, Registration.class));

            }
        });

        forgotpassid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, ForgotPassword.class));

            }
        });

    }


    private void signIn() {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                progressDialog.dismiss();
                final String personName = account.getDisplayName();
                startActivity(new Intent(MainActivity.this, Dashboard.class));
                Toast.makeText(MainActivity.this, "Welcome " + personName + " to News n Events", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_LONG).show();

                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .build();

                            // Build a GoogleSignInClient with the options specified by gso.
                            mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

                            GoogleSignInAccount acct =GoogleSignIn.getLastSignedInAccount(MainActivity.this);
//                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                            if (acct != null) {

                                String personName = acct.getDisplayName();
                                String personEmail = acct.getEmail();
                                String personGivenName = acct.getGivenName();
                                String personFamilyName = acct.getFamilyName();
                                String personId = acct.getId();
                                Uri personPhoto = acct.getPhotoUrl();

                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("email",personEmail);
                                hashMap.put("username",personName);
                                hashMap.put("mobilenumber","");
                                hashMap.put("password","");
                                hashMap.put("rollno","");
                                hashMap.put("year","");
                                hashMap.put("branch","");
                                hashMap.put("college","");
                                hashMap.put("address","");
                                hashMap.put("profileimage","!");

                                FirebaseDatabase.getInstance().getReference(databaseref.getKey())
                                        .child(mAuth.getCurrentUser().getUid())
                                        .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });

                                hashMap.put("email",personEmail);
                                hashMap.put("username",personName);
                                hashMap.put("mobilenumber","");
                                hashMap.put("password","");
                                hashMap.put("rollno","");
                                hashMap.put("year","");
                                hashMap.put("branch","");
                                hashMap.put("college","");
                                hashMap.put("address","");
                                hashMap.put("profileimage","!");

                                FirebaseDatabase.getInstance().getReference(databaseref.getKey())
                                        .child(mAuth.getCurrentUser().getUid())
                                        .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });
                            }
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "you are not able to log in to google", Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }


    public void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (null == activeNetwork) {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null || currentUser != null) {
            //final String personName = account.getDisplayName();
            startActivity(new Intent(MainActivity.this, Dashboard.class));
            Toast.makeText(MainActivity.this, "Welcome  to News n Events", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onStart();
    }

    public void prepareAD() {
        mInterstitialAd = new InterstitialAd(this);
        //Test AD Unit : ca-app-pub-3940256099942544/1033173712
        mInterstitialAd.setAdUnitId("ca-app-pub-2546283744340576/2313078313");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed() {

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();
                }
            });
        } else {
            super.onBackPressed();
        }
    }
}