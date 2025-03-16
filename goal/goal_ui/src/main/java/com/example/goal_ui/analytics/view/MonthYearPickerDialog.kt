package com.example.goal_ui.analytics.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import com.example.goal_ui.R

class MonthYearPickerDialog(
    context: Context,
    private val initialYear: Int,
    private val initialMonth: Int,
    private val onDateSelected: (year: Int, month: Int) -> Unit
) : Dialog(context) {

    private lateinit var npYear: NumberPicker
    private lateinit var npMonth: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_month_year_picker)

        npYear = findViewById(R.id.npYear)
        npMonth = findViewById(R.id.npMonth)

        // Year Picker
        npYear.minValue = 2000
        npYear.maxValue = 2100
        npYear.value = initialYear

        // Month Picker
        npMonth.minValue = 1
        npMonth.maxValue = 12
        npMonth.displayedValues = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        npMonth.value = initialMonth

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            onDateSelected(npYear.value, npMonth.value)
            dismiss()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
    }
}

