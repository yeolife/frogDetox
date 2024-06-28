package com.ssafy.frogdetox.ui

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
import com.ssafy.frogdetox.common.LocalAPIKey
import com.ssafy.frogdetox.data.local.SharedPreferencesManager
import com.ssafy.frogdetox.data.local.SharedPreferencesManager.getUId
import com.ssafy.frogdetox.data.local.SharedPreferencesManager.getUserName
import com.ssafy.frogdetox.data.local.SharedPreferencesManager.putUId
import com.ssafy.frogdetox.data.local.SharedPreferencesManager.putUserName
import com.ssafy.frogdetox.data.local.SharedPreferencesManager.putUserProfile
import com.ssafy.frogdetox.databinding.ActivityLoginBinding

private const val TAG = "LoginActivity_싸피"
class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding

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
        Log.d(TAG, "onCreate: ")
        SharedPreferencesManager.init(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSignIn.setOnClickListener { signIn() }

        val webClientId = LocalAPIKey.getSecretKey(this, "default_web_client_id")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        // Intent에서 "state" 값 검사
        val state = intent.getIntExtra("state", 0) // 기본값은 0
        if (state == 1) {
            clearAuthentication()
        }
        else if(state ==2){
            clearAll()
        }
    }
    private fun clearAll(){
        // Google 로그아웃
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // 로그아웃 후 UI 업데이트
            Log.d(TAG, "clearAll: ")
        }
        // SharedPreferences 내용 삭제
        SharedPreferencesManager.clearPreferences()

        //탈퇴 처리하고 logout
        val user = auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 사용자 삭제 성공
                    // 추가적인 작업이 필요하면 여기에 작성
                    Log.d(TAG, "clearAll: 성공")
                    Toast.makeText(this, "회원 탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 사용자 삭제 실패
                    Log.e(TAG, "회원 탈퇴 실패: ${task.exception}")
                }
            }
        // Firebase 로그아웃
        auth.signOut()
    }
    private fun clearAuthentication() {
        // Firebase 로그아웃
        auth.signOut()

        // Google 로그아웃
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // 로그아웃 후 UI 업데이트
            Log.d(TAG, "clearAuthentication: !@@!@#")
        }

        // SharedPreferences 내용 삭제
        SharedPreferencesManager.clearPreferences()

        // 필요한 경우 사용자에게 로그아웃 되었음을 알리기
        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
    }
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")

        updateUI()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "firebaseAuthWithGoogle: ")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    auth.currentUser?.let {
                        putUId(it.uid)
                        putUserName(it.displayName ?: "")
                        putUserProfile(it.photoUrl.toString())
                    }

                    updateUI()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun updateUI() {
        // shared에 id가 유효하면
        if(!getUId().isNullOrBlank()) {
            val intent2 = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "환영합니다, ${getUserName()}님", Toast.LENGTH_SHORT).show()
            startActivity(intent2)
            finish()
        }
    }

    private fun signIn() {
        Log.d(TAG, "signIn: ")
        val signInIntent = googleSignInClient.signInIntent
        activityLauncher.launch(signInIntent)
    }
}