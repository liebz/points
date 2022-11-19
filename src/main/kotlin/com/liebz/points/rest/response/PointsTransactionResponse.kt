package com.liebz.points.rest.response

import java.time.Instant

data class PointsTransactionResponse(
    val payer: String,
    val points: Int,
    val timestamp: Instant
)
