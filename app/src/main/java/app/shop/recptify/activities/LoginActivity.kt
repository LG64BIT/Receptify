package app.shop.recptify.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
}

    @Composable
    fun LoginScreen() {
        val email = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val context = LocalContext.current
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val isLoading = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE1E29F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "Login",
                    fontSize = 40.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(50.dp))

                RoundedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = "Email"
                )

                Spacer(modifier = Modifier.height(24.dp))

                RoundedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(30.dp))


                OutlinedButton(
                    onClick = {
                        if (validateLoginData(context, email.value, password.value)) {
                            isLoading.value = true
                            loginUser(context, auth, email.value, password.value, isLoading)
                        }
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
                    )) {
                    Text(text = "Login", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.align(Alignment.Start)) {
                    Text(text = "Don't have an account? ",fontSize = 16.sp, color = Color.Black)
                    Text(
                        text = "Create new account",
                        fontSize = 16.sp,
                        color = Color(0xFF0000FF),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            // Handle create new account click
                            openRegisterActivity(context)
                            finish()
                        }
                    )
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
    }

    // Function to handle user login
    private fun loginUser(
        context: Context,
        auth: FirebaseAuth,
        email: String,
        password: String,
        isLoading: MutableState<Boolean>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false // Hide the progress bar

                if (task.isSuccessful) {
                    // Login successful
                    Toast.makeText(
                        context,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    openMainActivity()
                    // Handle successful login, e.g., navigate to home screen
                } else {
                    // Login failed
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "Invalid email or password."
                        else -> "Login failed. Please try again."
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateLoginData(context: Context, email: String, password: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email format
            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            // Empty password
            Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
            return false
        }

        // Other validation rules...

        return true
    }

    private fun openRegisterActivity(context: Context) {
        val intent = Intent(context, RegisterActivity::class.java)
        context.startActivity(intent)
    }

    private fun openMainActivity() {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
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
}