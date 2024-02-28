package com.jocmp.feedbinclient

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CreateSubscriptionResponseAdapterTest {
    private lateinit var jsonAdapter: JsonAdapter<CreateSubscriptionResponse>

    @Before
    fun before() {
        val moshi = Moshi.Builder()
            .add(CreateSubscriptionResponseAdapter())
            .build()
        jsonAdapter = moshi.adapter(CreateSubscriptionResponse::class.java)
    }

    @Test
    fun fromJson_deserializesSingleResult() {
        val json = """
            {
                "id": 4105850,
                "created_at": "2017-10-28T14:30:39.324314Z",
                "feed_id": 838741,
                "title": "Daring Fireball",
                "feed_url": "https://daringfireball.net/feeds/main",
                "site_url": "https://daringfireball.net/"
            }
        """.trimIndent()

        val result = jsonAdapter.fromJson(json) as CreateSubscriptionResponse.Created

        val expected = Subscription(
            id = 4105850L,
            created_at = "2017-10-28T14:30:39.324314Z",
            feed_id = 838741,
            title = "Daring Fireball",
            feed_url = "https://daringfireball.net/feeds/main",
            site_url = "https://daringfireball.net/"
        )

        assertEquals(expected = expected, actual = result.subscription)
    }

    @Test
    fun fromJson_deserializesMultipleResult() {
        val json = """
            [
              {
                "feed_url": "https://github.com/blog.atom",
                "title": "The GitHub Blog"
              },
              {
                "feed_url": "https://github.com/blog/broadcasts.atom",
                "title": "The GitHub Blog (Broadcasts)"
              }
            ]
        """.trimIndent()

        val result = jsonAdapter.fromJson(json) as CreateSubscriptionResponse.MultipleChoices

        val expected = listOf(
            SubscriptionChoice(
                feed_url = "https://github.com/blog.atom",
                title = "The GitHub Blog",
            ),
            SubscriptionChoice(
                feed_url = "https://github.com/blog/broadcasts.atom",
                title = "The GitHub Blog (Broadcasts)",
            )
        )

        assertEquals(expected = expected, actual = result.choices)
    }
}
