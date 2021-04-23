//package com.bjorn.vanctrl
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import android.util.Log
//
///**
// * Created by Eyehunt Team on 07/06/18.
// */
//class DatabaseHandler(context: Context) :
//    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
//
//    override fun onCreate(db: SQLiteDatabase?) {
//        val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME " +
//                "(IN_1_A FLOAT, IN_1_U FLOAT, IN_2_A FLOAT, IN_2_U FLOAT, IN_3_A FLOAT, IN_3_U FLOAT, IN_4 FLOAT, IN_5 FLOAT, TIME TIMESTAMP)"
//
//        db?.execSQL(CREATE_TABLE)
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        // Called when the database needs to be upgraded
//    }
//
//    //Inserting (Creating) data
//    fun addUser(user: Users): Boolean {
//        //Create and/or open a database that will be used for reading and writing.
//        val db = this.writableDatabase
//        val values = ContentValues()
//        values.put(FIRST_NAME, user.firstName)
//        values.put(LAST_NAME, user.lastName)
//        val _success = db.insert(TABLE_NAME, null, values)
//        db.close()
//        Log.v("InsertedID", "$_success")
//        return (Integer.parseInt("$_success") != -1)
//    }
//
//    fun addMeasurement(measurement: Map<String, String>) {
//        val db = this.writableDatabase
//        val values = ContentValues()
//        for ((key, value) in measurement) {
//            values.put(key, value)
//        }
//        db.insert(TABLE_NAME, null, values)
//        db.close()
//    }
//
//    fun getLastTimestamp(): String {
//        val db = readableDatabase
//        val selectQuery = "SELECT MAX(TIME) AS T FROM $TABLE_NAME"
//        val cursor = db.rawQuery(selectQuery, null)
//        var t = "0"
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                t = cursor.getString(cursor.getColumnIndex("T"))
//            }
//        }
//        cursor.close()
//        db.close()
//        return t
//    }
//
//    //get all users
//    fun getAllUsers(): String {
//        var allUser: String = "";
//        val db = readableDatabase
//        val selectALLQuery = "SELECT * FROM $TABLE_NAME"
//        val cursor = db.rawQuery(selectALLQuery, null)
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    var id = cursor.getString(cursor.getColumnIndex(ID))
//                    var firstName = cursor.getString(cursor.getColumnIndex(FIRST_NAME))
//                    var lastName = cursor.getString(cursor.getColumnIndex(LAST_NAME))
//
//                    allUser = "$allUser\n$id $firstName $lastName"
//                } while (cursor.moveToNext())
//            }
//        }
//        cursor.close()
//        db.close()
//        return allUser
//    }
//
//    companion object {
//        private val DB_NAME = "vancontrol"
//        private val DB_VERSION = 1;
//        private val TABLE_NAME = "users"
//    }
//}