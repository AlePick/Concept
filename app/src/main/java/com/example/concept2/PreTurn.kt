package com.example.concept2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class PreTurn : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var user: FirebaseUser

    private var cloudFirestore = Firebase.firestore
    lateinit var gameID : String
    lateinit var turnTextView: TextView
    lateinit var prompterTextView: TextView
    lateinit var readyButton: Button

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
                        //DOCUMENT NON ESISTE
                        /*if (it.exists()){
                            do
                        } else {
                            Toast.makeText(this, "Game document does not exist", Toast.LENGTH_LONG).show()
                            finish()
                        }*/
                        val prompterUsername = "ale"
                        val gameTurn = "gameTurn"
                        var role = ""
                        if (prompterUsername == user.displayName){
                            role = "Prompter"
                        } else {
                            role = "Player"
                        }

                        turnTextView = findViewById<TextView>(R.id.turn)
                        turnTextView.setText("Turn ${gameTurn}")

                        prompterTextView = findViewById<TextView>(R.id.prompter)
                        prompterTextView.setText("${prompterUsername} is your turn")

                        readyButton = findViewById<Button>(R.id.ready_button)
                        readyButton.setOnClickListener{
                            var newIntent: Intent

                            if ( role == "Prompter"){
                                newIntent = Intent(this@PreTurn, PrompterTurn::class.java)
                            } else {
                                newIntent = Intent(this@PreTurn, PlayerTurn::class.java)
                            }

                            newIntent.putExtra("gameID", gameID)
                            startActivity(newIntent)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Something went wrong during the game", Toast.LENGTH_LONG).show()
                        finish()
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