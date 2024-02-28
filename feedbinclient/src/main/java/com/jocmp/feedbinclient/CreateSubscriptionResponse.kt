package com.jocmp.feedbinclient

sealed class CreateSubscriptionResponse {
    class Created(val subscription: Subscription): CreateSubscriptionResponse()

    class MultipleChoices(val choices: List<SubscriptionChoice>): CreateSubscriptionResponse()
}
