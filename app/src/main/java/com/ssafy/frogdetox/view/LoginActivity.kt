package com.ssafy.frogdetox.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ssafy.frogdetox.R
import com.ssafy.frogdetox.databinding.ActivityLoginBinding
import com.ssafy.frogdetox.common.SharedPreferencesManager
import com.ssafy.frogdetox.common.SharedPreferencesManager.getUId
import com.ssafy.frogdetox.common.SharedPreferencesManager.putUId

private const val TAG = "LoginActivity_싸피"
class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            task.runCatching {
                getResult(ApiException::class.java).idToken
            }.onSuccess { token ->
                token?.let { firebaseAuthWithGoogle(token) }
            }.onFailure { error ->
                Log.w(TAG, "Google sign in failed ${error.message}")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferencesManager.init(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnMain.setOnClickListener { signIn() }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        createNotificationChannel()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent2 = Intent(this, MainActivity::class.java).apply {
                putExtra("url", user.photoUrl.toString())
                putExtra("name", user.displayName)
                putExtra("state",intent.getIntExtra("state",0))
            }
            Log.d(TAG, "updateUI: ${user.uid}")
            putUId(user.uid)
            Log.d(TAG, "updateUI: !!! ${getUId()}")
            Toast.makeText(this, "환영합니다, ${user.displayName}님", Toast.LENGTH_SHORT).show()
            startActivity(intent2)
            finish()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        activityLauncher.launch(signInIntent)
    }

//    private fun initFCM() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) return@OnCompleteListener
//
//            // token log 남기기
//            Log.d(TAG, "token: ${task.result ?: "task.result is null"}")
////            task.result?.let { uploadToken(it) }
//        })
//    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(ID, NAME, importance))
    }

    companion object {
        const val ID = "ssafy_channel"
        const val NAME = "ssafy"
    }
}