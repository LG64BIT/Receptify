package app.shop.recptify.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shop.recptify.R
import app.shop.recptify.model.PostModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AddPostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostScreen()
        }
    }

    @Composable
    fun PostScreen() {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        val isLoading = remember { mutableStateOf(false) }
        val loadingState = remember { mutableStateOf(true) }
        val username = remember { mutableStateOf<String?>(null) }

        // Function to retrieve user data from Firebase
        getUserDataFromFirebase { retrievedUsername ->
            loadingState.value = false
            username.value = retrievedUsername
        }

        // Image Uri state to store the selected image URI
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        // Create an ActivityResultLauncher to handle the image selection result
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            // Update the imageUri state with the selected image URI
            imageUri = uri
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFE1E29F)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    // Username Text
                    if (loadingState.value) {
                        // Show loading indicator or placeholder text
                        Text(
                            text = "Loading...",
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .align(Alignment.CenterVertically)
                        )
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
                        painter = painterResource(R.drawable.user),
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                openProfileActivity()
                            }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(color = Color(0xFFE1E29F))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Post a recipe",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                )

                // Upload image view
                val uploadImageViewModifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))

                if (imageUri != null) {
                    // Display the selected image if available
                    val painter = rememberAsyncImagePainter(imageUri)

                    Image(
                        painter = painter,
                        contentDescription = "Recipe Image",
                        modifier = uploadImageViewModifier,
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    // Display the default image if no image is selected
                    Image(
                        painter = painterResource(id = R.drawable.upload_image),
                        contentDescription = "Recipe Image",
                        modifier = uploadImageViewModifier,
                        contentScale = ContentScale.FillBounds
                    )
                }
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
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
                ) {
                    Text(text = "Select image from gallery", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))

                // add spinner here
                val listCategory = listOf("Sweet", "Salty")
                var selectedCategory by remember { mutableStateOf("Select category") }

                Spinner(itemList = listCategory,
                    selectedItem = selectedCategory,
                    onItemSelected = { selectedCategory = it })
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Title", fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(5.dp))
                RoundedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Description",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                )
                RoundedTextFieldDescription(
                    value = description,
                    onValueChange = { description = it },
                    label = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(bottom = 16.dp),
                    visualTransformation = VisualTransformation.None
                )
                Spacer(modifier = Modifier.height(20.dp))
                val context = LocalContext.current
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                OutlinedButton(
                    onClick = {
                        if (imageUri == null) {
                            Toast.makeText(context, "Please select image", Toast.LENGTH_SHORT)
                                .show()
                            return@OutlinedButton
                        }
                        if (selectedCategory == "Select category") {
                            // Upload the selected category to the database
                            Toast.makeText(context, "Please select category", Toast.LENGTH_SHORT)
                                .show()
                            return@OutlinedButton
                        }
                        if (title.isEmpty()) {
                            // Upload the selected category to the database
                            Toast.makeText(context, "Please select title", Toast.LENGTH_SHORT)
                                .show()
                            return@OutlinedButton
                        }
                        if (description.isEmpty()) {
                            // Upload the selected category to the database
                            Toast.makeText(context, "Please select description", Toast.LENGTH_SHORT)
                                .show()
                            return@OutlinedButton
                        }
                        uploadImageToFirebaseStorage(
                            imageUri!!,
                            title,
                            description,
                            userId!!,
                            context,
                            isLoading,
                            selectedCategory
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
                ) {
                    Text(text = "Post Recipe", fontSize = 16.sp)
                }
            }
        }

        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable(enabled = false) { /* Disable clicks when loading */ },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    private fun openProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    @Composable
    fun RoundedTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        visualTransformation: VisualTransformation = VisualTransformation.None
    ) {
        var isHintVisible by remember { mutableStateOf(value.isEmpty()) }

        BasicTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                isHintVisible = it.isEmpty()
            },
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
            textStyle = TextStyle(color = Color.Black),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (isHintVisible) {
                        Text(text = label, modifier = Modifier.padding(start = 0.dp))
                    }
                    innerTextField()
                }
            },
            visualTransformation = visualTransformation
        )
    }

    @Composable
    fun RoundedTextFieldDescription(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        visualTransformation: VisualTransformation = VisualTransformation.None
    ) {
        var isHintVisible by remember { mutableStateOf(value.isEmpty()) }

        BasicTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                isHintVisible = it.isEmpty()
            },
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.TopStart) {
                    if (isHintVisible) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(top = 20.dp, start = 16.dp),
                            style = TextStyle(color = Color.Gray, fontSize = 24.sp)
                        )
                    }
                    innerTextField()
                }
            },
            visualTransformation = visualTransformation
        )
    }

    // Function to upload image to Firebase Storage
    private fun uploadImageToFirebaseStorage(
        imageUri: Uri,
        title: String,
        description: String,
        userId: String,
        context: Context,
        isLoading: MutableState<Boolean>,
        selectedCategory: String
    ): Task<Uri> {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef: StorageReference =
            storageRef.child("images/$userId/${imageUri.lastPathSegment}")
        isLoading.value = true
        return imageRef.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                isLoading.value = false
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnSuccessListener { downloadUri ->
            // Image upload successful, call uploadDataToFirebase function
            uploadPostToFirebase(
                downloadUri.toString(),
                title,
                description,
                userId,
                context,
                isLoading,
                selectedCategory
            )
        }
    }

    private fun uploadPostToFirebase(
        imageUri: String?,
        title: String,
        description: String,
        userId: String?,
        context: Context,
        isLoading: MutableState<Boolean>,
        selectedCategory: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.getReference("posts")
        val post = PostModel(selectedCategory, imageUri.toString(), title, description, userId ?: "")

        // Validate the user input
        if (post.title.isNotEmpty() && post.description.isNotEmpty() && post.image.isNotEmpty()) {
            // Generate a unique key for the post
            val postId = postsRef.push().key
            isLoading.value = true
            // Store the post data under the generated key
            postsRef.child(postId ?: "").setValue(post)
                .addOnSuccessListener {
                    // Post uploaded successfully
                    Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                    isLoading.value = false
                    openHomeScreen()
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
        } else {
            // Display an error message if the input is invalid
            Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0,0)
        finish() // finish the current activity
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
    fun Spinner(
        itemList: List<String>,
        selectedItem: String,
        onItemSelected: (String) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedItem,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.padding(10.dp)
                )
                TextButton(
                    onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.Unspecified // Remove stroke by setting the color to Unspecified
                    )
                }
            }

            if (expanded) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    itemList.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onItemSelected(item)
                            },
                            text = { Text(item) }
                        )
                    }
                }
            }
        }
    }
}
