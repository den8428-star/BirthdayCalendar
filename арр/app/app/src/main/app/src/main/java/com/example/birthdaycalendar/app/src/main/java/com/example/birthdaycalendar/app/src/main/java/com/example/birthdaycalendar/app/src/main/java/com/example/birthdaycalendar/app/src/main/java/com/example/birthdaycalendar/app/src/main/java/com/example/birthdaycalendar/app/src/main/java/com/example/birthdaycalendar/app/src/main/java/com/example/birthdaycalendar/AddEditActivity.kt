package com.example.birthdaycalendar

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class AddEditActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.get(this) }
    private var personId: Long = 0L

    private var selDay: Int = 1
    private var selMonth: Int = 1
    private var selYear: Int? = null
    private var photoPath: String? = null

    private lateinit var dateBtn: Button
    private lateinit var photoPreview: ImageView
    private lateinit var noYearCheck: CheckBox
    private lateinit var nameField: EditText
    private lateinit var relationField: EditText
    private lateinit var noteField: EditText
    private lateinit var remindField: EditText

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = copyImageToInternal(uri)
            if (path != null) {
                photoPath = path
                photoPreview.setImageURI(Uri.fromFile(File(path)))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        nameField = findViewById(R.id.fieldName)
        relationField = findViewById(R.id.fieldRelation)
        noteField = findViewById(R.id.fieldNote)
        remindField = findViewById(R.id.fieldRemind)
        dateBtn = findViewById(R.id.fieldDateButton)
        noYearCheck = findViewById(R.id.fieldNoYear)
        photoPreview = findViewById(R.id.fieldPhotoPreview)

        personId = intent.getLongExtra("person_id", 0L)

        findViewById<Button>(R.id.btnSave).setOnClickListener { savePerson() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { deletePerson() }
        findViewById<Button>(R.id.btnPickPhoto).setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        dateBtn.setOnClickListener { showDatePicker() }

        if (personId != 0L) {
            supportActionBar?.title = "Редактировать"
            loadPerson()
        } else {
            supportActionBar?.title = "Новый человек"
            findViewById<Button>(R.id.btnDelete).visibility = View.GONE
            updateDateButton()
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (selYear != null) cal.set(selYear!!, selMonth - 1, selDay)

        DatePickerDialog(
            this,
            { _, y, m, d ->
                selYear = y
                selMonth = m + 1
                selDay = d
                updateDateButton()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateButton() {
        val months = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        val yearPart = if (noYearCheck.isChecked) "" else " ${selYear ?: ""}"
        dateBtn.text = "$selDay ${months[selMonth - 1]}$yearPart"
    }

    private fun loadPerson() {
        lifecycleScope.launch {
            val p = withContext(Dispatchers.IO) { db.personDao().getById(personId) }
            p?.let {
                nameField.setText(it.name)
                relationField.setText(it.relation ?: "")
                noteField.setText(it.note ?: "")
                remindField.setText(it.remindDaysBefore.toString())
                selDay = it.day
                selMonth = it.month
                selYear = it.year
                noYearCheck.isChecked = it.year == null
                photoPath = it.photoPath
                if (it.photoPath != null && File(it.photoPath).exists()) {
                    photoPreview.setImageURI(Uri.fromFile(File(it.photoPath)))
                }
                updateDateButton()
            }
        }
    }

    private fun savePerson() {
        val name = nameField.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show()
            return
        }

        val year = if (noYearCheck.isChecked) null else selYear
        val relation = relationField.text.toString().trim().ifBlank { null }
        val note = noteField.text.toString().trim().ifBlank { null }
        val remind = remindField.text.toString().toIntOrNull()?.coerceIn(0, 60) ?: 1

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (personId == 0L) {
                    db.personDao().insert(
                        Person(
                            name = name,
                            day = selDay,
                            month = selMonth,
                            year = year,
                            relation = relation,
                            note = note,
                            photoPath = photoPath,
                            remindDaysBefore = remind
                        )
                    )
                } else {
                    val old = db.personDao().getById(personId) ?: return@withContext
                    db.personDao().update(
                        old.copy(
                            name = name,
                            day = selDay,
                            month = selMonth,
                            year = year,
                            relation = relation,
                            note = note,
                            photoPath = photoPath,
                            remindDaysBefore = remind
                        )
                    )
                }
            }
            finish()
        }
    }

    private fun deletePerson() {
        if (personId == 0L) return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val p = db.personDao().getById(personId)
                if (p != null) {
                    p.photoPath?.let { runCatching { File(it).delete() } }
                    db.personDao().delete(p)
                }
            }
            finish()
        }
    }

    private fun copyImageToInternal(uri: Uri): String? {
        return try {
            val file = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
