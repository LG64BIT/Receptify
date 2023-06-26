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
import androidx.compose.foundation.layout.*
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
import app.shop.recptify.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
        }
    }

    private fun openLoginActivity(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)

    }


    @Composable
    fun RegisterScreen() {
        val email = remember { mutableStateOf("") }
        val username = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val repeatPassword = remember { mutableStateOf("") }
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
                    text = "Register",
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
                    value = username.value,
                    onValueChange = { username.value = it },
                    label = "Username"
                )

                Spacer(modifier = Modifier.height(24.dp))

                RoundedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                RoundedTextField(
                    value = repeatPassword.value,
                    onValueChange = { repeatPassword.value = it },
                    label = "Repeat Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedButton(
                    onClick = {
                        if (validateUserData(
                                context,
                                email.value,
                                username.value,
                                password.value,
                                repeatPassword.value
                            )
                        ) {
                            isLoading.value = true
                            registerUser(
                                context,
                                auth,
                                email.value,
                                username.value,
                                password.value,
                                isLoading
                            )
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
                    Text(text = "Register", fontSize = 16.sp)
                }


                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.align(Alignment.Start)) {
                    Text(text = "Already registered? ", fontSize = 16.sp, color = Color.Black)
                    Text(
                        text = "Login",
                        color = Color(0xFF0000FF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            // Handle Login click
                            openLoginActivity(context)
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


    // Function to handle user registration
    private fun registerUser(
        context: Context,
        auth: FirebaseAuth,
        email: String,
        username: String,
        password: String,
        isLoading: MutableState<Boolean>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // Save user data to Realtime Database
                    saveUserDataToDatabase(context, auth, email, username, isLoading)
                } else {
                    // Registration failed
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "User with this email already exists."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password format."
                        else -> "Registration failed. Please try again."
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Function to save user data to Realtime Database
    private fun saveUserDataToDatabase(
        context: Context,
        auth: FirebaseAuth,
        email: String,
        username: String,
        isLoading: MutableState<Boolean>
    ) {
        val user = auth.currentUser
        val userId = user?.uid
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId!!)

        // Example: Saving email and username to the database
        val userData = mapOf(
            "email" to email,
            "username" to username
        )

        userRef.setValue(userData)
            .addOnSuccessListener {

                isLoading.value = false // Hide the progress bar
                // Retrieving name and password
                Prefs.getInstance(context).username = username
                Prefs.getInstance(context).email = email

                // Registration successful
                Toast.makeText(
                    context,
                    "Registration successful!",
                    Toast.LENGTH_SHORT
                ).show()

                openHomeActivity()

            }.addOnFailureListener { exception ->
                // Error occurred while saving data
                Toast.makeText(
                    context,
                    "Failed to save user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()

                isLoading.value = false // Hide the progress bar

            }
    }

    private fun openHomeActivity() {
        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
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

    private fun validateUserData(
        context: Context,
        email: String,
        username: String,
        password: String,
        repeatPassword: String
    ): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(context, "Please enter an email.", Toast.LENGTH_SHORT).show()
            return false
        }


        if (username.isEmpty()) {
            Toast.makeText(context, "Please enter a username.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(context, "Please enter a password.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(
                context,
                "Password must be at least 6 characters long.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (repeatPassword.isEmpty()) {
            Toast.makeText(context, "Please repeat the password.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != repeatPassword) {
            Toast.makeText(context, "Passwords are not equal.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}

