package com.example.v_sion.main

import android.app.Application
import com.example.v_sion.models.ResultFireStore
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MainApp : Application(), AnkoLogger {

    lateinit var results: ResultFireStore

    // [START declare_auth]
    lateinit var auth: FirebaseAuth
    // [END declare_auth]
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate() {
        super.onCreate()
        results = ResultFireStore(applicationContext)
        info("Firestore started")
    }
}