package app.shop.recptify.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import app.shop.recptify.R
import app.shop.recptify.model.PostModel
import app.shop.recptify.viewmodel.PostViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class HomeActivity : ComponentActivity() {

    private lateinit var postViewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            postViewModel = ViewModelProvider(this)[PostViewModel::class.java]

            UserPostScreen(postViewModel)
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UserPostScreen(viewModel: PostViewModel) {
        Log.d("HomeScreen", "UserPostScreen")
        val postsState by viewModel.posts.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val selectedCategoryState = remember { mutableStateOf("All") }

        var mDisplayMenu by remember { mutableStateOf(false) }


        val context = LocalContext.current
        LaunchedEffect(selectedCategoryState.value) {
            if (selectedCategoryState.value == "All") {
                viewModel.loadRandomPosts()
            } else {
                viewModel.loadPostsByCategory(selectedCategoryState.value)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFE1E29F))
        ) {
            Column(Modifier.fillMaxSize()) {
                // top bar

                TopAppBar(
                    title = { Text("RECEPTIFY",color= Color.White) },
                    actions = {
                        // Clickable icon in top bar
                        IconButton(onClick = {
                        }) {
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
                                },
                                text = { Text("Salty") }
                            )

                            DropdownMenuItem(
                                onClick = {
                                    mDisplayMenu = false
                                    selectedCategoryState.value = "Sweet"
                                },
                                text = { Text("Sweet") }
                            )
                        }
                    },
                    colors =TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF9FA06F))
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
                        items(postsState) { post ->
                            RecipeItem(recipe = post) { clickedPost ->
                                // Handle the click event, e.g., open detail screen
                                openDetailActivity(clickedPost, context)
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { openPostActivity(context) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF017BFE),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }


    private fun openProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun openDetailActivity(clickedPost: PostModel, context: Context) {
        val intent = Intent(context, PostDetailActivity::class.java).apply {
            putExtra("POST_MODEL", clickedPost)
        }
        context.startActivity(intent)
    }

    @Composable
    fun RecipeItem(recipe: PostModel, onItemClick: (PostModel) -> Unit) {
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
            }
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
            contentScale = ContentScale.FillBounds
        ) {
            it.error(R.drawable.upload_image)
                .placeholder(R.drawable.upload_image)
                .load(path)
        }
    }

    private fun openPostActivity(context: Context) {
        val intent = Intent(context, AddPostActivity::class.java)
        context.startActivity(intent)
    }
}
