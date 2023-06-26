package app.shop.recptify.activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.shop.recptify.R
import app.shop.recptify.model.PostModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostDetailScreen()
        }
    }

    @Composable
    private fun PostDetailScreen() {
        val clickedPost = intent?.getSerializableExtra("POST_MODEL") as? PostModel
        val isLoading = remember { mutableStateOf(false) }
        val loadingState = remember { mutableStateOf(true) }
        val username = remember { mutableStateOf<String?>(null) }

        Log.d("PostDetailScreen", clickedPost!!.description)

        // Function to retrieve user data from Firebase
        getUserDataFromFirebase { retrievedUsername ->
            loadingState.value = false
            username.value = retrievedUsername
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFE1E29F)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth()
                        .background(color = Color(0xFF9FA06F))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "RECEPTIFY",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        if (loadingState.value) {
                            Text(
                                text = "Loading...",
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        } else {
                            username.value?.let {
                                Text(
                                    text = username.value.toString(),
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                        Image(
                            painter = painterResource(R.drawable.user),
                            contentDescription = "User Icon",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterVertically)
                                .clickable { onProfileScreen() }
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    LoadImage(clickedPost.image)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = clickedPost.title,
                        style = TextStyle(fontSize = 20.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = clickedPost.description,
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedButton(
                            onClick = {
                                uploadPostToFirebase(
                                    clickedPost.selectedCategory,
                                    clickedPost.image,
                                    clickedPost.title,
                                    clickedPost.description,
                                    baseContext,
                                    isLoading
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color(0xFF017BFE), RoundedCornerShape(8.dp)),
                            border = BorderStroke(1.dp, Color(0xFF017BFE)),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color(0xFF017BFE),
                                disabledContainerColor = Color(0xFF017BFE)
                            )
                        )
                        {
                            Text(text = "Add to my collection", fontSize = 20.sp)
                        }
                    }
                    // Show the progress dialog based on the isLoading state
                    if (isLoading.value) {
                        ProgressDialog()
                    }
                }
            }
        }
    }

    private fun onProfileScreen() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun getUserDataFromFirebase(callback: (String?) -> Unit) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId.toString())

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java)
                callback(username)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    @Composable
    fun ProgressDialog() {
        Dialog(
            onDismissRequest = { /* Handle dialog dismiss */ },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                CircularProgressIndicator()
            }
        }
    }

    private fun uploadPostToFirebase(
        category: String,

        imageUrl: String?,
        title: String,
        description: String,
        context: Context,
        isLoading: MutableState<Boolean>
    ) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val postsRef = database.getReference("MyCollections").child(userId!!)
        val post = PostModel(category,imageUrl.toString(), title, description, "") // Initialize ID as empty string

        // Validate the user input
        if (post.title.isNotEmpty() && post.description.isNotEmpty() && post.image.isNotEmpty()) {
            // Show progress dialog
            isLoading.value = true

            // Generate a unique key for the post
            val postId = postsRef.push().key

            if (postId != null) {
                // Assign the generated ID to the post
                post.id = postId

                // Store the post data under the generated key
                postsRef.child(postId).setValue(post)
                    .addOnSuccessListener {
                        // Post uploaded successfully
                        Toast.makeText(context, "Collection uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        // Error occurred while uploading the post
                        Toast.makeText(
                            context,
                            "Error uploading post: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        isLoading.value = false
                    }
                    .addOnCompleteListener {
                        // Hide progress dialog
                        isLoading.value = false
                    }
            }
        } else {
            Toast.makeText(context, "Please select all the fields", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun LoadImage(path: String) {
        GlideImage(
            model = path,
            contentDescription = "LoadImage",
            modifier = Modifier
                .size(160.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = Color.Gray),
            contentScale = ContentScale.FillBounds){
            it.error(R.drawable.upload_image)
                .placeholder(R.drawable.upload_image)
                .load(path)}
    }
}
