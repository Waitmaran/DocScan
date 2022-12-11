package com.colin.docscan

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.colin.docscan.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("669840233480-puiorb862b1v2rd2le47disj4ldrra6f.apps.googleusercontent.com")
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        binding.buttonOffline.setOnClickListener {
            val int = Intent(this, MainActivity::class.java)
            int.putExtra("token", "offline")
            startActivity(int)
            finish()
        }

        binding.buttonLoginGoogle.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        loginResultHandler.launch(
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage!!.toString())
                }
        }
    }

    private val loginResultHandler = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        // handle intent result here
        if (result.resultCode == RESULT_OK) Log.d(
            TAG,
            "RESULT_OK."
        )
        if (result.resultCode == RESULT_CANCELED) Log.d(
            TAG,
            "RESULT_CANCELED."
        )
        if (result.resultCode == RESULT_FIRST_USER) Log.d(
            TAG,
            "RESULT_FIRST_USER."
        )
        try {
            val credential =
                oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken

            var user: FirebaseUser? = null
            //val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LOGIN", "signInWithCredential:success")
                        user = auth.currentUser

                        if(user != null) {
                            val int = Intent(this, MainActivity::class.java)
                            int.putExtra("token", user!!.uid)
                            val rootRef = Firebase.database("https://documentscanner-67991-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users/" + user!!.uid)
                            rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (!snapshot.exists()) {
                                        Firebase.database("https://documentscanner-67991-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users").child(user!!.uid).child("Documents")
                                        //rootRef.removeEventListener(this)
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    // Failed, how to handle?
                                }
                            })
                            startActivity(int)
                            finish()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LOGIN", "signInWithCredential:failure", task.exception)
                    }
                }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Log.d(TAG, "One-tap dialog was closed.")
                    // Don't re-prompt the user.
                    //showOneTapUI = false
                }
                CommonStatusCodes.NETWORK_ERROR -> Log.d(
                    TAG,
                    "One-tap encountered a network error."
                )
                else -> Log.d(
                    TAG, "Couldn't get credential from result."
                            + e.localizedMessage
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?): FirebaseUser? {
        var user: FirebaseUser? = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LOGIN", "signInWithCredential:success")
                    user = auth.currentUser!!
                    user!!.email?.let { Log.d("ABOBA", it) }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LOGIN", "signInWithCredential:failure", task.exception)
                }
            }
        return user
    }
}