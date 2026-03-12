package com.example.lessondialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tv_result)

        // 1. Простой диалог
        findViewById<Button>(R.id.btn_simple_dialog).setOnClickListener {
            showSimpleDialog()
        }

        // 2. Диалог с выбором
        findViewById<Button>(R.id.btn_selection_dialog).setOnClickListener {
            showSelectionDialog()
        }

        // 3. Диалог с вводом текста
        findViewById<Button>(R.id.btn_input_dialog).setOnClickListener {
            showInputDialog()
        }

        // 4. Диалог подтверждения
        findViewById<Button>(R.id.btn_confirm_dialog).setOnClickListener {
            showConfirmDialog()
        }

        // 5. Прогресс-диалог
        findViewById<Button>(R.id.btn_progress_dialog).setOnClickListener {
            showProgressDialog()
        }

        // 6. DatePickerDialog
        findViewById<Button>(R.id.btn_date_dialog).setOnClickListener {
            showDatePickerDialog()
        }

        // 7. BottomSheetDialog
        findViewById<Button>(R.id.btn_bottom_sheet).setOnClickListener {
            showBottomSheetDialog()
        }

        // 8. Кастомный DialogFragment
        findViewById<Button>(R.id.btn_custom_dialog).setOnClickListener {
            showCustomDialogFragment()
        }
    }

    // ================== 1. ПРОСТОЙ ДИАЛОГ ==================
    private fun showSimpleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Простое сообщение")
            .setMessage("Это самый простой AlertDialog.\nОн только показывает информацию.")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK") { dialog, _ ->
                updateResult("Нажали: OK (Простой диалог)")
                dialog.dismiss()
            }
            .setNeutralButton("Детали") { dialog, _ ->
                updateResult("Нажали: Детали")
                // Диалог остается открытым
            }
            .setCancelable(true)  // Можно закрыть кнопкой Назад
            .show()
    }

    // ================== 2. ДИАЛОГ ВЫБОРА ==================
    private fun showSelectionDialog() {
        val colors = arrayOf("Красный", "Зеленый", "Синий", "Желтый", "Черный")

        AlertDialog.Builder(this)
            .setTitle("Выберите цвет")
            .setSingleChoiceItems(colors, 2) { dialog, which ->
                val selectedColor = colors[which]
                updateResult("Выбран цвет: $selectedColor")
                // Не закрываем сразу - ждем кнопку OK
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                updateResult("Выбор отменен")
                dialog.dismiss()
            }
            .show()
    }

    // ================== 3. ДИАЛОГ С ВВОДОМ ТЕКСТА ==================
    private fun showInputDialog() {
        val input = EditText(this).apply {
            hint = "Введите ваше имя"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Ввод данных")
            .setMessage("Как вас зовут?")
            .setView(input)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    updateResult("Привет, $name!")
                } else {
                    updateResult("Имя не введено")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                updateResult("Ввод отменен")
                dialog.dismiss()
            }
            .show()
    }

    // ================== 4. ДИАЛОГ ПОДТВЕРЖДЕНИЯ ==================
    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение действия")
            .setMessage("Вы действительно хотите удалить этот файл?\nЭто действие нельзя отменить.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Да, удалить") { dialog, _ ->
                // Имитация удаления
                Handler(Looper.getMainLooper()).postDelayed({
                    updateResult("Файл успешно удален")
                }, 500)
                dialog.dismiss()
            }
            .setNegativeButton("Нет, оставить") { dialog, _ ->
                updateResult("Файл не удален")
                dialog.dismiss()
            }
            .setNeutralButton("Перенести в корзину") { dialog, _ ->
                updateResult("Файл перемещен в корзину")
                dialog.dismiss()
            }
            .show()
    }

    // ================== 5. ПРОГРЕСС-ДИАЛОГ ==================
    private fun showProgressDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Загрузка данных")
            .setMessage("Пожалуйста, подождите...")
            .setCancelable(false)  // Нельзя закрыть кнопкой Назад
            .create()

        dialog.show()

        // Имитация загрузки
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            updateResult("Загрузка завершена успешно!")
        }, 3000)

        // Можно менять сообщение во время загрузки
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.setMessage("Обработка данных...")
        }, 1000)

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.setMessage("Сохранение результатов...")
        }, 2000)
    }

    // ================== 6. DATEPICKER DIALOG ==================
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "${selectedDay}.${selectedMonth + 1}.${selectedYear}"
                updateResult("Выбрана дата: $date")
            },
            year, month, day
        )

        // Устанавливаем минимальную дату (сегодня)
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        // Устанавливаем максимальную дату (через год)
        calendar.add(Calendar.YEAR, 1)
        datePicker.datePicker.maxDate = calendar.timeInMillis

        datePicker.show()
    }

    // ================== 7. BOTTOMSHEET DIALOG ==================
    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)

        // Создаем кастомный View
        val view = layoutInflater.inflate(R.layout.bottom_sheet_content, null)

        // Находим элементы
        view.findViewById<Button>(R.id.btn_option1).setOnClickListener {
            updateResult("Выбрано: Опция 1")
            bottomSheetDialog.dismiss()
        }

        view.findViewById<Button>(R.id.btn_option2).setOnClickListener {
            updateResult("Выбрано: Опция 2")
            bottomSheetDialog.dismiss()
        }

        view.findViewById<Button>(R.id.btn_option3).setOnClickListener {
            updateResult("Выбрано: Опция 3")
            bottomSheetDialog.dismiss()
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            updateResult("BottomSheet отменен")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    // ================== 8. КАСТОМНЫЙ DIALOGFRAGMENT ==================
    private fun showCustomDialogFragment() {
        val dialog = CustomDialogFragment().apply {
            setListener(object : CustomDialogFragment.CustomDialogListener {
                override fun onLoginSuccess(username: String, password: String) {
                    updateResult("Вход выполнен: $username")
                }

                override fun onLoginCancelled() {
                    updateResult("Вход отменен")
                }

                override fun onRegisterRequested() {
                    updateResult("Запрос регистрации")
                    // Можно показать другой диалог
                }
            })
        }

        dialog.show(supportFragmentManager, "custom_dialog")
    }

    // ================== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ==================
    private fun updateResult(message: String) {
        tvResult.text = message
        // Красим текст в зависимости от типа сообщения
        when {
            message.contains("ошибк", ignoreCase = true) -> {
                tvResult.setTextColor(getColor(android.R.color.holo_red_dark))
            }
            message.contains("успеш", ignoreCase = true) -> {
                tvResult.setTextColor(getColor(android.R.color.holo_green_dark))
            }
            else -> {
                tvResult.setTextColor(getColor(android.R.color.white))
            }
        }
    }
}