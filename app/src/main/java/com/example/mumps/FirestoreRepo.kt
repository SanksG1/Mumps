package com.example.mumps

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class CheckIn(
    val id: String = "",
    val mood: Int = 3,
    val note: String = "",
    val createdAtMillis: Long = 0L
)

class FirestoreRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val checkins = db.collection("checkins")
    suspend fun addCheckIn(mood: Int, note: String) {
        val data = mapOf(
            "mood" to mood,
            "note" to note,
            "createdAt" to FieldValue.serverTimestamp()
        )
        checkins.add(data).await()
    }
    suspend fun fetchRecent(limit: Long = 5): List<CheckIn> {
        val snap = checkins
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snap.documents.map { doc ->
            val mood = (doc.getLong("mood") ?: 3L).toInt()
            val note = doc.getString("note") ?: ""
            val ts = doc.getTimestamp("createdAt") ?: Timestamp.now()
            CheckIn(
                id = doc.id,
                mood = mood,
                note = note,
                createdAtMillis = ts.toDate().time
            )
        }
    }
    suspend fun fetchMoodCountsAllTime(): Map<Int, Int> {
        val out = mutableMapOf<Int, Int>()
        for (mood in 1..5) {
            val snap = checkins
                .whereEqualTo("mood", mood)
                .get()
                .await()
            out[mood] = snap.size()
        }
        return out
    }

}
