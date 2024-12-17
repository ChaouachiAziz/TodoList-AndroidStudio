package com.example.finalmobileproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.finalmobileproject.Model.TaskModel;
import com.example.finalmobileproject.Utils.DataBaseHelper;
import com.example.finalmobileproject.interfaces.OnDialogCloseListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskDetailsActivity extends AppCompatActivity implements OnDialogCloseListener {

    private Button completeButton;
    private Button setReminderButton;
    private Button shareButton;
    private Button editButton;
    private Button deleteButton;
    private DataBaseHelper myDB;
    private TextView titleTextView, descriptionTextView, deadlineTextView, reminderTextView;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        // Initialize database helper
        myDB = new DataBaseHelper(this);

        // Initialize views
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        reminderTextView = findViewById(R.id.reminderTextView);

        // Retrieve task ID from the intent
        taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Invalid task ID", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no valid ID is provided
            return;
        }


        // Load task details
        loadTaskDetails();

        // Initialize the complete button
        completeButton = findViewById(R.id.completeButton);
        // Set the complete button action
        completeButton.setOnClickListener(v -> {
            markTaskAsComplete();
        });

        // Initialize the set reminder button
        setReminderButton = findViewById(R.id.setReminderButton);
        // Set reminder button action
        setReminderButton.setOnClickListener(v -> {
            setReminder();
        });

        // Initialize share button
        shareButton = findViewById(R.id.shareButton);
        // Set share button action
        shareButton.setOnClickListener(v -> {
            shareTask();
        });

        // Initialize the edit button
        editButton = findViewById(R.id.editButton);
        // Set edit button action
        editButton.setOnClickListener(v -> {
            editTask();
        });

        // Initialize delete button
        deleteButton = findViewById(R.id.deleteButton);
        // Set delete button action
        deleteButton.setOnClickListener(v -> {
            deleteTask();
        });

    }
    private void loadTaskDetails() {
        // Fetch the task from the database
        TaskModel task = myDB.getTaskById(taskId);
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the task doesn't exist
            return;
        }

        // Update UI with task details
        titleTextView.setText(task.getTitre());
        descriptionTextView.setText(task.getDescription());
        deadlineTextView.setText("Date d’échéance: " + new SimpleDateFormat("dd-MM-yyyy").format(task.getDate_echeance()));
        reminderTextView.setText("Nombre de Rappels: " + task.getRappel());
    }

    private void markTaskAsComplete() {
        if (taskId != 0) {
            myDB.updateStatut(taskId, 1); // Mark as completed (status 1)
            Toast.makeText(this, "Task marked as complete", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Task already done", Toast.LENGTH_SHORT).show();
        }
        loadTaskDetails();
    }

    private void setReminder() {
        if (taskId != -1) {
            // Open a date and time picker to set the reminder (use your existing reminder logic)
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    // Schedule the reminder notification
                    scheduleNotification(calendar.getTimeInMillis());

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        loadTaskDetails();
    }

    private void scheduleNotification(long triggerTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(triggerTime);

        // Extract hour and minute
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create the intent
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "N'oubliez pas de compléter cette tâche!");
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false); // Show alarm UI to the user

        // Verify that there is an app to handle the alarm intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No app available to set an alarm", Toast.LENGTH_SHORT).show();
        }
    }


    private void shareTask() {
        if (taskId != -1) {
            TaskModel task = myDB.getTaskById(taskId);
            if (task != null) {
                String shareMessage = "Task: " + task.getTitre() + "\n" +
                        "Description: " + task.getDescription() + "\n" +
                        "Deadline: " + new SimpleDateFormat("dd-MM-yyyy").format(task.getDate_echeance());

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Task Details");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Share Task"));
            }
        }
        loadTaskDetails();
    }

    private void editTask() {
        if (taskId != -1) {
            TaskModel task = myDB.getTaskById(taskId);
            if (task != null) {
                // Pass the task details to the AddNewTask fragment
                Bundle bundle = new Bundle();
                bundle.putInt("id", task.getId());
                bundle.putString("titre", task.getTitre());
                bundle.putString("description", task.getDescription());
                bundle.putLong("date_echeance", task.getDate_echeance().getTime());
                bundle.putInt("rappel", task.getRappel());

                AddNewTask addNewTaskFragment = new AddNewTask();
                addNewTaskFragment.setArguments(bundle);
                addNewTaskFragment.show(getSupportFragmentManager(), addNewTaskFragment.getTag());
            }
        }
        loadTaskDetails();
    }

    private void deleteTask() {
        if (taskId != -1) {
            myDB.deleteTask(taskId);
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }
        loadTaskDetails();
    }
    // Update the task list after closing the dialog
    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        loadTaskDetails();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadTaskDetails();
    }
}