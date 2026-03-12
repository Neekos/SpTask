package com.example.lessondialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText

// Кастомный диалог для входа в систему
// Наследуется от DialogFragment, что обеспечивает правильный жизненный цикл
// и сохранение состояния при повороте экрана
class CustomDialogFragment : DialogFragment() {

    // Интерфейс обратного вызова (callback)
    // Определяет методы, которые Activity/Fragment должны реализовать
    // для обработки событий диалога
    interface CustomDialogListener {
        // Вызывается при успешном заполнении формы входа
        fun onLoginSuccess(username: String, password: String)

        // Вызывается при отмене входа (нажата кнопка "Отмена")
        fun onLoginCancelled()

        // Вызывается при запросе регистрации (нажата кнопка "Регистрация")
        fun onRegisterRequested()
    }

    // Ссылка на слушатель событий
    // Используем nullable тип, так как слушатель может быть не установлен
    private var listener: CustomDialogListener? = null

    // Публичный метод для установки слушателя
    // Вызывается из Activity/Fragment перед показом диалога
    fun setListener(listener: CustomDialogListener) {
        this.listener = listener
    }

    // Основной метод создания диалогового окна
    // Вызывается системой при показе диалога
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЬСКОГО ИНТЕРФЕЙСА
        // Получаем LayoutInflater из Activity и надуваем кастомный макет
        // requireActivity() гарантирует, что Activity существует
        val view = requireActivity().layoutInflater
            .inflate(R.layout.fragment_custom_dialog, null)

        // 2. ПОЛУЧЕНИЕ ССЫЛОК НА ЭЛЕМЕНТЫ UI
        // Находим поля ввода по их ID в надутом макете
        val etUsername = view.findViewById<TextInputEditText>(R.id.et_username)
        val etPassword = view.findViewById<TextInputEditText>(R.id.et_password)

        // 3. СОЗДАНИЕ И НАСТРОЙКА ALERTDIALOG
        return androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Вход в систему")  // Устанавливаем заголовок диалога

            // Устанавливаем кастомный View как содержимое диалога
            .setView(view)

            // 4. КНОПКА "ВОЙТИ" (POSITIVE BUTTON)
            // Вызывается при попытке входа
            .setPositiveButton("Войти") { dialog, _ ->
                // Получаем значения из полей ввода
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                // 5. ВАЛИДАЦИЯ ВВОДА
                // Проверка 1: Оба поля должны быть заполнены
                if (username.isNotBlank() && password.isNotBlank()) {
                    // Проверка 2: Пароль должен быть не менее 6 символов
                    if (password.length >= 6) {
                        // Все проверки пройдены - успешный вход
                        listener?.onLoginSuccess(username, password)
                    } else {
                        // Пароль слишком короткий - показываем ошибку
                        Toast.makeText(
                            requireContext(),
                            "Пароль должен быть не менее 6 символов",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Возврат без закрытия диалога - пользователь может исправить ошибку
                        return@setPositiveButton
                    }
                } else {
                    // Не все поля заполнены - показываем ошибку
                    Toast.makeText(
                        requireContext(),
                        "Заполните все поля",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Возврат без закрытия диалога
                    return@setPositiveButton
                }

                // 6. ЗАКРЫТИЕ ДИАЛОГА ПРИ УСПЕШНОМ ВХОДЕ
                dialog.dismiss()
            }

            // 7. КНОПКА "ОТМЕНА" (NEGATIVE BUTTON)
            // Вызывается при отмене входа
            .setNegativeButton("Отмена") { dialog, _ ->
                // Уведомляем слушателя об отмене
                listener?.onLoginCancelled()
                // Закрываем диалог
                dialog.dismiss()
            }

            // 8. КНОПКА "РЕГИСТРАЦИЯ" (NEUTRAL BUTTON)
            // Вызывается при запросе регистрации нового пользователя
            .setNeutralButton("Регистрация") { dialog, _ ->
                // Уведомляем слушателя о запросе регистрации
                listener?.onRegisterRequested()
                // Закрываем текущий диалог (откроется диалог регистрации)
                dialog.dismiss()
            }

            // 9. СОЗДАНИЕ И ВОЗВРАТ ДИАЛОГА
            .create()
    }
}