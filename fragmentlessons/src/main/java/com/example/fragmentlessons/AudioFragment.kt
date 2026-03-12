package com.example.fragmentlessons

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fragmentlessons.databinding.FragmentAudioBinding
import java.util.concurrent.TimeUnit

class AudioFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentAudioBinding? = null
    private val binding get() = _binding!!

    // MediaPlayer для воспроизведения аудио
    private var mediaPlayer: MediaPlayer? = null

    // Handler для обновления UI
    private val handler = Handler(Looper.getMainLooper())

    // Флаг для отслеживания взаимодействия с SeekBar
    private var isUserSeeking = false

    // Адаптер для списка аудиофайлов (теперь из отдельного файла)
    private lateinit var audioAdapter: AudioAdapter

    // Список аудиофайлов
    private val audioFiles = mutableListOf<AudioFile>()

    // Текущий воспроизводимый файл
    private var currentAudioFile: AudioFile? = null

    // Launcher для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        handlePermissionResult(isGranted)
    }

    // Runnable для обновления SeekBar
    private val updateSeekBar = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 1000) // Обновляем каждую секунду
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupPlayer()
        setupControls()
        loadAudioFiles()
    }

    private fun setupRecyclerView() {
        // Инициализируем адаптер из отдельного файла
        audioAdapter = AudioAdapter { audioFile ->
            playAudioFile(audioFile)
        }

        binding.recyclerViewAudio.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun setupPlayer() {
        mediaPlayer = MediaPlayer()

        mediaPlayer?.setOnPreparedListener {
            // Аудио готово к воспроизведению
            binding.seekBar.max = mediaPlayer!!.duration
            updateTotalTime(mediaPlayer!!.duration)

            // Начинаем воспроизведение
            mediaPlayer!!.start()
            updatePlayButtons(true)
            handler.post(updateSeekBar)

            Toast.makeText(requireContext(), "Воспроизведение начато", Toast.LENGTH_SHORT).show()
        }

        mediaPlayer?.setOnCompletionListener {
            // Аудио завершило воспроизведение
            updatePlayButtons(false)
            binding.seekBar.progress = 0
            updateCurrentTime(0)
            handler.removeCallbacks(updateSeekBar)
        }

        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Toast.makeText(requireContext(), "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
            Log.e("AudioFragment", "MediaPlayer error: what=$what, extra=$extra")
            true
        }
    }

    private fun setupControls() {
        // Кнопка Play
        binding.btnPlay.setOnClickListener {
            handlePlayClick()
        }

        // Кнопка Pause
        binding.btnPause.setOnClickListener {
            handlePauseClick()
        }

        // Кнопка Stop
        binding.btnStop.setOnClickListener {
            handleStopClick()
        }

        // SeekBar
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateCurrentTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                isUserSeeking = false
                seekBar?.progress?.let { progress ->
                    mediaPlayer?.seekTo(progress)
                }
            }
        })
    }

    private fun loadAudioFiles() {
        audioFiles.clear()

        // 1. Загружаем файлы из res/raw (автоматически)
        loadAudioFromRawResources()

        // 2. Если есть разрешения, загружаем из медиатеки
        if (hasStoragePermissions()) {
            loadAudioFromMediaStore()
        } else {
            showPermissionHint()
        }

        // Обновляем адаптер
        audioAdapter.submitList(audioFiles.toList())

        // Обновляем заголовок
        updateTitle()
    }

    private fun updateTitle() {
        val rawCount = audioFiles.count { it.sourceType == AudioSource.RAW }
        val externalCount = audioFiles.count { it.sourceType == AudioSource.EXTERNAL }

        binding.tvTitle.text = if (externalCount > 0) {
            "Аудио плеер ($rawCount демо + $externalCount с устройства)"
        } else {
            "Аудио плеер ($rawCount треков)"
        }
    }

    private fun loadAudioFromRawResources() {
        // Список демо-файлов из res/raw
        val rawAudioFiles = listOf(
            AudioFile(
                id = 1,
                displayName = "Демо песня 1",
                fileName = "demo_song_1",
                sourceType = AudioSource.RAW,
                uri = Uri.parse("android.resource://${requireContext().packageName}/raw/demo_song_1"),
            ),
            AudioFile(
                id = 2,
                displayName = "Демо песня 2",
                fileName = "demo_song_2",
                sourceType = AudioSource.RAW,
                uri = Uri.parse("android.resource://${requireContext().packageName}/raw/demo_song_2")
            ),
            AudioFile(
                id = 3,
                displayName = "Демо песня 3",
                fileName = "demo_song_3",
                sourceType = AudioSource.RAW,
                uri = Uri.parse("android.resource://${requireContext().packageName}/raw/demo_song_3"),

            )
        )

        // Проверяем существование файлов
        rawAudioFiles.forEach { audioFile ->
            try {
                val resourceId = resources.getIdentifier(audioFile.fileName, "raw", requireContext().packageName)
                if (resourceId != 0) {
                    audioFiles.add(audioFile)
                }
            } catch (e: Exception) {
                Log.e("AudioFragment", "File not found: ${audioFile.fileName}")
            }
        }

        if (audioFiles.isEmpty()) {
            // Если нет демо-файлов, показываем сообщение
            binding.tvTrackName.text = "Добавьте mp3 файлы в папку res/raw"
        }
    }

    /**
     * Форматирует имя файла для отображения
     * Убирает расширение, заменяет подчеркивания на пробелы
     */
    private fun formatDisplayName(fileName: String): String {
        // Убираем расширение
        var displayName = fileName
        listOf(".mp3", ".wav", ".ogg", ".m4a", ".flac", ".aac", ".wma")
            .forEach { ext ->
                if (displayName.lowercase().endsWith(ext)) {
                    displayName = displayName.substring(0, displayName.length - ext.length)
                }
            }

        // Заменяем подчеркивания и дефисы на пробелы
        displayName = displayName
            .replace("_", " ")
            .replace("-", " ")
            .replace(".", " ")

        // Делаем первую букву заглавной для каждого слова
        return displayName.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.first().uppercase() + word.drop(1).lowercase()
            } else {
                word
            }
        }.trim()
    }

    @SuppressLint("Range")
    private fun loadAudioFromMediaStore() {
        // Этот метод загружает музыку из медиатеки устройства
        // Для работы нужны разрешения

        try {
            // Для всех версий Android используем MediaStore
            // Настройки запроса могут отличаться для разных версий

            val projection = arrayOf(
                android.provider.MediaStore.Audio.Media._ID,
                android.provider.MediaStore.Audio.Media.DISPLAY_NAME,
                android.provider.MediaStore.Audio.Media.TITLE,
                android.provider.MediaStore.Audio.Media.ARTIST,
                android.provider.MediaStore.Audio.Media.ALBUM,
                android.provider.MediaStore.Audio.Media.DATE_ADDED
            )

            // Отбираем только музыкальные файлы
            val selection = "${android.provider.MediaStore.Audio.Media.IS_MUSIC} != 0"

            // Сортируем по дате добавления (новые сначала)
            val sortOrder = "${android.provider.MediaStore.Audio.Media.DATE_ADDED} DESC"

            // Для разных версий Android могут быть разные URI
            val audioUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.provider.MediaStore.Audio.Media.getContentUri(
                    android.provider.MediaStore.VOLUME_EXTERNAL
                )
            } else {
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            requireContext().contentResolver.query(
                audioUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(cursor.getColumnIndex(
                            android.provider.MediaStore.Audio.Media._ID
                        ))

                        val fileName = cursor.getString(cursor.getColumnIndex(
                            android.provider.MediaStore.Audio.Media.DISPLAY_NAME
                        )) ?: "unknown"

                        val title = cursor.getString(cursor.getColumnIndex(
                            android.provider.MediaStore.Audio.Media.TITLE
                        )) ?: formatDisplayName(fileName)

                        val artist = cursor.getString(cursor.getColumnIndex(
                            android.provider.MediaStore.Audio.Media.ARTIST
                        ))

                        // Формируем отображаемое имя
                        val displayName = if (!artist.isNullOrEmpty()) {
                            "$artist - $title"
                        } else {
                            title
                        }

                        // Создаем URI для доступа к файлу
                        val contentUri = android.content.ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        // Создаем AudioFile
                        val audioFile = AudioFile(
                            id,
                            displayName,
                            fileName,
                            contentUri,
                            AudioSource.EXTERNAL
                        )

                        audioFiles.add(audioFile)

                        Log.d("AudioFragment", "Loaded from MediaStore: $displayName")

                    } catch (e: Exception) {
                        Log.e("AudioFragment", "Error processing MediaStore entry", e)
                    }
                }

                // Логируем количество загруженных файлов
                Log.i("AudioFragment", "Loaded ${audioFiles.size} files from MediaStore")

            } ?: run {
                Log.e("AudioFragment", "Cursor is null for MediaStore query")
            }

        } catch (e: SecurityException) {
            // Нет разрешений
            Log.e("AudioFragment", "SecurityException: ${e.message}")
            Toast.makeText(
                requireContext(),
                "Нет разрешений для доступа к медиатеке",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Log.e("AudioFragment", "Error loading from MediaStore", e)
            Toast.makeText(
                requireContext(),
                "Ошибка загрузки музыки с устройства",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 и ниже
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Показываем объяснение перед запросом
        if (shouldShowRequestPermissionRationale(permission)) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Нужно разрешение")
                .setMessage("Для доступа к вашей музыке нужно дать разрешение на чтение аудиофайлов")
                .setPositiveButton("OK") { _, _ ->
                    requestPermissionLauncher.launch(permission)
                }
                .setNegativeButton("Отмена", null)
                .show()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            Toast.makeText(requireContext(), "Разрешение получено!", Toast.LENGTH_SHORT).show()
            loadAudioFiles()
        } else {
            Toast.makeText(
                requireContext(),
                "Разрешение не получено. Будут доступны только демо-файлы",
                Toast.LENGTH_LONG
            ).show()
            showPermissionHint()
        }
    }

    private fun showPermissionHint() {
        if (audioFiles.isEmpty()) {
            // Нет вообще никаких файлов
            binding.tvTrackName.text = "Нет доступных треков. Дайте разрешения для доступа к музыке."

            // Добавляем кнопку запроса разрешений (временно в заголовке)
            binding.tvTitle.setOnClickListener {
                requestStoragePermission()
            }
            binding.tvTitle.text = "Аудио плеер (нажмите для разрешений)"
        } else if (audioFiles.all { it.sourceType == AudioSource.RAW }) {
            // Есть только демо-файлы
            binding.tvTitle.text = "Аудио плеер (только демо)"
            binding.tvTitle.setOnClickListener {
                requestStoragePermission()
            }
        }
    }

    private fun playAudioFile(audioFile: AudioFile) {
        try {
            // Останавливаем текущее воспроизведение
            mediaPlayer?.reset()

            currentAudioFile = audioFile

            // Устанавливаем источник аудио
            when (audioFile.sourceType) {
                AudioSource.RAW -> {
                    val resourceId = resources.getIdentifier(
                        audioFile.fileName,
                        "raw",
                        requireContext().packageName
                    )
                    if (resourceId != 0) {
                        val assetFileDescriptor = resources.openRawResourceFd(resourceId)
                        mediaPlayer?.setDataSource(
                            assetFileDescriptor.fileDescriptor,
                            assetFileDescriptor.startOffset,
                            assetFileDescriptor.length
                        )
                        assetFileDescriptor.close()
                    } else {
                        throw IllegalArgumentException("Файл не найден: ${audioFile.fileName}")
                    }
                }
                AudioSource.EXTERNAL -> {
                    audioFile.uri?.let { uri ->
                        mediaPlayer?.setDataSource(requireContext(), uri)
                    } ?: throw IllegalArgumentException("URI не указан")
                }
            }

            // Обновляем UI
            binding.tvTrackName.text = audioFile.displayName
            binding.tvTrackName.isSelected = true // Включаем бегущую строку

            updatePlayButtons(false)

            // Готовим асинхронно
            mediaPlayer?.prepareAsync()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("AudioFragment", "Error playing audio", e)
        }
    }

    private fun handlePlayClick() {
        currentAudioFile?.let { audioFile ->
            if (mediaPlayer?.isPlaying == true) {
                // Если уже играет, ничего не делаем
                return@let
            }

            if (mediaPlayer?.currentPosition ?: 0 > 0) {
                // Возобновляем воспроизведение
                mediaPlayer?.start()
                updatePlayButtons(true)
                handler.post(updateSeekBar)
            } else {
                // Начинаем воспроизведение сначала
                playAudioFile(audioFile)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Сначала выберите трек", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePauseClick() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            updatePlayButtons(false)
            handler.removeCallbacks(updateSeekBar)
        }
    }

    private fun handleStopClick() {
        mediaPlayer?.stop()
        updatePlayButtons(false)
        binding.seekBar.progress = 0
        updateCurrentTime(0)
        handler.removeCallbacks(updateSeekBar)

        // Сбрасываем плеер для следующего воспроизведения
        try {
            mediaPlayer?.reset()
        } catch (e: Exception) {
            // Игнорируем ошибки при сбросе
        }
    }

    private fun updatePlayButtons(isPlaying: Boolean) {
        binding.btnPlay.isEnabled = !isPlaying
        binding.btnPause.isEnabled = isPlaying
        binding.btnStop.isEnabled = true
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying && !isUserSeeking) {
                val currentPosition = player.currentPosition
                binding.seekBar.progress = currentPosition
                updateCurrentTime(currentPosition)
            }
        }
    }

    private fun updateCurrentTime(milliseconds: Int) {
        val formattedTime = formatTime(milliseconds)
        binding.tvCurrentTime.text = formattedTime
    }

    private fun updateTotalTime(milliseconds: Int) {
        val formattedTime = formatTime(milliseconds)
        binding.tvTotalTime.text = formattedTime
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        handler.removeCallbacks(updateSeekBar)
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer?.isPlaying == true) {
            handler.post(updateSeekBar)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateSeekBar)
        _binding = null
    }
}