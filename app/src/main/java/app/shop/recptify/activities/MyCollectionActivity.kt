package app.shop.recptify.activities

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shop.recptify.R
import app.shop.recptify.model.PostModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import android.os.Handler
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.abs

@Suppress("NAME_SHADOWING")
class MyCollectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCollectionScreen()
        }
    }

    private var isActivityOpened = false

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyCollectionScreen() {
        var posts by remember { mutableStateOf(emptyList<PostModel>()) }
        var isLoading by remember { mutableStateOf(true) }
        val selectedCategoryState = remember { mutableStateOf("All") }
        var randomRecipe by remember { mutableStateOf<PostModel?>(null) }
        var mDisplayMenu by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) { }

            private var isShakeDetected = false
            private val shakeThreshold = 20

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val floatSum = abs(x) + abs(y) + abs(z)

                    if (!isShakeDetected && floatSum > shakeThreshold && !isActivityOpened) {
                        isShakeDetected = true

                        Handler().postDelayed({
                            isShakeDetected = false
                        }, 1000) // Reset isShakeDetected after 1 second

                        if (selectedCategoryState.value != "All") {
                            val selectedCategory = selectedCategoryState.value
                            val recipesForCategory = posts.filter { post ->
                                post.selectedCategory == selectedCategory
                            }

                            if (recipesForCategory.isNotEmpty()) {
                                val randomIndex = (recipesForCategory.indices).random()
                                val randomRecipe = recipesForCategory[randomIndex]
                                openDetailActivity(randomRecipe, context)
                                isActivityOpened = true
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(selectedCategoryState.value) {
            if (selectedCategoryState.value == "All") {
                getUserPosts { fetchedPosts ->
                    posts = fetchedPosts
                    isLoading = false
                    randomRecipe = null // Reset randomRecipe when category changes
                    Log.d("UserPostScreenPost", "Post Size: ${posts.size}")
                }
            } else {
                loadPostsByCategory({ fetchedPosts ->
                    posts = fetchedPosts
                    isLoading = false
                    randomRecipe = null // Reset randomRecipe when category changes
                    Log.d("UserPostScreenPost", "Post Size: ${posts.size}")
                }, selectedCategoryState.value)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFE1E29F))
        ) {
            Column(Modifier.fillMaxSize()) {

                TopAppBar(
                    title = { Text("My Collection", color = Color.White) },
                    actions = {
                        IconButton(onClick = {}) {
                            Image(painter = painterResource(R.drawable.user),
                                contentDescription = "User Icon",
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.CenterVertically)
                                    .clickable {}
                            )
                        }

                        // Creating Icon button for dropdown menu
                        IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                            Icon(Icons.Default.MoreVert, "", tint = Color.White)
                        }

                        // Creating a dropdown menu
                        DropdownMenu(
                            expanded = mDisplayMenu,
                            onDismissRequest = { mDisplayMenu = false }) {

                            DropdownMenuItem(
                                onClick = {
                                    mDisplayMenu = false
                                    selectedCategoryState.value = "Salty"
                                    isLoading = true
                                    randomRecipe = null // Reset randomRecipe when category changes
                                    isActivityOpened= false
                                },
                                text = { Text("Salty") }
                            )

                            DropdownMenuItem(
                                onClick = {
                                    isLoading = true
                                    mDisplayMenu = false
                                    selectedCategoryState.value = "Sweet"
                                    randomRecipe = null // Reset randomRecipe when category changes
                                    isActivityOpened = false

                                },
                                text = { Text("Sweet") }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(
                            0xFF9FA06F
                        )
                    )
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(posts) { post ->
                            RecipeItem(
                                recipe = post,
                                onItemClick = { clickedPost ->
                                    openDetailActivity(clickedPost, context)
                                },
                                onDelete = { recipeToDelete ->
                                    onDelete(recipeToDelete) {
                                        posts = posts.filter { it.id != recipeToDelete.id }
                                        Toast.makeText(
                                            context,
                                            "Recipe deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )
            onDispose {
                sensorManager.unregisterListener(sensorListener)
            }
        }
    }


    private fun openDetailActivity(clickedPost: PostModel, context: Context) {
        val intent = Intent(context, PostDetailActivity::class.java).apply {
            putExtra("POST_MODEL", clickedPost)
        }
        context.startActivity(intent)
    }

    @Composable
    fun RecipeItem(
        recipe: PostModel,
        onItemClick: (PostModel) -> Unit,
        onDelete: (PostModel) -> Unit // Add the onDelete parameter here
    ) {
        val showDialog = remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onItemClick(recipe) }
        ) {
            LoadImage(recipe.image)

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = recipe.title,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recipe.description,
                    style = TextStyle(fontSize = 14.sp),
                    maxLines = 5, // Set the maximum number of lines
                    overflow = TextOverflow.Ellipsis, // Add ellipsis if text exceeds the maximum lines
                    modifier = Modifier.fillMaxWidth()
                )

                Image(
                    painter = painterResource(R.drawable.baseline_delete_24),
                    contentDescription = "Delete Icon",
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.End)
                        .clickable { showDialog.value = true }
                )

                if (showDialog.value) {
                    AlertDialog(
                        modifier = Modifier.clip(RoundedCornerShape(0.dp)),
                        onDismissRequest = { showDialog.value = false },
                        title = { Text(text = "Confirmation") },
                        text = { Text(text = "Are you sure you want to delete this recipe?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    // Perform the delete operation
                                    onDelete(recipe) // Invoke the onDelete callback
                                    showDialog.value = false
                                }
                            ) {
                                Text(text = "OK")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDialog.value = false }
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun onDelete(recipe: PostModel, onDeleteSuccess: () -> Unit) {
        // Get a reference to the Firebase Realtime Database instance
        val database = FirebaseDatabase.getInstance()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Get a reference to the "MyCollection" node
        val myCollectionRef = database.getReference("MyCollections").child(userId!!)

        // Get a reference to the specific recipe node
        val recipeRef = myCollectionRef.child(recipe.id)

        // Delete the recipe node
        recipeRef.removeValue()
            .addOnSuccessListener {
                // Successfully deleted from the Firebase
                onDeleteSuccess.invoke() // Invoke the callback function
            }
            .addOnFailureListener { }
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
            contentScale = ContentScale.FillBounds
        ) {
            it.error(R.drawable.upload_image)
                .placeholder(R.drawable.upload_image)
                .load(path)
        }
    }

    private fun getUserPosts(callback: (List<PostModel>) -> Unit) {
        val posts = mutableListOf<PostModel>()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Get a reference to the specific path in the Firebase database
        val database = FirebaseDatabase.getInstance().getReference("MyCollections").child(userId!!)

        // Fetch data from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the existing posts list
                posts.clear()

                // Iterate through the dataSnapshot to get individual posts
                for (postSnapshot in snapshot.children) {
                    // Parse the post data and add it to the list
                    val post = postSnapshot.getValue(PostModel::class.java)
                    post?.let {
                        posts.add(it)
                        Log.d("FirebaseData", "Post: $it")
                    }
                }
                callback(posts)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadPostsByCategory(callback: (List<PostModel>) -> Unit, category: String) {
        val posts = mutableListOf<PostModel>()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Get a reference to the specific path in the Firebase database
        val database = FirebaseDatabase.getInstance().getReference("MyCollections").child(userId!!)
            .orderByChild("selectedCategory").equalTo(category)

        // Fetch data from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the existing posts list
                posts.clear()

                // Iterate through the dataSnapshot to get individual posts
                for (postSnapshot in snapshot.children) {
                    // Parse the post data and add it to the list
                    val post = postSnapshot.getValue(PostModel::class.java)
                    post?.let {
                        posts.add(it)
                        Log.d("FirebaseData", "Post: $it")
                    }
                }
                callback(posts)
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private val handler = Handler()

    override fun onResume() {
        super.onResume()

        // Set isActivityOpened to false after a delay of 1 seconds
        handler.postDelayed({
            isActivityOpened = false
        }, 1000)
    }

    override fun onPause() {
        super.onPause()
        // Remove any pending callbacks to avoid unwanted behavior
        handler.removeCallbacksAndMessages(null)
    }
}
