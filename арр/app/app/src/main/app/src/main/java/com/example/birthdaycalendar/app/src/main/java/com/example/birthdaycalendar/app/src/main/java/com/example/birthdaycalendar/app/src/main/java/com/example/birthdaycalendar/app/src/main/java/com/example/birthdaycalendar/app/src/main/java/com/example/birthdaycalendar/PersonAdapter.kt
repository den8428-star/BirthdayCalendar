package com.example.birthdaycalendar

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PersonAdapter(
    private val onClick: (Person) -> Unit
) : ListAdapter<Person, PersonAdapter.VH>(DIFF) {

    companion object {
        private val MONTHS_GEN = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )

        private val DIFF = object : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(a: Person, b: Person) = a.id == b.id
            override fun areContentsTheSame(a: Person, b: Person) = a == b
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.itemPhoto)
        val name: TextView = view.findViewById(R.id.itemName)
        val date: TextView = view.findViewById(R.id.itemDate)
        val note: TextView = view.findViewById(R.id.itemNote)
        val daysCount: TextView = view.findViewById(R.id.itemDaysCount)
        val daysLabel: TextView = view.findViewById(R.id.itemDaysLabel)
        val card: View = view.findViewById(R.id.itemCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)

        holder.name.text = p.name

        val dateStr = buildString {
            append("${p.day} ${MONTHS_GEN[p.month - 1]}")
            if (p.year != null) append(" ${p.year}")
            val age = DateUtils.ageTurning(p.year, p.day, p.month)
            if (age != null) append(" · исполнится $age")
            if (!p.relation.isNullOrBlank()) append(" · ${p.relation}")
        }
        holder.date.text = dateStr

        if (!p.note.isNullOrBlank()) {
            holder.note.visibility = View.VISIBLE
            holder.note.text = p.note
        } else {
            holder.note.visibility = View.GONE
        }

        val days = DateUtils.daysUntil(p.day, p.month).toInt()
        when (days) {
            0 -> {
                holder.daysCount.text = "🎉"
                holder.daysLabel.text = "сегодня"
            }
            1 -> {
                holder.daysCount.text = "1"
                holder.daysLabel.text = "завтра"
            }
            else -> {
                holder.daysCount.text = days.toString()
                holder.daysLabel.text = pluralDays(days)
            }
        }

        if (p.photoPath != null && File(p.photoPath).exists()) {
            holder.photo.setImageURI(Uri.fromFile(File(p.photoPath)))
        } else {
            holder.photo.setImageResource(R.drawable.ic_person_placeholder)
        }

        holder.card.setOnClickListener { onClick(p) }
    }

    private fun pluralDays(n: Int): String {
        val n10 = n % 10
        val n100 = n % 100
        return when {
            n10 == 1 && n100 != 11 -> "день"
            n10 in 2..4 && (n100 < 10 || n100 >= 20) -> "дня"
            else -> "дней"
        }
    }
}
