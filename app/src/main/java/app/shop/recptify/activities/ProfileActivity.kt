package app.shop.recptify.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shop.recptify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
        }
    }

    @Composable
    fun ProfileScreen() {

        val loadingState = remember { mutableStateOf(true) }
        val username = remember { mutableStateOf<String?>(null) }
        // Retrieve user data from Firebase
        getUserDataFromFirebase { retrievedUsername ->
            loadingState.value = false
            username.value = retrievedUsername
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFE1E29F)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // app bar
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .background(color = Color(0xFF9FA06F))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (loadingState.value) {
                        // Show loading indicator or placeholder text
                        Text(
                            text = "Loading...",
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .align(Alignment.CenterVertically))
                    } else {
                        // Show the retrieved username
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
                        painter = painterResource(R.drawable.baseline_logout_24),
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                // Log out the user
                                FirebaseAuth.getInstance().signOut()
                                openLoginActivity()
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Profile",
                fontSize = 40.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(painter = painterResource(R.drawable.user),
                contentDescription = "Profile Image",
                modifier = Modifier.size(200.dp))

            Spacer(modifier = Modifier.height(10.dp))
            if (loadingState.value) {
                Text(
                    text = "Loading...",
                    fontSize = 20.sp,
                    color = Color.Black,
                )
            } else {
                username.value?.let {
                    Text(
                        text = username.value.toString(),
                        fontSize = 20.sp,
                        color = Color.Black,
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedButton(
                onClick = { openMyCollectionActivity() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
                    .background(Color(0xFF017BFE), RoundedCornerShape(8.dp)),
                border = BorderStroke(1.dp, Color(0xFF017BFE)),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color(0xFF017BFE),
                    disabledContainerColor = Color(0xFF017BFE)
                )) {
                Text(text = "My Collection", fontSize = 20.sp)
            }
        }
    }

    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun openMyCollectionActivity() {
        val intent = Intent(this, MyCollectionActivity::class.java)
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
}
