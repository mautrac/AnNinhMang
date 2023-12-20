package com.example.message.sqlUtils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import javax.crypto.SecretKey


object FeedReaderContract {
    // Table contents are grouped together in an anonymous object.
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "AES_TABLE"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_AES_KEY = "aes_key"
    }
}

class DbHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE IF NOT EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${FeedReaderContract.FeedEntry.COLUMN_USER_ID} TEXT," +
                "${FeedReaderContract.FeedEntry.COLUMN_AES_KEY} TEXT)"

    private val SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"
    }

    fun addNewKey(userId: String, key: SecretKey) {
        // Gets the data repository in write mode
        val db = this.writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_USER_ID, userId)
            put(FeedReaderContract.FeedEntry.COLUMN_AES_KEY, key.toString())
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db?.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

        db.close();
    }

    fun getKeyByUser(userId: String): String? {
        val db = this.writableDatabase
        val selection = "${FeedReaderContract.FeedEntry.COLUMN_USER_ID} = ?"
        val selectionArgs = arrayOf(userId)
        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )
        val res: String = cursor.getString(2)
        cursor.close()
        return res
    }
}