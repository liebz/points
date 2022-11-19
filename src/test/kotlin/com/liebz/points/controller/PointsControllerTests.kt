package com.liebz.points.controller

import com.liebz.points.data.model.PointsTransaction
import com.liebz.points.rest.controller.PointsController
import com.liebz.points.rest.request.PointsTransactionRequest
import com.liebz.points.rest.request.SpendRequest
import com.liebz.points.rest.response.PayerSpendSummary
import com.liebz.points.service.PointsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant

class PointsControllerTests {

    private val pointsService = mock<PointsService>()
    private val pointsController = PointsController(pointsService)

    @Test
    fun `Get all points returns the payer points summary`() {
        val dannonPayer = "DANNON"
        val unileverPayer = "UNILEVER"
        val pointsSummary = mapOf(
            dannonPayer to 500,
            unileverPayer to 1000
        )

        whenever(pointsService.getPointsTransactionsSummary()).thenReturn(pointsSummary)

        val pointsSummaryResponse = pointsController.getAllPoints()

        verify(pointsService).getPointsTransactionsSummary()
        assertEquals(HttpStatus.OK, pointsSummaryResponse.statusCode)
        assertEquals(pointsSummary[dannonPayer], pointsSummaryResponse.body?.get(dannonPayer))
        assertEquals(pointsSummary[unileverPayer], pointsSummaryResponse.body?.get(unileverPayer))
    }

    @Test
    fun `Adding points returns the correct PointsTransactionResponse`() {
        val payer = "DANNON"
        val points = 500
        val timestamp = Instant.now()

        whenever(pointsService.addTransactionForPayer(payer, points, timestamp)).thenReturn(
            PointsTransaction(
                payer = payer,
                originalBalance = points,
                timestamp = timestamp
            )
        )

        val pointsTransactionResponse = pointsController.addPoints(
            PointsTransactionRequest(
                payer = payer,
                points = points,
                timestamp = timestamp
            )
        )

        assertEquals(HttpStatus.CREATED, pointsTransactionResponse.statusCode)
        assertEquals(payer, pointsTransactionResponse.body?.payer)
        assertEquals(points, pointsTransactionResponse.body?.points)
        assertEquals(timestamp, pointsTransactionResponse.body?.timestamp)
    }

    @Test
    fun `Spending points returns the correct PayerSpendSummary`() {
        val dannonPayer = "DANNON"
        val unileverPayer = "UNILEVER"
        val points = 1000
        val spendPointsSummary = mapOf(
            dannonPayer to -400,
            unileverPayer to -600
        )

        whenever(pointsService.spendPoints(points)).thenReturn(spendPointsSummary)

        val spendPointsResponse = pointsController.spendPoints(SpendRequest(points))

        assertEquals(HttpStatus.OK, spendPointsResponse.statusCode)
        spendPointsResponse.body?.contains(PayerSpendSummary(dannonPayer, -400))?.let { assertTrue(it) }
        spendPointsResponse.body?.contains(PayerSpendSummary(unileverPayer, -600))?.let { assertTrue(it) }
    }

}