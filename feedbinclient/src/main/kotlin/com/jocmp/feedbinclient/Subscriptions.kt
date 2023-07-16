package com.jocmp.feedbinclient

import com.jocmp.feedbinclient.api.FeedbinClient
import com.jocmp.feedbinclient.api.FeedbinSubscription
import com.jocmp.feedbinclient.common.request
import com.jocmp.feedbinclient.db.FeedbinDatabase
import retrofit2.Response

interface Subscriptions {
    suspend fun all(): Result<List<Subscription>>
}

class DefaultSubscriptions(
    private val database: FeedbinDatabase,
    private val client: FeedbinClient,
) : Subscriptions {
    override suspend fun all(): Result<List<Subscription>> {
        val subscriptions = allSubscriptions()

        if (subscriptions.isNotEmpty()) {
            return Result.success(subscriptions)
        }
//
//        if (campuses != null && isFreshCache(campuses.created_at)) {
//            return Result.success(convertFromLocations(moshi, campuses))
//        }
//
        return request { client.subscriptions() }.fold(
            onSuccess = { cacheAndFind(it) },
            onFailure = { Result.failure(it) }
        )
    }

    private fun cacheAndFind(result: Response<List<FeedbinSubscription>>): Result<List<Subscription>> {
        val feedbinSubscriptions = result.body().orEmpty()

        database.transaction {
            feedbinSubscriptions.forEach { subscription ->
                table.insert(
                    id = subscription.id.toLong(),
                    created_at = subscription.created_at,
                    feed_id = subscription.feed_id.toLong(),
                    feed_url = subscription.feed_url,
                    site_url = subscription.site_url
                )
            }
        }

        return try {
            Result.success(allSubscriptions())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun allSubscriptions() = table.all().executeAsList()

    private val table: SubscriptionQueries
        get() = database.subscriptionQueries
}

//private val CampusSearchResult.toDomainModel: List<Location>
//    get() = locationSearchDetails.map { locationDetail ->
//        Location(
//            id = locationDetail.location_id!!.toString(),
//            name = locationDetail.location_name ?: "",
//            mediaMediumURL = optionalURL(locationDetail.media_medium_url),
//            mediaLargeURL = optionalURL(locationDetail.media_large_url),
//        )
//    }.sortedBy { it.id }
