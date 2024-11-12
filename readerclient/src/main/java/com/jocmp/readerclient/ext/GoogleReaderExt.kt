package com.jocmp.readerclient.ext

import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.Stream
import com.jocmp.readerclient.SubscriptionEditAction

suspend fun GoogleReader.streamItemsIDs(
    stream: Stream,
    continuation: String? = null,
    count: Int = 10_000,
    excludedStream: Stream? = null,
) = streamItemsIDs(
    streamID = stream.id,
    continuation = continuation,
    count = count,
    excludedStreamID = excludedStream?.id
)

suspend fun GoogleReader.editSubscription(
    id: String,
    action: SubscriptionEditAction,
    addCategoryID: String? = null,
    title: String? = null,
    postToken: String?,
) = editSubscription(
    id = id,
    actionID = action.id,
    addCategoryID = addCategoryID,
    title = title,
    postToken = postToken
)
