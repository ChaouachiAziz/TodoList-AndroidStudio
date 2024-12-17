package com.example.finalmobileproject;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalmobileproject.Model.TaskModel;
import com.example.finalmobileproject.Utils.DataBaseHelper;
import com.example.finalmobileproject.interfaces.OnDialogCloseListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNewTask extends BottomSheetDialogFragment {
    public static final String TAG = "AddNewTask";

    // Widgets
    private EditText mEditTitre;
    private EditText mEditDescription;
    private EditText mEditDate;
    private Button mSaveButton;

    private DataBaseHelper myDb;

    //Open Dialog
    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_task, container, false);
    }

    //User Interaction
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditTitre = view.findViewById(R.id.edit_titre);
        mEditDescription = view.findViewById(R.id.edit_description);
        mEditDate = view.findViewById(R.id.edit_date);
        mSaveButton = view.findViewById(R.id.button_save);

        myDb = new DataBaseHelper(getActivity());

        // Disable Save button initially
        mSaveButton.setEnabled(false);
        mSaveButton.setBackgroundColor(Color.GRAY);

        // Handle Date Selection
        mEditDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
                        mEditDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Handle Updates
        final Bundle bundle = getArguments();
        if (bundle != null) {
            // Editing an existing task: Load original values
            String originalTitle = bundle.getString("titre", "");
            String originalDescription = bundle.getString("description", "");
            String originalDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date(bundle.getLong("date_echeance")));
            mEditTitre.setText(originalTitle);
            mEditDescription.setText(originalDescription);
            mEditDate.setText(originalDate);

            // Add a TextWatcher for each field to enable the Save button when changes occur
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Enable the Save button only if title or description changes
                    String currentTitle = mEditTitre.getText().toString();
                    String currentDescription = mEditDescription.getText().toString();
                    String currentDate = mEditDate.getText().toString();

                    boolean isChanged = !currentTitle.equals(originalTitle)
                            || !currentDescription.equals(originalDescription)
                            || !currentDate.equals(originalDate);

                    mSaveButton.setEnabled(isChanged);
                    mSaveButton.setBackgroundColor(isChanged ? getResources().getColor(R.color.colorPrimary) : Color.GRAY);
                }

                @Override
                public void afterTextChanged(Editable s) { }
            };

            // Attach the TextWatcher to title and description fields
            mEditTitre.addTextChangedListener(textWatcher);
            mEditDescription.addTextChangedListener(textWatcher);
            mEditDate.addTextChangedListener(textWatcher);
        } else {
            // Handle creation logic (unchanged)
            mEditTitre.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean isEnabled = !s.toString().isEmpty();
                    mSaveButton.setEnabled(isEnabled);
                    mSaveButton.setBackgroundColor(isEnabled ? getResources().getColor(R.color.colorPrimary) : Color.GRAY);
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        // Save Task
        mSaveButton.setOnClickListener(v -> {
            String titre = mEditTitre.getText().toString();
            String description = mEditDescription.getText().toString();
            String date = mEditDate.getText().toString();

            if (bundle != null) {
                // Update Existing Task
                myDb.updateTitre(bundle.getInt("id"), titre);
                myDb.updateDescription(bundle.getInt("id"), description);
                myDb.updateDateEcheance(bundle.getInt("id"), date);
            } else {
                // Insert New Task
                TaskModel item = new TaskModel();
                item.setTitre(titre);
                item.setDescription(description);
                try {
                    item.setDate_echeance(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(date));
                } catch (Exception e) {
                    item.setDate_echeance(new Date());
                }
                item.setStatut(0); // Default Status
                myDb.insertTask(item);
            }

            // Notify the activity that the dialog is closed, and pass updated data.
            if(getActivity() instanceof OnDialogCloseListener){
                ((OnDialogCloseListener) getActivity()).onDialogClose(getDialog());
            }

            dismiss();
        });
    }

    //Close Dialog
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}