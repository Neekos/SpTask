package com.example.fragmentlessons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fragmentlessons.databinding.ItemAudioBinding
import java.util.concurrent.TimeUnit

class AudioAdapter(
    private val onItemClick: (AudioFile) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var audioFiles = listOf<AudioFile>()

    /**
     * Обновляет список аудиофайлов в адаптере
     */
    fun submitList(newList: List<AudioFile>) {
        audioFiles = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioFiles[position])
    }

    override fun getItemCount(): Int = audioFiles.size

    /**
     * ViewHolder для отображения элемента аудиофайла
     */
    inner class AudioViewHolder(
        private val binding: ItemAudioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(audioFile: AudioFile) {
            binding.apply {
                // Устанавливаем название трека
                textAudioTitle.text = audioFile.displayName

                // Показываем источник файла
                textAudioSource.text = when (audioFile.sourceType) {
                    AudioSource.RAW -> "[Демо]"
                    AudioSource.EXTERNAL -> "[Устройство]"
                }

                // Устанавливаем иконку в зависимости от источника
                val iconRes = when (audioFile.sourceType) {
                    AudioSource.RAW -> R.drawable.ic_audio
                    AudioSource.EXTERNAL -> R.drawable.ic_audio
                }
                imageAudioIcon.setImageResource(iconRes)

                // Обработка клика на элемент
                root.setOnClickListener {
                    onItemClick(audioFile)
                }
            }
        }
    }
}