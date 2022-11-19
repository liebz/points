package com.liebz.points.data.model

import java.time.Instant

data class PointsTransaction(
    val payer: String,
    val originalBalance: Int,
    var remainingBalance: Int = originalBalance,
    val timestamp: Instant
)
