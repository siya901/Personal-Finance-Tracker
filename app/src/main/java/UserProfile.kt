
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.my.MyDatabase
import com.example.my.R
import com.example.my.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.layout.ContentScale

// ✅ Add this at the top
@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF4A148C)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1B1B)
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen(navController: NavController, username: String) {
    val context = LocalContext.current
    val db = MyDatabase.getInstance(context)
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(username) {
        withContext(Dispatchers.IO) {
            user = db.getUserByUsername(username)
        }
    }

    val backgroundImage: Painter = painterResource(id = R.drawable.bgg)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = backgroundImage,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xAA4A148C), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(110.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF6A1B9A))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            user?.let {
                Text(
                    text = it.name.uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } ?: CircularProgressIndicator(color = Color.White)

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            user?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ProfileInfoRow("Username:", it.username)
                        ProfileInfoRow("Email:", it.email)
                        ProfileInfoRow("Phone:", it.phone)
                        ProfileInfoRow("Address:", it.address)
                    }
                }
            } ?: CircularProgressIndicator()

            Spacer(modifier = Modifier.height(36.dp))

            val buttonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))

            Button(
                onClick = { navController.navigate("home_screen/$username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = buttonColors,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("➕ ADD TRANSACTION", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { navController.navigate("overview/$username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = buttonColors,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📊 OVERVIEW", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = buttonColors,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🚪 LOGOUT", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
