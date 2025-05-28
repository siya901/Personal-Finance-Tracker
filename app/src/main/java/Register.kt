package com.example.my

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val db = MyDatabase.getInstance(context)
    val background = painterResource(id = R.drawable.bgg)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = background,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xAA000000), Color(0xAA1B1B1B))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(value = name, onValueChange = {
                        if (it.all { ch -> ch.isLetter() || ch.isWhitespace() }) name = it
                    }, label = "Name", icon = Icons.Default.Person)

                    CustomTextField(value = email, onValueChange = { email = it }, label = "Email",
                        keyboardType = KeyboardType.Email, icon = Icons.Default.Email)

                    CustomTextField(value = phone, onValueChange = {
                        phone = it.filter { c -> c.isDigit() }
                    }, label = "Phone", keyboardType = KeyboardType.Phone, icon = Icons.Default.Phone)

                    CustomTextField(value = address, onValueChange = { address = it },
                        label = "Address", icon = Icons.Default.LocationOn)

                    CustomTextField(value = username, onValueChange = { username = it },
                        label = "Username", icon = Icons.Default.AccountBox)

                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
                            val specialCharRegex = Regex(".*[!@#\$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/].*")

                            when {
                                !email.matches(emailRegex) ->
                                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()

                                phone.length != 10 ->
                                    Toast.makeText(context, "Phone number must be 10 digits", Toast.LENGTH_SHORT).show()

                                db.isUsernameTaken(username) ->
                                    Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()

                                password.length < 8 ->
                                    Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()

                                password != confirmPassword ->
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()

                                !password.matches(specialCharRegex) ->
                                    Toast.makeText(context, "Weak password: Add a special character", Toast.LENGTH_SHORT).show()

                                listOf(name, email, phone, address, username, password).any { it.isEmpty() } ->
                                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()

                                else -> {
                                    db.insertUser(User(name, email, phone, address, username, password))
                                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("userProfile/$username") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                    ) {
                        Text("Sign Up", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF6A1B9A),
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color(0xFF6A1B9A)
        )
    )
}
