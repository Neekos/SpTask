package com.example.fragmentlessons

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.fragmentlessons.databinding.FragmentVideoBinding
import kotlin.getValue
import androidx.core.net.toUri


class VideoFragment : Fragment() {
    private val dataModel: DataModel by activityViewModels()
    lateinit var binding: FragmentVideoBinding
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка VideoView
        setupVideoPlayer()

        // Обработчики кнопок
        binding.btnPlay.setOnClickListener {
            playVideo()
        }

        binding.btnPause.setOnClickListener {
            pauseVideo()
        }

        binding.btnStop.setOnClickListener {
            stopVideo()
        }

        // Слушатель завершения видео
        binding.videoView.setOnCompletionListener {
            binding.videoStatus.text = "Воспроизведение завершено"
            isPlaying = false
            updateButtonStates()
        }

        // Слушатель готовности видео
        binding.videoView.setOnPreparedListener {
            binding.progressBar.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            binding.videoStatus.text = "Видео готово к воспроизведению"
        }
    }
    /**
     * Настройка видеоплеера
     */
    private fun setupVideoPlayer() {
        binding.videoTitle.text = "Демонстрационное видео"

        // Установка видео (локальный ресурс или URL)
        val videoPath = ("android.resource://" +
                requireContext().packageName + "/" + R.raw.sample_video).toUri()
        binding.videoView.setVideoURI(videoPath)

        // Показываем прогресс при загрузке
        binding.progressBar.visibility = View.VISIBLE
        binding.videoView.visibility = View.GONE

        updateButtonStates()
    }

    /**
     * Воспроизведение видео
     */
    private fun playVideo() {
        if (!isPlaying) {
            binding.videoView.start()
            isPlaying = true
            binding.videoStatus.text = "Воспроизводится"
            updateButtonStates()
        }
    }

    /**
     * Пауза видео
     */
    private fun pauseVideo() {
        if (isPlaying) {
            binding.videoView.pause()
            isPlaying = false
            binding.videoStatus.text = "На паузе"
            updateButtonStates()
        }
    }

    /**
     * Остановка видео
     */
    private fun stopVideo() {
        binding.videoView.stopPlayback()
        isPlaying = false
        binding.videoStatus.text = "Остановлено"
        updateButtonStates()
        // Подготовка к повторному воспроизведению
        binding.videoView.resume()
    }

    /**
     * Обновление состояния кнопок
     */
    private fun updateButtonStates() {
        binding.btnPlay.isEnabled = !isPlaying
        binding.btnPause.isEnabled = isPlaying
        binding.btnStop.isEnabled = isPlaying

        // Изменение прозрачности для визуального отображения состояния
        binding.btnPlay.alpha = if (isPlaying) 0.5f else 1f
        binding.btnPause.alpha = if (isPlaying) 1f else 0.5f
        binding.btnStop.alpha = if (isPlaying) 1f else 0.5f
    }

    override fun onPause() {
        super.onPause()
        // Приостанавливаем видео при скрытии фрагмента
        if (isPlaying) {
            binding.videoView.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Освобождаем ресурсы видеоплеера
        binding.videoView.stopPlayback()
    }
}