 package com.example.concept2

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.model.Document
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


 class LoggedIn : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var user: FirebaseUser

    lateinit var conceptButton: Button
    lateinit var addFriendButton: Button
    lateinit var addFriendInput: EditText
    lateinit var friendListView: RecyclerView

    private var galleryPermissionCode = 1001
    private var imagePickCode = 1000

    private var cloudFirestore = Firebase.firestore
    lateinit var snapshotListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_in)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser!!

        Toast.makeText(this, "Welcome " + user.displayName, Toast.LENGTH_SHORT).show()

        initHomepage()
    }

     override fun onStart() {
         super.onStart()
     }

     override fun onStop(){
         super.onStop()
     }
    private fun initHomepage(){
        //Recycler view from the friends list
        friendListView = findViewById<RecyclerView>(R.id.friends_list_view)
        val fsUser = cloudFirestore.collection("accounts").document("${user.displayName}")
        getOnlineFriendsList(fsUser)


        //SETTING ONLINE STATUS
        fsUser.update("Online", true)
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong with your online status. Try restarting Concept", Toast.LENGTH_SHORT).show()
                }


        conceptButton = findViewById<Button>(R.id.menu_button)
        activateConceptMenu(conceptButton)

        //RENDER PROFILE PICTURE MA PASSANDO COME ARGOMENTO UN StorageReference AL POSTO DI UN FILE URI

        addFriendButton = findViewById<Button>(R.id.add_friend_button)
        addFriendInput = findViewById<EditText>(R.id.add_friend)
        activateAddFriendButton(addFriendButton, addFriendInput)

    }


    private fun activateConceptMenu(button: Button){
        button.setOnClickListener {

            val menuDialog = Dialog(this)
            menuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            menuDialog.setContentView(R.layout.menu_pop_up)
            menuDialog.setTitle("Menu")

            //CLOSE MENU BUTTON
            menuDialog.findViewById<ImageView>(R.id.close_menu_button).setOnClickListener {
                menuDialog.cancel()
            }
            //USERNAME LABEL
            menuDialog.findViewById<TextView>(R.id.username_in_menu).text = user.displayName
            //PROFILE PICTURE
            val profilePicture = menuDialog.findViewById<ImageView>(R.id.profile_picture)
            Picasso.get().load(user.photoUrl).into(profilePicture)

            menuDialog.show()
            activateChangePictureListener(menuDialog)

            //LOGOUT BUTTON
            val logoutButton = menuDialog.findViewById<Button>(R.id.logout_button)
            activateLogoutButton(logoutButton)
        }
    }

    private fun activateAddFriendButton(button: Button, textInput: EditText){
        button.setOnClickListener {
            val friendUsername = textInput.text.toString()

            if (friendUsername.isNotEmpty()) {
                val userAccount = cloudFirestore.collection("accounts").document(friendUsername)
                userAccount.update("Friends", FieldValue.arrayUnion(user.displayName))
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val thisAccount = cloudFirestore.collection("accounts").document("${user.displayName}")
                                thisAccount.update("Friends", FieldValue.arrayUnion(friendUsername))
                                        .addOnCompleteListener(this) { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(this, "$friendUsername is now your friend!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this, "An error occurred during the request", Toast.LENGTH_LONG).show()
                                            }
                                        }
                            } else {
                                Toast.makeText(this, "This username does not exists", Toast.LENGTH_LONG).show()
                            }
                        }
            }
        }
    }




    private fun activateChangePictureListener(dialog: Dialog){
        dialog.findViewById<ImageView>(R.id.profile_picture).setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, galleryPermissionCode);
                }
                else{
                    //permission already granted
                    pickImageFromGallery()
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery()
            }
        }

    }

    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, imagePickCode)
    }



     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)

         when (requestCode){
             galleryPermissionCode -> {
                 if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                     pickImageFromGallery()
                 }
                 else {
                     Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                 }
             }
         }
     }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         if (resultCode == Activity.RESULT_OK && requestCode == imagePickCode){
             updateProfilePicture(data?.data!!)
         }
     }

     private fun updateProfilePicture(profilePictureUri: Uri){
         val profileUpdate = UserProfileChangeRequest.Builder().setPhotoUri(profilePictureUri).build()
         try{
             user.updateProfile(profileUpdate)
                 .addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         Toast.makeText(this@LoggedIn, "Succesfully updated!", Toast.LENGTH_SHORT).show()
                     }
                 }
         } catch (e:Exception){
             Toast.makeText(this@LoggedIn, e.message, Toast.LENGTH_SHORT).show()
         }
     }


    private fun activateLogoutButton(button: Button){
        button.setOnClickListener {
            snapshotListener.remove()
            cloudFirestore.collection("accounts").document("${user.displayName}")
                    .update("Online", false)
                    .addOnSuccessListener {
                        mAuth!!.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
        }
    }



    private fun getOnlineFriendsList(userAccount: DocumentReference) {

        snapshotListener = userAccount.addSnapshotListener(EventListener<DocumentSnapshot> addSnapshotListener@{ snapshot, e ->
            if (e != null) {
                Toast.makeText(this@LoggedIn, e.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val friendsList = ArrayList<Friend>()
            if (snapshot != null && snapshot.exists()) {
                val userFriends = snapshot.data?.get("Friends") as ArrayList<String>

                userFriends.forEachIndexed { index, friend ->
                    /* NON FUNZIONA, le due funzioni restituiscono sempre true
                    val isOnline = isOnline(cloudFirestore.collection("accounts").document(friend))
                    val isInGame = isInGame(cloudFirestore.collection("accounts").document(friend))
                    */

                    /* GENERAZIONE MANUALE ISONLINE ISONGAME*/
                    val isOnline = index - 2 < 0
                    var image = 0
                    if (isOnline){
                        image = R.drawable.user_green
                    } else{
                        image = R.drawable.user_red
                    }
                    val item= Friend(image, friend, isOnline, false)
                    friendsList += item
                }
            } else {
                Toast.makeText(this@LoggedIn, "Current data: null", Toast.LENGTH_SHORT).show()
            }

            friendListView.adapter = FriendAdapter(friendsList)
            friendListView.layoutManager = LinearLayoutManager(this)
            friendListView.setHasFixedSize(true)
        })
    }

    private fun isOnline(userAccount: DocumentReference): Boolean{
        var bool = true
        userAccount.get().addOnSuccessListener { document ->
            bool = document.data?.get("Online") as Boolean
        }
        return bool
    }
    private fun isInGame(userAccount: DocumentReference): Boolean{
        var bool = true
        userAccount.get().addOnSuccessListener { document ->
            bool = document.data?.get("Ingame") as Boolean
        }
        return bool
    }

 }
