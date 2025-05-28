package com.example.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.my.database.FinanceDatabase
import com.example.my.database.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, dbHelper: FinanceDatabase, username: String) {
    val coroutineScope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf("All") }
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }

    val incomeList = transactions.filter { it.type == "Income" }
    val expenseList = transactions.filter { it.type == "Expense" }

    fun loadTransactions() {
        coroutineScope.launch {
            val allTransactions = dbHelper.getAllTransactionsByUserDescending(username)
            val now = Calendar.getInstance()

            transactions = when (selectedFilter) {
                "Weekly" -> {
                    val startOfWeek = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    allTransactions.filter {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                        date?.after(startOfWeek) == true
                    }
                }

                "Monthly" -> {
                    val currentMonth = now.get(Calendar.MONTH) + 1
                    allTransactions.filter {
                        val month = it.date.split("-")[1].toIntOrNull()
                        month == currentMonth
                    }
                }

                "Yearly" -> {
                    val currentYear = now.get(Calendar.YEAR)
                    allTransactions.filter {
                        val year = it.date.split("-")[0].toIntOrNull()
                        year == currentYear
                    }
                }

                else -> allTransactions
            }
        }
    }

    LaunchedEffect(selectedFilter) {
        loadTransactions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Transaction History", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Filter Buttons
            val filters = listOf("All", "Weekly", "Monthly", "Yearly")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                filters.forEach { filter ->
                    ElevatedButton(
                        onClick = { selectedFilter = filter },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFilter == filter) Color(0xFF4A148C) else Color(0xFFF3E5F5),
                            contentColor = if (selectedFilter == filter) Color.White else Color(0xFF4A148C)
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(text = filter, fontWeight = FontWeight.Medium)
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Expense Section first
                item {
                    Text("Expense", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFFC2361F))
                    if (expenseList.isEmpty()) {
                        Text("No expense transactions", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                items(expenseList.size) { index ->
                    TransactionCard(expenseList[index])
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Income", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF388E3C))
                    if (incomeList.isEmpty()) {
                        Text("No income transactions", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                items(incomeList.size) { index ->
                    TransactionCard(incomeList[index])
                }
            }

        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Date: ${transaction.date}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transaction.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "₹${transaction.amount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (transaction.type == "Income") Color(0xFF388E3C) else Color(0xFFC2361F)
                )
            }
        }
    }
}
