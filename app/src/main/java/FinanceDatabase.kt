package com.example.my.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class FinanceDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FinanceDB.db"
        private const val DATABASE_VERSION = 1

        // Transactions Table
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTransactionsTable = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_TYPE TEXT CHECK($COLUMN_TYPE IN ('Income', 'Expense')),
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_USERNAME) REFERENCES users(username) ON DELETE CASCADE
            );
        """.trimIndent()
        db.execSQL(createTransactionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    // 🔹 Add a new transaction
    fun addTransaction(username: String, type: String, amount: Double, category: String, date: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_TYPE, type)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DATE, date)
        }
        db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
    }

    // Function to get income, expense, and balance for the current month
    fun getCurrentMonthBalance(username: String): Triple<Double, Double, Double> {
        val currentCalendar = Calendar.getInstance()
        val year = currentCalendar.get(Calendar.YEAR).toString()
        val month = (currentCalendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')

        // Get total income and expense for the current month
        val (totalIncome, totalExpense) = calculateBalanceForMonth(username, year, month)

        // Calculate the balance (income - expense)
        val totalBalance = totalIncome - totalExpense

        return Triple(totalIncome, totalExpense, totalBalance)
    }



    // 🔹 Get all transactions for a user sorted by ID in descending order
    fun getAllTransactionsByUserDescending(username: String): List<Transaction> {
        val db = readableDatabase
        val transactions = mutableListOf<Transaction>()

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $COLUMN_USERNAME = ? ORDER BY $COLUMN_ID DESC",
            arrayOf(username)
        )

        while (cursor.moveToNext()) {
            transactions.add(
                Transaction(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getString(4),
                    cursor.getString(5)
                )
            )
        }

        cursor.close()
        db.close()
        return transactions
    }

    // 🔹 Calculate total income & expense for selected month
    fun calculateBalanceForMonth(username: String, year: String, month: String): Pair<Double, Double> {
        val db = readableDatabase
        var totalIncome = 0.0
        var totalExpense = 0.0

        val monthStr = month.padStart(2, '0')

        val cursor = db.rawQuery(
            "SELECT type, amount FROM $TABLE_TRANSACTIONS WHERE $COLUMN_USERNAME = ? " +
                    "AND strftime('%Y', $COLUMN_DATE) = ? AND strftime('%m', $COLUMN_DATE) = ?",
            arrayOf(username, year, monthStr)
        )

        while (cursor.moveToNext()) {
            val type = cursor.getString(0)
            val amount = cursor.getDouble(1)

            if (type == "Income") {
                totalIncome += amount
            } else {
                totalExpense += amount
            }
        }

        cursor.close()
        db.close()
        return Pair(totalIncome, totalExpense)
    }

    // 🔹 Get total expenses grouped by category for selected month
    fun getCategoryTotalsForMonth(username: String, year: String, month: String, type: String): List<Pair<String, Double>> {
        val db = readableDatabase
        val results = mutableListOf<Pair<String, Double>>()

        val monthStr = month.padStart(2, '0')

        val cursor = db.rawQuery(
            """
            SELECT category, SUM(amount) as total 
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_USERNAME = ? AND $COLUMN_TYPE = ? 
            AND strftime('%Y', $COLUMN_DATE) = ? AND strftime('%m', $COLUMN_DATE) = ?
            GROUP BY category
            """.trimIndent(),
            arrayOf(username, type, year, monthStr)
        )

        while (cursor.moveToNext()) {
            val category = cursor.getString(0)
            val total = cursor.getDouble(1)
            results.add(category to total)
        }

        cursor.close()
        db.close()
        return results
    }
    // 🔹 Get the day with the highest total expenses for selected month
    fun getMostExpensiveDay(username: String, year: String, month: String): String? {
        val db = readableDatabase
        val monthStr = month.padStart(2, '0')
        var result: String? = null
        var maxExpense = 0.0

        val cursor = db.rawQuery(
            """
        SELECT $COLUMN_DATE, SUM($COLUMN_AMOUNT) as total 
        FROM $TABLE_TRANSACTIONS 
        WHERE $COLUMN_USERNAME = ? AND $COLUMN_TYPE = 'Expense' 
        AND strftime('%Y', $COLUMN_DATE) = ? AND strftime('%m', $COLUMN_DATE) = ?
        GROUP BY $COLUMN_DATE 
        ORDER BY total DESC 
        LIMIT 1
        """.trimIndent(),
            arrayOf(username, year, monthStr)
        )

        if (cursor.moveToFirst()) {
            result = cursor.getString(0)  // This will be the date string like "2025-04-08"
        }

        cursor.close()
        db.close()
        return result
    }



}

// 🔹 Data Class for Transactions
data class Transaction(
    val id: Int,
    val username: String,
    val type: String,
    val amount: Double,
    val category: String,
    val date: String
)
