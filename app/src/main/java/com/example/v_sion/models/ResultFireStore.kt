package com.example.v_sion.models

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class ResultFireStore(val context: Context): AnkoLogger {

    // Access a Cloud Firestore instance from your Activity
    private val db = FirebaseFirestore.getInstance()

    //for Create & Update (set is used to update, but if no document with date passed in found, create one instead)
    fun addUsage(history: HistoryModel) {

        val date = history.date
        val uid = history.uid
        // Create a new user with a first and last name
        val history = hashMapOf(
            "email" to history.user_email,
            "date" to history.date,
            "time" to history.time,
            "targetAchieved" to history.targetAchieved
        )

        // Add a new document with a generated ID
        db.collection("v-sion").document(uid!!).collection("usage_data").document(date).set(history)
            .addOnSuccessListener { info("DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> info( "Error writing document", e) }
        
    }

    //for Read, addOnCompleteListener is used so to make sure every steps in it has done before doing the next
    //better approach to try? : Coroutine
    fun readUsage(uid:String, myCallback: (MutableList<HistoryModel>) -> Unit) {
        db.collection("v-sion").document(uid).collection("usage_data").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val list = mutableListOf<HistoryModel>()
                for (document in task.result!!) {
                    val date = document.data["date"].toString()
                    val time = document.data["time"].toString()
                    val targetAchieved = document.data["targetAchieved"].toString()
                    list.add(HistoryModel(null,null,date,time,targetAchieved))
                }
                myCallback(list)
            }
        }
    }

    //for Delete
    fun deleteUsage(uid:String,date:String){
        db.collection("v-sion").document(uid).collection("usage_data").document(date).delete()
    }

}
