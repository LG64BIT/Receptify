package app.shop.recptify.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shop.recptify.ui.theme.RecptifyTheme
import com.google.firebase.auth.FirebaseAuth

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecptifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE1E29F) // Set the background color here
                ) {
                    GreetingScreen()
                }
            }
        }
    }

    private fun openLoginActivity(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }

    private fun openRegisterActivity(context: Context) {
        val intent = Intent(context, RegisterActivity::class.java)
        context.startActivity(intent)
    }

    @Composable
    fun GreetingScreen() {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE1E29F))
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Greeting(name = "RECEPTIFY", fontSize = 50, marginTop = 50.dp)
            Greeting(name = "Cook with ease!", fontSize = 26, marginTop = 30.dp)
            Greeting(name = "Login", fontSize = 34, marginTop = 100.dp) {
                // Handle click on Login
                openLoginActivity(context)
            }
            Greeting(name = "Register", fontSize = 34, marginTop = 50.dp) {
                // Handle click on Register
                openRegisterActivity(context)
            }
        }
    }

    @Composable
    fun Greeting(name: String, fontSize: Int, marginTop: Dp, onClick: () -> Unit = {}) {
        Text(
            text = name,
            fontSize = fontSize.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(top = marginTop)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .clickable { onClick() }
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        RecptifyTheme {
            GreetingScreen()
        }
    }

    override fun onStart() {
        super.onStart()

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // User is already signed in
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}