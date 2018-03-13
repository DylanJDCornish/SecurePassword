package com.password.dylan.cet324password;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dylan on 16/01/2017.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "PasswordDB";

    public static final String TABLE_PASSWORD = "passwordCS";

    // Places Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SALT = "salt";


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create places table
        String CREATE_PASSWORD_TABLE = "CREATE TABLE passwordCS ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT," +
                "password TEXT," +
                "salt BYTE )";

        // create password table
        db.execSQL(CREATE_PASSWORD_TABLE);

        db.execSQL("INSERT into passwordCS VALUES (null, 'user', 'password', '')");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older places table if existed
        db.execSQL("DROP TABLE IF EXISTS passwordCS");

        // create fresh places table
        this.onCreate(db);
    }

    public Cursor fetchPassword() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor mCursor = db.query(TABLE_PASSWORD, new String[] {KEY_ID, KEY_USERNAME, KEY_PASSWORD, KEY_SALT},
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }
}
