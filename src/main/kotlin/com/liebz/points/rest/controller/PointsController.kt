package com.liebz.points.rest.controller

import com.liebz.points.rest.request.PointsTransactionRequest
import com.liebz.points.rest.request.SpendRequest
import com.liebz.points.rest.response.PayerSpendSummary
import com.liebz.points.rest.response.PointsTransactionResponse
import com.liebz.points.service.PointsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class PointsController(
    private val pointsService: PointsService
) {

    @GetMapping("/api/points")
    fun getAllPoints(): ResponseEntity<Map<String, Int>> {
        val payerPointsSummary = pointsService.getPointsTransactionsSummary()

        return ResponseEntity(payerPointsSummary, HttpStatus.OK)
    }

    @PostMapping("/api/points")
    fun addPoints(
        @Valid
        @RequestBody
        pointsTransactionRequest: PointsTransactionRequest
    ): ResponseEntity<PointsTransactionResponse> {
        // !! is safe here from @Valid validations on the PointsTransactionRequest
        val newPointsTransaction = pointsService.addTransactionForPayer(
            pointsTransactionRequest.payer!!,
            pointsTransactionRequest.points!!,
            pointsTransactionRequest.timestamp!!
        )

        val response = PointsTransactionResponse(
            payer = newPointsTransaction.payer,
            points = newPointsTransaction.originalBalance,
            timestamp = newPointsTransaction.timestamp
        )

        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @PostMapping("/api/spend")
    fun spendPoints(
        @Valid
        @RequestBody
        spendRequest: SpendRequest
    ): ResponseEntity<List<PayerSpendSummary>> {
        // !! is safe here from @Valid validations on the SpendRequest
        val spendSummary = pointsService.spendPoints(spendRequest.points!!)
        val response = spendSummary.map {
            PayerSpendSummary(payer = it.key, points = it.value)
        }

        return ResponseEntity(response, HttpStatus.OK)
    }

}