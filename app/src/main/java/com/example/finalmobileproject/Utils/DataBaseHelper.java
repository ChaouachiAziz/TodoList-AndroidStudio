package com.example.finalmobileproject.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


import com.example.finalmobileproject.Model.TaskModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataBaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    private static final String DATABASE_NAME = "GestionDesTache.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TITRE = "titre";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE_ECHEANCE = "dateEcheance";
    private static final String COL_STATUT = "statut";
    private static final String COL_RAPPEL = "rappel";

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //DataBase Initialization
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_TITRE + " TEXT, " +
                        COL_DESCRIPTION + " TEXT, " +
                        COL_DATE_ECHEANCE + " TEXT, " +
                        COL_STATUT + " INTEGER, " +
                        COL_RAPPEL + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //CRUD Operations
    public void insertTask(TaskModel task) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITRE, task.getTitre());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DATE_ECHEANCE, dateFormat.format(task.getDate_echeance()));
        values.put(COL_STATUT, task.getStatut());
        values.put(COL_RAPPEL, task.getRappel());
        db.insert(TABLE_NAME, null, values);
    }

    public void updateTitre(int taskId, String newTitre) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITRE, newTitre);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateDescription(int taskId, String newDescription) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DESCRIPTION, newDescription);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateDateEcheance(int taskId, String newDate) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE_ECHEANCE, newDate);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateStatut(int taskId, int newStatus) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUT, newStatus);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateRappel(int taskId, int newRappel) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RAPPEL, newRappel);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void deleteTask(int taskId) {
        db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    //Transaction Management
    @SuppressLint("Range")
    public List<TaskModel> getAllTasks() {
        db = this.getWritableDatabase();
        Cursor cursor = null;
        List<TaskModel> tasksList = new ArrayList<>();

        db.beginTransaction();
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    TaskModel task = new TaskModel();
                    task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                    task.setTitre(cursor.getString(cursor.getColumnIndex(COL_TITRE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
                    // Parse Date
                    String dateString = cursor.getString(cursor.getColumnIndex(COL_DATE_ECHEANCE));
                    try {
                        task.setDate_echeance(dateFormat.parse(dateString)); // Convert back to Date
                    } catch (ParseException e) {
                        task.setDate_echeance(new Date()); // Fallback to current date
                    }
                    task.setStatut(cursor.getInt(cursor.getColumnIndex(COL_STATUT)));
                    task.setRappel(cursor.getInt(cursor.getColumnIndex(COL_RAPPEL)));
                    tasksList.add(task);
                } while (cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (cursor != null) cursor.close();
        }
        return tasksList;
    }

    @SuppressLint("Range")
    public TaskModel getTaskById(int taskId) {
        db = this.getReadableDatabase();
        Cursor cursor = null;
        TaskModel task = null;

        try {
            // Query to fetch the task with the specified ID
            cursor = db.query(
                    TABLE_NAME,                 // Table name
                    null,                       // Columns (null = all columns)
                    COL_ID + " = ?",             // WHERE clause
                    new String[]{String.valueOf(taskId)}, // WHERE arguments
                    null,                       // Group by
                    null,                       // Having
                    null                        // Order by
            );

            // Check if the cursor has a result
            if (cursor != null && cursor.moveToFirst()) {
                task = new TaskModel();
                task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                task.setTitre(cursor.getString(cursor.getColumnIndex(COL_TITRE)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));

                // Parse Date
                String dateString = cursor.getString(cursor.getColumnIndex(COL_DATE_ECHEANCE));
                try {
                    task.setDate_echeance(dateFormat.parse(dateString));
                } catch (ParseException e) {
                    task.setDate_echeance(new Date()); // Fallback to current date
                }

                task.setStatut(cursor.getInt(cursor.getColumnIndex(COL_STATUT)));
                task.setRappel(cursor.getInt(cursor.getColumnIndex(COL_RAPPEL)));
            }
        } finally {
            if (cursor != null) cursor.close(); // Close the cursor to prevent memory leaks
        }

        return task;
    }

}
