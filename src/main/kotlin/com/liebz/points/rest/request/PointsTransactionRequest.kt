package com.liebz.points.rest.request

import java.time.Instant
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent

data class PointsTransactionRequest(
    @field:NotBlank(message = "payer name is required")
    var payer: String?,

    @field:NotNull(message = "points is required")
    var points: Int?,

    @field:NotNull(message = "timestamp is required")
    @field:PastOrPresent(message = "timestamp cannot be in the future")
    var timestamp: Instant?
)
