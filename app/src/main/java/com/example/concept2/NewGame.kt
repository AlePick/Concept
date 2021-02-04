package com.example.concept2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList

class NewGame : AppCompatActivity(), FriendAdapter.OnItemClickListener {
    private var mAuth: FirebaseAuth? = null
    private lateinit var user: FirebaseUser

    lateinit var friendListView: RecyclerView
    lateinit var readyFriendsListView: RecyclerView
    lateinit var backButton: Button
    lateinit var startGameButton: Button

    var onlineFriendsList = ArrayList<Friend>()
    var readyFriendsList = ArrayList<Friend>()
    var isPlayerReady = ArrayList<Boolean>()

    private var cloudFirestore = Firebase.firestore
    lateinit var snapshotListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser!!

        initLobbyPage()
    }


    private fun initLobbyPage(){
        friendListView = findViewById<RecyclerView>(R.id.online_friends_list_view)
        readyFriendsListView = findViewById<RecyclerView>(R.id.ready_friends_list_view)

        val fsUser = cloudFirestore.collection("accounts").document("${user.displayName}")
        onlineFriendsList = getOnlineFriendsList(fsUser)

        backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            this.finish()
        }

        startGameButton = findViewById(R.id.start_game_button)
        startGameButton.setOnClickListener{
            startNewGame(readyFriendsList)
        }
    }


    override fun onItemClick(position: Int) {
        if (!isPlayerReady[position]) {
            readyFriendsList.add(onlineFriendsList[position])
            isPlayerReady[position] = true

            readyFriendsListView.adapter = FriendAdapter(readyFriendsList, null)
            readyFriendsListView.layoutManager = LinearLayoutManager(this)
            readyFriendsListView.setHasFixedSize(true)
        } else {
            Toast.makeText(this, "${readyFriendsList[position].username} is already in the lobby", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOnlineFriendsList(userAccount: DocumentReference): ArrayList<Friend> {

        val friendsList = ArrayList<Friend>()

        snapshotListener = userAccount.addSnapshotListener(EventListener<DocumentSnapshot> addSnapshotListener@{ snapshot, e ->
            if (e != null) {
                Toast.makeText(this@NewGame, e.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val userFriends = snapshot.data?.get("Friends") as ArrayList<String>

                userFriends.forEachIndexed { index, friend ->
                    /* COROUTINES PER LA LISTA AMICI, NON FUNZIONA*/
                    /*val friendDocument = cloudFirestore.collection("accounts").document(friend)
                    GlobalScope.launch(Dispatchers.Unconfined) {
                        val friendData = friendDocument.get().await().toObject(User::class.java)
                        val isOnline = friendData!!.online
                        val image: Int
                        if (isOnline){
                            image = R.drawable.user_green
                        } else{
                            image = R.drawable.user_red
                        }
                        val item= Friend(image, friend, isOnline, false)
                        friendsList += item
                    }*/


                    // GENERAZIONE MANUALE AMICI ONLINE
                    var image = R.drawable.user_green
                    val item= Friend(image, friend, true, false)
                    friendsList.add(item)
                    isPlayerReady.add(false)
                }
            } else {
                Toast.makeText(this@NewGame, "Current data: null", Toast.LENGTH_SHORT).show()
            }

            friendListView.adapter = FriendAdapter(friendsList, this)
            friendListView.layoutManager = LinearLayoutManager(this)
            friendListView.setHasFixedSize(true)

        })
        return friendsList
    }


    private fun startNewGame(users: ArrayList<Friend>){
        if (users.size > 0) {
            val playersNames = ArrayList<String>()
            val playersScores = ArrayList<Number>()
            val newId = (0..9999999).random()

            playersNames.add(user.displayName.toString())
            playersScores.add(0)
            users.forEach {
                playersNames.add(it.username)
                playersScores.add(0)
            }

            val newGame = hashMapOf(
                    "Turn" to 1,
                    "Creator" to 0,
                    "Prompter" to 0,
                    "Players" to playersNames,
                    "Scores" to playersScores
            )
            cloudFirestore.collection("matches").document(newId.toString())
                    .set(newGame)
                    .addOnSuccessListener {
                        val intent = Intent(this@NewGame, PreTurn::class.java)
                        intent.putExtra("gameID", newId.toString())
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Something went wrong during the creation of the game. Try again!", Toast.LENGTH_LONG).show()
                    }
        } else {
            Toast.makeText(this, "The lobby is empty, invite some of your friends by clicking on them", Toast.LENGTH_LONG).show()
        }
    }
}