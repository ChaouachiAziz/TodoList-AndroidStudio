package com.example.finalmobileproject;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalmobileproject.Adapter.TaskAdapter;
import com.example.finalmobileproject.AddNewTask;
import com.example.finalmobileproject.Model.TaskModel;
import com.example.finalmobileproject.R;
import com.example.finalmobileproject.RecyclerViewTouchHelper;
import com.example.finalmobileproject.Utils.DataBaseHelper;
import com.example.finalmobileproject.interfaces.OnDialogCloseListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    private RecyclerView mRecyclerview;
    private FloatingActionButton fab;
    private DataBaseHelper myDB;

    private List<TaskModel> tasksList;
    private TaskAdapter adapter;

    private SearchView searchView;

    // Activity Initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerview = findViewById(R.id.recyclerview);
        fab = findViewById(R.id.fab);
        myDB = new DataBaseHelper(MainActivity.this);
        tasksList = new ArrayList<>();
        adapter = new TaskAdapter(myDB, MainActivity.this);

        // RecyclerView Setup
        mRecyclerview.setHasFixedSize(true);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerview.setAdapter(adapter);

        // Load initial tasks
        loadTasks();

        // FloatingActionButton Click
        fab.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        // Swipe gestures on tasks
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerview);

        // Initialize SearchView
        searchView = findViewById(R.id.searchView);
        setupSearchView();
    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }
    private void loadTasks(){
        tasksList = myDB.getAllTasks();
        Collections.reverse(tasksList);
        adapter.setTasks(tasksList);
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        loadTasks();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
        adapter.notifyDataSetChanged();
    }
}