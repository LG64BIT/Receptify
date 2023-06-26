package app.shop.recptify.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import app.shop.recptify.model.PostModel
import com.google.firebase.database.*
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PostViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("posts")
    private val _posts: MutableStateFlow<List<PostModel>> = MutableStateFlow(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Load random posts initially
        loadRandomPosts()
    }

    fun loadRandomPosts() {
        viewModelScope.launch {
            _isLoading.value = true

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<PostModel>()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(PostModel::class.java)
                        post?.let {
                            posts.add(it)
                        }
                    }

                    // Shuffle the posts list
                    posts.shuffle()
                    _posts.value = posts

                    Log.d("FirebaseData", "Random Posts: $posts")

                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                    _isLoading.value = false
                }
            })
        }
    }

    fun loadPostsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Clear the existing posts list
            _posts.value = emptyList()

            database.orderByChild("selectedCategory").equalTo(category)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val posts = mutableListOf<PostModel>()
                        for (postSnapshot in snapshot.children) {
                            val post = postSnapshot.getValue(PostModel::class.java)
                            post?.let {
                                posts.add(it)
                            }
                        }
                        _posts.value = posts

                        // Log the posts
                        Log.d("FirebaseData", "Posts by Category '$category': $posts")
                        _isLoading.value = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if needed
                        _isLoading.value = false
                    }
                })
        }
    }
}
