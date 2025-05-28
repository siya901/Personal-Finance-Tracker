package com.example.my

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.my.database.FinanceDatabase
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, dbHelper: FinanceDatabase, username: String) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedType by remember { mutableStateOf("Income") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    var totalIncome by remember { mutableStateOf(0.0) }
    var totalExpense by remember { mutableStateOf(0.0) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var selectedDate by remember {
        mutableStateOf(
            "${year}-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
        )
    }

    fun refreshSummary() {
        coroutineScope.launch {
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val data = dbHelper.calculateBalanceForMonth(username, currentYear.toString(), currentMonth.toString())
            totalIncome = data.first
            totalExpense = data.second
        }
    }

    LaunchedEffect(Unit) {
        refreshSummary()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PocketPilot",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    // Overview Button
                    IconButton(onClick = { navController.navigate("overview/$username") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Overview", tint = Color.White)
                    }

                    // History Button
                    IconButton(onClick = { navController.navigate("history/$username") }) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Profile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A148C))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 💰 Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Monthly Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF6A1B9A))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem("Income", totalIncome, Color(0xFF388E3C))
                        SummaryItem("Expense", totalExpense, Color(0xFFD32F2F))
                        SummaryItem("Balance", totalIncome - totalExpense, Color(0xFF1976D2))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { selectedType = "Income" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0F0C0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Income", color = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { selectedType = "Expense" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFC62828))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Expense", color = Color(0xFFC62828))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✨ Stylized Add Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add New $selectedType",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1976D2)
                        )
                    }

                    var isAmountError by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            val regex = Regex("^\\d{0,7}(\\.\\d{0,2})?")
                            val isValidFormat = regex.matches(it)
                            val isNotZero = it.toDoubleOrNull()?.let { v -> v > 0 } == true
                            isAmountError = !(isValidFormat && isNotZero)
                            if (!isAmountError) amount = it
                        },
                        label = { Text("Amount") },
                        isError = isAmountError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    var isCategoryError by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            val regex = Regex("^[a-zA-Z ]*")
                            isCategoryError = !regex.matches(it)
                            category = it
                        },
                        label = { Text("Category") },
                        isError = isCategoryError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 📅 Date Picker
                    Button(
                        onClick = {
                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                                    selectedDate = "${selectedYear}-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}"
                                },
                                year,
                                month,
                                day
                            )
                            // ✅ Restrict future dates
                            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
                            datePickerDialog.show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1C4E9))
                    ) {
                        Text("Select Date: $selectedDate", color = Color(0xFF4A148C))
                    }

                    Button(
                        onClick = {
                            if (amount.isEmpty() || category.isEmpty() || isAmountError || isCategoryError) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please enter valid amount and category")
                                }
                            } else {
                                coroutineScope.launch {
                                    dbHelper.addTransaction(username, selectedType, amount.toDouble(), category, selectedDate)
                                    snackbarHostState.showSnackbar("$selectedType added successfully!")
                                    amount = ""
                                    category = ""
                                    refreshSummary()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "Income") Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add $selectedType", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text("₹$value", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
    }
}
