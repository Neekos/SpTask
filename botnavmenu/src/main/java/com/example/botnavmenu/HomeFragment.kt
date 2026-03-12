package com.example.botnavmenu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.botnavmenu.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fileAdapter: FileAdapter
    private var selectedFile: FileExample? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createInitialFiles()
        initAdapter()
        setupButtonListeners()
        loadFiles()
        updateFileInfo()

        // Обновляем имя файла при вводе
        binding.editFileName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateButtonsState()
            }
        })
    }

    private fun createInitialFiles() {
        val testFiles = mapOf(
            "readme.txt" to "Это тестовый файл. Добро пожаловать!\nВы можете редактировать этот текст.",
            "notes.txt" to "Мои заметки:\n1. Купить молоко\n2. Позвонить маме\n3. Сделать домашку",
            "data.json" to "{\n  \"name\": \"Тестовые данные\",\n  \"value\": 123,\n  \"active\": true\n}"
        )

        testFiles.forEach { (fileName, content) ->
            if (FileExample.createFile(requireContext(), fileName)) {
                val file = FileExample.getAllFiles(requireContext())
                    .find { it.fileName == fileName }
                file?.writeContent(requireContext(), content)
            }
        }
    }

    private fun initAdapter() {
        val files = FileExample.getAllFiles(requireContext())

        fileAdapter = FileAdapter(files, requireContext()) { file ->
            selectFile(file)
        }

        binding.listContainer.layoutManager = LinearLayoutManager(context)
        binding.listContainer.adapter = fileAdapter
    }

    private fun selectFile(file: FileExample) {
        selectedFile = file
        binding.editFileName.setText(file.fileName)
        updateFileInfo()
        updateButtonsState()
        if (file.exists(requireContext())) {
            val content = file.readContent(requireContext())
            binding.textViewFile.text = content
        } else {
            binding.textViewFile.text = "Файл не существует"
        }
        Toast.makeText(context, "Выбран файл: ${file.fileName}", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {

        // Кнопка создания файла
        binding.btnCreateFile.setOnClickListener {
            val fileName = binding.editFileName.text.toString().trim()
            if (fileName.isBlank()) {
                Toast.makeText(context, "Введите имя файла", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createNewFile(fileName)
        }

        // Кнопка удаления файла
        binding.btnDeleteFile.setOnClickListener {
            selectedFile?.let { file ->
                deleteFile(file)
            } ?: run {
                Toast.makeText(context, "Сначала выберите файл", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка переименования файла
        binding.btnRenameFile.setOnClickListener {
            selectedFile?.let { file ->
                val newName = binding.editFileName.text.toString().trim()
                if (newName.isBlank()) {
                    Toast.makeText(context, "Введите новое имя файла", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                renameFile(file, newName)
            } ?: run {
                Toast.makeText(context, "Сначала выберите файл", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка чтения
        binding.btnRead.setOnClickListener {
            selectedFile?.let { file ->
                readFileContent(file)
            } ?: run {
                Toast.makeText(context, "Сначала выберите файл", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка записи
        binding.btnWrite.setOnClickListener {
            selectedFile?.let { file ->
                writeToFile(file)
            } ?: run {
                Toast.makeText(context, "Сначала выберите файл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonsState() {
        val hasSelectedFile = selectedFile != null
        val hasFileName = binding.editFileName.text.toString().trim().isNotBlank()

        // Кнопки доступны только если есть выбранный файл
        binding.btnDeleteFile.isEnabled = hasSelectedFile
        binding.btnRenameFile.isEnabled = hasSelectedFile && hasFileName
        binding.btnRead.isEnabled = hasSelectedFile
        binding.btnWrite.isEnabled = hasSelectedFile

        // Кнопка создания доступна всегда, если есть имя
        binding.btnCreateFile.isEnabled = hasFileName
    }

    private fun loadFiles() {
        val files = FileExample.getAllFiles(requireContext())
        fileAdapter.updateDataSet(files)
        updateButtonsState()
    }

    private fun updateFileInfo() {
        selectedFile?.let { file ->
            val info = """
                Файл: ${file.fileName}
                Размер: ${file.getSize(requireContext())}
                Изменен: ${file.getLastModified(requireContext())}
            """.trimIndent()

            binding.txtFileInfo.text = info
        } ?: run {
            binding.txtFileInfo.text = "Выберите файл из списка"
        }
    }

    private fun createNewFile(fileName: String) {
        if (FileExample.createFile(requireContext(), fileName)) {
            loadFiles()

            // Выбираем созданный файл
            val newFile = FileExample.getAllFiles(requireContext())
                .find { it.fileName == fileName || it.fileName == "$fileName.txt" }

            newFile?.let {
                selectFile(it)
                Toast.makeText(context, "Файл создан: ${it.fileName}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Файл с таким именем уже существует", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readFileContent(file: FileExample) {
        val content = file.readContent(requireContext())
        binding.textViewFile.text = content
        binding.inputTextFile.setText(content)

        Toast.makeText(context, "Файл прочитан: ${file.fileName}", Toast.LENGTH_SHORT).show()
        updateFileInfo()
    }

    private fun writeToFile(file: FileExample) {
        val content = binding.inputTextFile.text.toString()

        if (content.isBlank()) {
            Toast.makeText(context, "Введите текст для записи", Toast.LENGTH_SHORT).show()
            return
        }

        if (file.writeContent(requireContext(), content)) {
            binding.textViewFile.text = content
            Toast.makeText(context, "Файл сохранен: ${file.fileName}", Toast.LENGTH_SHORT).show()
            updateFileInfo()
        } else {
            Toast.makeText(context, "Ошибка записи файла", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFile(file: FileExample) {
        if (file.delete(requireContext())) {
            // Сбрасываем выбранный файл
            selectedFile = null
            binding.editFileName.setText("")
            binding.inputTextFile.setText("")
            binding.textViewFile.text = ""

            loadFiles()
            updateFileInfo()
            Toast.makeText(context, "Файл удален: ${file.fileName}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Ошибка удаления файла", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renameFile(file: FileExample, newName: String) {
        if (newName == file.fileName) {
            Toast.makeText(context, "Имя файла не изменилось", Toast.LENGTH_SHORT).show()
            return
        }

        if (file.rename(requireContext(), newName)) {
            loadFiles()

            // Обновляем выбранный файл
            val renamedFile = FileExample.getAllFiles(requireContext())
                .find { it.fileName == newName }
            selectedFile = renamedFile

            updateFileInfo()
            Toast.makeText(context, "Файл переименован", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Ошибка переименования", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}