package com.example.birthdaycalendar

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class BirthdayWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val dao = AppDatabase.get(applicationContext).personDao()
        val people = runBlocking { dao.getAll() }

        people.forEach { p ->
            val days = DateUtils.daysUntil(p.day, p.month).toInt()
            if (days == 0) {
                val age = DateUtils.ageTurning(p.year, p.day, p.month)
                val text = buildString {
                    append(p.name)
                    if (age != null) append(" — исполняется $age")
                    if (!p.note.isNullOrBlank()) append("\n${p.note}")
                }
                NotificationHelper.show(
                    applicationContext,
                    p.id.toInt(),
                    "🎂 Сегодня день рождения",
                    text
                )
            }
        }
        return Result.success()
    }
}
