package com.liebz.points.exception

class NotEnoughPointsException(existingPoints: Int, requiredPoints: Int): RuntimeException(
    "Unable to remove $requiredPoints points when only $existingPoints points are present"
)