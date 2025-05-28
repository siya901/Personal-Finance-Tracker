package com.example.my.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.my.database.FinanceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowDropDown


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(navController: NavController, db: FinanceDatabase, username: String) {
    val viewModel: OverviewViewModel = viewModel(factory = OverviewViewModelFactory(db))

    var selectedYear by remember { mutableIntStateOf(2025) }
    var selectedMonth by remember { mutableIntStateOf(4) }
    val incomeExpense by viewModel.incomeExpenseData.collectAsState()
    val expensePieData by viewModel.expenseCategoryData.collectAsState()
    val mostExpensiveDay by viewModel.mostExpensiveDay.collectAsState()
    val smartTips by viewModel.smartTips.collectAsState()

    LaunchedEffect(selectedYear, selectedMonth) {
        viewModel.fetchIncomeExpense(username, selectedYear, selectedMonth)
        viewModel.fetchExpenseCategoryData(username, selectedYear, selectedMonth)
        viewModel.fetchMostExpensiveDay(username, selectedYear, selectedMonth)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" Monthly Overview", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonthDropdown(selectedMonth) { selectedMonth = it }
                YearDropdown(selectedYear) { selectedYear = it }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Total Income", "₹${incomeExpense.first}", Color(0xFFDCC9FF), Modifier.weight(1f))
                SummaryCard("Total Expenses", "₹${incomeExpense.second}", Color(0xFFFFE0CC), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Expense by Category",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpensePieChart(expensePieData)
                    InsightsSection(
                        income = incomeExpense.first,
                        expenses = incomeExpense.second,
                        mostExpensiveDay = mostExpensiveDay,
                        insights = smartTips
                    )
                }
            }
        }
    }
}

@Composable
fun ExpensePieChart(data: List<Pair<String, Double>>) {
    val total = data.sumOf { it.second }
    if (total == 0.0) {
        Text("No expenses this month", modifier = Modifier.padding(16.dp))
        return
    }

    val colors = listOf(
        Color(0xFFFFE0CC), Color(0xFFBA68C8), Color(0xFF64B5F6),
        Color(0xFF98FF98), Color(0xFFF8DE7E), Color(0xFFC0C0C0)
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = 0f
        data.forEachIndexed { index, (_, amount) ->
            val sweep = (amount / total * 360f).toFloat()
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset.Zero,
                size = size
            )
            startAngle += sweep
        }
    }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        data.forEachIndexed { index, (category, amount) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(colors[index % colors.size], shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("$category: ₹${amount.roundToInt()}", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun MonthDropdown(selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFF1F1F1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(months[selectedMonth - 1], fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month")
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEachIndexed { index, month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun YearDropdown(selectedYear: Int, onYearSelected: (Int) -> Unit) {
    val years = (2024..2030).toList()
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFF1F1F1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedYear.toString(), fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Year")
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, backgroundColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InsightsSection(
    income: Double,
    expenses: Double,
    mostExpensiveDay: String = "N/A",
    insights: List<String>
) {
    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Expense-to-Income Ratio",
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val ratio = if (income == 0.0) 0f else (expenses / income).toFloat().coerceIn(0f, 1f)

    LinearProgressIndicator(
        progress = ratio,
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = if (ratio < 0.5f) Color(0xFF98FF98) else Color(0xFFE57373),
        trackColor = Color.LightGray
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Most Expensive Day: $mostExpensiveDay",
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Color(0xFFEF5350),
        modifier = Modifier.padding(bottom = 16.dp)
    )

    /*Text(
        text = "Smart Tips",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )*/
    if (insights.isEmpty()) {
        Text(
            "No tips for now. You're doing great!",
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            insights.forEach { tip ->
                val bgColor = when {
                    tip.contains("Great job", ignoreCase = true) || tip.contains("No expenses", ignoreCase = true) ->
                        Color(0xFFE0F2F1) // Mint green (positive)
                    tip.contains("Warning", ignoreCase = true) ->
                        Color(0xFFFFEBEE) // Light red (alert)
                    tip.contains("High spending", ignoreCase = true) ->
                        Color(0xFFFFF8E1) // Yellowish (caution)
                    else -> Color(0xFFEDE7F6) // Light purple (neutral/info)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = tip,
                        fontSize = 15.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }

}

class OverviewViewModelFactory(private val db: FinanceDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OverviewViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class OverviewViewModel(private val db: FinanceDatabase) : ViewModel() {
    private val _incomeExpenseData = MutableStateFlow(0.0 to 0.0)
    val incomeExpenseData: StateFlow<Pair<Double, Double>> = _incomeExpenseData

    private val _expenseCategoryData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val expenseCategoryData: StateFlow<List<Pair<String, Double>>> = _expenseCategoryData

    private val _mostExpensiveDay = MutableStateFlow("N/A")
    val mostExpensiveDay: StateFlow<String> = _mostExpensiveDay

    private val _smartTips = MutableStateFlow<List<String>>(emptyList())
    val smartTips: StateFlow<List<String>> = _smartTips

    fun fetchIncomeExpense(username: String, year: Int, month: Int) {
        viewModelScope.launch {
            val data = db.calculateBalanceForMonth(username, year.toString(), month.toString())
            _incomeExpenseData.value = data
            updateSmartTips()
        }
    }

    fun fetchExpenseCategoryData(username: String, year: Int, month: Int) {
        viewModelScope.launch {
            val categoryData = db.getCategoryTotalsForMonth(username, year.toString(), month.toString(), "Expense")
            _expenseCategoryData.value = categoryData
            updateSmartTips()
        }
    }

    fun fetchMostExpensiveDay(username: String, year: Int, month: Int) {
        viewModelScope.launch {
            val date = db.getMostExpensiveDay(username, year.toString(), month.toString())
            _mostExpensiveDay.value = date ?: "N/A"
        }
    }

    private fun updateSmartTips() {
        val (income, expenses) = _incomeExpenseData.value
        val categoryData = _expenseCategoryData.value

        val tips = mutableListOf<String>()

        if (income > 0) {
            val ratio = expenses / income
            if (ratio < 0.5) {
                tips.add("Great job! You saved over 50% of your income.")
            } else if (ratio > 0.9) {
                tips.add("Warning: You're spending more than 90% of your income.")
            }
        }

        val highCategory = categoryData.maxByOrNull { it.second }
        if (highCategory != null && highCategory.second > (0.3 * expenses)) {
            tips.add("High spending detected in ${highCategory.first}. Consider reviewing it.")
        }

        if (categoryData.isEmpty() && expenses > 0) {
            tips.add("Expenses recorded, but no category breakdown. Categorize your transactions for insights.")
        }

        if (expenses == 0.0) {
            tips.add("No expenses this month. Great saving!")
        }

        _smartTips.value = tips
    }
}
