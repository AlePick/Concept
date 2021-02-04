package com.example.concept2

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PreTurn : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var user: FirebaseUser

    private var cloudFirestore = Firebase.firestore
    lateinit var gameID : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_turn)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser!!

        val intent = intent
        if (intent.getStringExtra("gameID") !== null){
            gameID = intent.getStringExtra("gameID")!!

            val gameRef = cloudFirestore.collection("macthes").document(gameID)
            gameRef.get()
                    .addOnSuccessListener {
                        if (it != null){
                            handleGameScreen(it)
                        }
                    }
        }

    }

    private fun handleGameScreen(gameRef: DocumentSnapshot){
        val game = gameRef.data
        val turnValue = game!!["Turn"]
        val turnTextView = findViewById<TextView>(R.id.turn)

        if (turnValue != null){
            turnTextView.text = "Turn ${turnValue.toString()}"
        } else {
            Toast.makeText(this, "Turn NULL", Toast.LENGTH_LONG).show()
        }

    }

}