package com.example.mumps

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val repo = remember { FirestoreRepo() }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            var selectedMood by remember { mutableIntStateOf(3) }
            var note by remember { mutableStateOf("") }
            val recent = remember { mutableStateListOf<CheckIn>() }

            //Hist value
            var moodCounts by remember { mutableStateOf(listOf(0, 0, 0, 0, 0)) }
            val totalCheckIns = moodCounts.sum()

            LaunchedEffect(Unit) {
                val items = repo.fetchRecent(5)
                recent.clear()
                recent.addAll(items)

                val countsMap = repo.fetchMoodCountsAllTime()
                moodCounts = (1..5).map { countsMap[it] ?: 0 }
            }

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
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMood == mood)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
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

                        scope.launch {
                            repo.addCheckIn(mood = selectedMood, note = trimmed)

                            val items = repo.fetchRecent(5)
                            recent.clear()
                            recent.addAll(items)

                            val countsMap = repo.fetchMoodCountsAllTime()
                            moodCounts = (1..5).map { countsMap[it] ?: 0 }

                            note = ""
                            Toast.makeText(context, "Saved to Firestore", Toast.LENGTH_SHORT).show()
                        }
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
                            .format(Date(item.createdAtMillis))
                        Text("• Mood ${item.mood}/5 — ${item.note.ifBlank { "(no note)" }} — $time")
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Mood histogram (all check-ins) — Total: $totalCheckIns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )


                Spacer(modifier = Modifier.height(8.dp))

                MoodHistogram(counts = moodCounts)
            }
        }
    }
}

@Composable
fun MoodHistogram(counts: List<Int>) {
    val safeCounts = if (counts.size >= 5) counts.take(5) else listOf(0, 0, 0, 0, 0)
    val max = (safeCounts.maxOrNull() ?: 0).coerceAtLeast(1)

    Text(
        text = "Scale: 0 – $max",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    for (mood in 1..5) {
        val count = safeCounts[mood - 1]
        val fraction = count.toFloat() / max.toFloat()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mood.toString(),
                modifier = Modifier.padding(end = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(18.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }

            Text(
                text = "  $count",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}
