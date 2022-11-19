package com.liebz.points.rest.request

import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class SpendRequest(
    @field:NotNull(message = "points is required")
    @field:Positive(message = "points must be a positive value")
    var points: Int?
)
