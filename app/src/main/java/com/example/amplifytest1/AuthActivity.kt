package com.example.amplifytest1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.*


class AuthActivity: AppCompatActivity() {
    private val TAG = AuthActivity::class.java.getSimpleName()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        AWSMobileClient.getInstance()
            .initialize(applicationContext, object: Callback<UserStateDetails> {
                override fun onResult(userStateDetails: UserStateDetails) {
                    when (userStateDetails.userState) {
                        UserState.SIGNED_IN -> {
                            val intent = Intent(this@AuthActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                        UserState.SIGNED_OUT -> {
                            showSignIn()
                        }
                        else -> {
                            AWSMobileClient.getInstance().signOut()
                            showSignIn()
                        }
                    }
                }

                override fun onError(e: Exception) {
                    Log.e(TAG, e.toString())
                }
            })
    }

    private fun showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
                SignInUIOptions.builder().nextActivity(MainActivity::class.java).build())
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}