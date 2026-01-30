package com.example.mumps

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CheckIn(
    val mood: Int,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            var selectedMood by remember { mutableIntStateOf(3) } // default
            var note by remember { mutableStateOf("") }
            val recent = remember { mutableStateListOf<CheckIn>() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Quick Check-in",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mood (1–5): $selectedMood/5",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..5).forEach { mood ->
                        Button(
                            onClick = { selectedMood = mood },
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                        ) {
                            Text(text = mood.toString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val trimmed = note.trim()
                        val checkIn = CheckIn(mood = selectedMood, note = trimmed)
                        recent.add(0, checkIn) // newest first

                        // keep only last 5 for now
                        while (recent.size > 5) recent.removeAt(recent.size - 1)

                        note = ""
                        Toast.makeText(context, "Saved locally ✅", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Recent check-ins (latest 5)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (recent.isEmpty()) {
                    Text("No check-ins yet. Add one above.")
                } else {
                    recent.forEach { item ->
                        val time = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                            .format(Date(item.createdAt))

                        Text("• Mood ${item.mood}/5 — ${item.note.ifBlank { "(no note)" }} — $time")
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
