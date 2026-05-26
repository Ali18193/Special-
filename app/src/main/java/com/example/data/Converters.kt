package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val examTopicListType = Types.newParameterizedType(List::class.java, ExamTopic::class.java)
    private val examTopicAdapter = moshi.adapter<List<ExamTopic>>(examTopicListType)

    @TypeConverter
    fun fromExamTopicList(list: List<ExamTopic>?): String? {
        return examTopicAdapter.toJson(list)
    }

    @TypeConverter
    fun toExamTopicList(json: String?): List<ExamTopic>? {
        return json?.let { examTopicAdapter.fromJson(it) }
    }
}
