package com.example.myapplicationkotlin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

const val MY_VARIABLE  = 40
class MainActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var educationSpinner: Spinner
    private lateinit var sportCheckBox: CheckBox
    private lateinit var readingCheckBox: CheckBox
    private lateinit var programmingCheckBox: CheckBox
    private lateinit var musicCheckBox: CheckBox
    private lateinit var ageSeekBar: SeekBar
    private lateinit var ageValueTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pz)

        initViews()
        setupSpinner()
        setupSeekBar()
        setupSubmitButton()
    }

    private fun initViews() {
        nameEditText = findViewById(R.id.nameEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        educationSpinner = findViewById(R.id.educationSpinner)
        sportCheckBox = findViewById(R.id.sportCheckBox)
        readingCheckBox = findViewById(R.id.readingCheckBox)
        programmingCheckBox = findViewById(R.id.programmingCheckBox)
        musicCheckBox = findViewById(R.id.musicCheckBox)
        ageSeekBar = findViewById(R.id.ageSeekBar)
        ageValueTextView = findViewById(R.id.ageValueTextView)
        resultTextView = findViewById(R.id.resultTextView)
        submitButton = findViewById(R.id.submitButton)
    }
    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.education_levels,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        educationSpinner.adapter = adapter
    }
    private fun setupSeekBar() {
        ageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val age = progress + 16
                ageValueTextView.text = age.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Не требуется
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Не требуется
            }
        })
    }
    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            collectAndDisplayData()
        }
    }
    private fun collectAndDisplayData() {
        val result = buildString {
            // Name
            val name = nameEditText.text.toString().trim()
            append("Пользователь: ${if (name.isEmpty()) "Не указано" else name}\n\n")

            // Gender
            val gender = getSelectedGender()
            append("Пол: $gender\n\n")

            // Education
            val education = educationSpinner.selectedItem.toString()
            append("Образование: $education\n\n")

            // Hobbies
            val hobbies = getSelectedHobbies()
            append("Увлечения: $hobbies\n\n")

            // Age
            val age = ageSeekBar.progress + 16
            append("Возраст: $age")
        }

        resultTextView.text = result
    }
    private fun getSelectedGender(): String {
        return when (genderRadioGroup.checkedRadioButtonId) {
            R.id.maleRadioButton -> "Мужской"
            R.id.femaleRadioButton -> "Женский"
            R.id.notSpecifiedRadioButton -> "Не указывать"
            else -> "Не указан"
        }
    }
    private fun getSelectedHobbies(): String {
        val hobbies = mutableListOf<String>()
        if (sportCheckBox.isChecked) hobbies.add("Спорт")
        if (readingCheckBox.isChecked) hobbies.add("Чтение")
        if (programmingCheckBox.isChecked) hobbies.add("Программирование")
        if (musicCheckBox.isChecked) hobbies.add("Музыка")

        return if (hobbies.isEmpty()) "Не указаны" else hobbies.joinToString(", ")
    }
}