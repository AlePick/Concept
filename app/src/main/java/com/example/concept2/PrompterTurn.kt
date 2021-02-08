package com.example.concept2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class PrompterTurn : AppCompatActivity() {
    
    private var mAuth: FirebaseAuth? = null
    private lateinit var user: FirebaseUser
    private var storage = Firebase.storage
    private var storageRef = storage.getReference().child("cards")

    private lateinit var cardsListView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prompter_turn3)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser!!

        //NON FUNZIONA
        //loadCards()
    }

    private fun loadCards(){
        val cardsRows: ArrayList<CardsRow> = ArrayList()

        cardsListView = findViewById<RecyclerView>(R.id.cards_list_view)

        val imagesListTasks: Task<ListResult> = storageRef.listAll()
        imagesListTasks.addOnCompleteListener { result ->
            val images: List<StorageReference> = result.result!!.items
            var counter = 0
            var temp: ArrayList<String> = ArrayList()

            images.forEach {item ->
                item.downloadUrl.addOnSuccessListener {
                    temp.add(it.toString())
                    if (counter == 2){
                        cardsRows.add(CardsRow(temp[0], temp[1], temp[2]))
                    }
                    counter += 1
                }.addOnCompleteListener {
                    if (counter >= 3){
                        cardsListView.adapter = CardsRowAdapter(cardsRows, this)
                        cardsListView.layoutManager = LinearLayoutManager(this)
                        counter = 0
                    }

                }
            }

        }
    }


}