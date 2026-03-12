package com.example.fragmentlesson1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PhotosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PhotosFragment : Fragment() {
    private lateinit var photoCounter: TextView
    private lateinit var photoImage: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var btnPhotoInfo: Button

    private var currentPhotoIndex = 0

    // Список фото (можно заменить на реальные URL)
    private val photoUrls = listOf(
        "https://i.playground.ru/e/4XuHTsf0bY0m4fZCI_xrAg.jpeg",
        "https://i.ytimg.com/vi/Zpvv9TdQU2k/maxresdefault.jpg",
        "https://i.ytimg.com/vi/okMp4tk-vT4/maxresdefault.jpg",
        "https://platform.theverge.com/wp-content/uploads/sites/2/chorus/uploads/chorus_asset/file/24888673/Screenshot_2023_08_31_at_2.57.29_PM.png?quality=90&strip=all&crop=0%2C3.4649279698221%2C100%2C93.070144060356&w=1200",
        "https://avatars.mds.yandex.net/i?id=e03e25830bf925341f076277c1fc63a4_l-10876589-images-thumbs&n=13"
    )

    private val photoTitles = listOf(
        "android",
        "android",
        "android",
        "android",
        "android"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Загружаем макет фрагмента
        val view = inflater.inflate(R.layout.fragment_photos, container, false)
        // Инициализация View
        photoCounter = view.findViewById(R.id.photo_counter)
        photoImage = view.findViewById(R.id.photo_image)
        btnChangePhoto = view.findViewById(R.id.btn_change_photo)
        btnPhotoInfo = view.findViewById(R.id.btn_photo_info)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка первой фото
        loadPhoto(currentPhotoIndex)

        // Обработчик кнопки смены фото
        btnChangePhoto.setOnClickListener {
            currentPhotoIndex = (currentPhotoIndex + 1) % photoUrls.size
            loadPhoto(currentPhotoIndex)
        }

        // Обработчик кнопки информации
        btnPhotoInfo.setOnClickListener {
            showPhotoInfo()
        }
    }

    /**
     * Загрузка фото по индексу
     */
    private fun loadPhoto(index: Int) {
        // Обновляем счетчик
        photoCounter.text = "Фото ${index + 1} из ${photoUrls.size}"

        // Загружаем фото с помощью Glide
        Glide.with(this)
            .load(photoUrls[index])
            .placeholder(R.drawable.placeholder_image)

            .centerCrop()
            .into(photoImage)
    }

    /**
     * Показать информацию о фото
     */
    private fun showPhotoInfo() {
        val title = photoTitles[currentPhotoIndex]

        // Создаем простой диалог
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Информация о фото")
            .setMessage("Название: $title\nРазмер: 1920x1080\nФормат: JPEG\nДата: 2024-01-15")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ресурсы при уничтожении View
    }
}