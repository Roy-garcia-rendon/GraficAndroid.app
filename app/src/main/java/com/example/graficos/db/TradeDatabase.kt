package com.example.graficos.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.graficos.data.TradeResult
import java.text.SimpleDateFormat
import java.util.*

class TradeDatabase(context: Context) : SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "trades.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TRADES = "trades"
        
        private const val COLUMN_ID = "id"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_PROFIT = "profit"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_TRADES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TYPE TEXT,
                $COLUMN_AMOUNT DOUBLE,
                $COLUMN_PRICE DOUBLE,
                $COLUMN_DATE TEXT,
                $COLUMN_PROFIT DOUBLE
            )
        """.trimIndent()
        
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRADES")
        onCreate(db)
    }

    fun saveTrade(trade: TradeResult) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TYPE, trade.type)
            put(COLUMN_AMOUNT, trade.amount)
            put(COLUMN_PRICE, trade.price)
            put(COLUMN_DATE, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(trade.date)))
            put(COLUMN_PROFIT, trade.profit)
        }
        db.insert(TABLE_TRADES, null, values)
        db.close()
    }

    fun getAllTrades(): List<TradeResult> {
        val trades = mutableListOf<TradeResult>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TRADES, null, null, null, null, null, "$COLUMN_DATE DESC")

        with(cursor) {
            while (moveToNext()) {
                val trade = TradeResult(
                    type = getString(getColumnIndexOrThrow(COLUMN_TYPE)),
                    price = getDouble(getColumnIndexOrThrow(COLUMN_PRICE)),
                    amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(getString(getColumnIndexOrThrow(COLUMN_DATE)))?.time ?: 0L,
                    profit = getDouble(getColumnIndexOrThrow(COLUMN_PROFIT))
                )
                trades.add(trade)
            }
        }
        cursor.close()
        db.close()
        return trades
    }
} 