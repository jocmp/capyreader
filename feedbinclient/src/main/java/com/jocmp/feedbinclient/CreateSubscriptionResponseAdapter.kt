package com.jocmp.feedbinclient

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal class CreateSubscriptionResponseAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        single: JsonAdapter<Subscription>,
        multiple: JsonAdapter<SubscriptionChoice>
    ): CreateSubscriptionResponse {
        return when(reader.peek()) {
            Token.BEGIN_ARRAY -> {
                val choices = mutableListOf<SubscriptionChoice>()
                reader.beginArray()
                while (reader.hasNext()) {
                    choices.add(multiple.fromJson(reader)!!)
                }
                reader.endArray()
                CreateSubscriptionResponse.MultipleChoices(choices)
            }
            else -> CreateSubscriptionResponse.Created(single.fromJson(reader)!!)
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: CreateSubscriptionResponse) {
    }
}
