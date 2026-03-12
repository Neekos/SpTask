package com.example.fragmentlesson1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VideosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideosFragment : Fragment() {

    private lateinit var videoView: VideoView
    private lateinit var videoTitle: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var videoStatus: TextView

    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_videos, container, false)

        // Инициализация View
        videoView = view.findViewById(R.id.video_view)
        videoTitle = view.findViewById(R.id.video_title)
        btnPlay = view.findViewById(R.id.btn_play)
        btnPause = view.findViewById(R.id.btn_pause)
        btnStop = view.findViewById(R.id.btn_stop)
        progressBar = view.findViewById(R.id.progress_bar)
        videoStatus = view.findViewById(R.id.video_status)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка VideoView
        setupVideoPlayer()

        // Обработчики кнопок
        btnPlay.setOnClickListener {
            playVideo()
        }

        btnPause.setOnClickListener {
            pauseVideo()
        }

        btnStop.setOnClickListener {
            stopVideo()
        }

        // Слушатель завершения видео
        videoView.setOnCompletionListener {
            videoStatus.text = "Воспроизведение завершено"
            isPlaying = false
            updateButtonStates()
        }

        // Слушатель готовности видео
        videoView.setOnPreparedListener {
            progressBar.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoStatus.text = "Видео готово к воспроизведению"
        }
    }

    /**
     * Настройка видеоплеера
     */
    private fun setupVideoPlayer() {
        videoTitle.text = "Демонстрационное видео"

        // Установка видео (локальный ресурс или URL)
        val videoPath = "android.resource://${requireContext().packageName}/${R.raw.sample_video}.mkv"
        videoView.setVideoPath(videoPath)

        // Показываем прогресс при загрузке
        progressBar.visibility = View.VISIBLE
        videoView.visibility = View.GONE

        updateButtonStates()
    }

    /**
     * Воспроизведение видео
     */
    private fun playVideo() {
        if (!isPlaying) {
            videoView.start()
            isPlaying = true
            videoStatus.text = "Воспроизводится"
            updateButtonStates()
        }
    }

    /**
     * Пауза видео
     */
    private fun pauseVideo() {
        if (isPlaying) {
            videoView.pause()
            isPlaying = false
            videoStatus.text = "На паузе"
            updateButtonStates()
        }
    }

    /**
     * Остановка видео
     */
    private fun stopVideo() {
        videoView.stopPlayback()
        isPlaying = false
        videoStatus.text = "Остановлено"
        updateButtonStates()
        // Подготовка к повторному воспроизведению
        videoView.resume()
    }

    /**
     * Обновление состояния кнопок
     */
    private fun updateButtonStates() {
        btnPlay.isEnabled = !isPlaying
        btnPause.isEnabled = isPlaying
        btnStop.isEnabled = isPlaying

        // Изменение прозрачности для визуального отображения состояния
        btnPlay.alpha = if (isPlaying) 0.5f else 1f
        btnPause.alpha = if (isPlaying) 1f else 0.5f
        btnStop.alpha = if (isPlaying) 1f else 0.5f
    }

    override fun onPause() {
        super.onPause()
        // Приостанавливаем видео при скрытии фрагмента
        if (isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Освобождаем ресурсы видеоплеера
        videoView.stopPlayback()
    }
}