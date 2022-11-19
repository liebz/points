package com.liebz.points.data

import com.liebz.points.data.model.PointsTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class PointsTransactionRepositoryTests {

    private val pointsTransactionRepository = PointsTransactionRepository()

    @Test
    fun `Saves multiple PointTransactions with the correct payer`() {
        val dannonPayer = "DANNON"
        val unileverPayer = "UNILEVER"

        val dannonTransaction_1 = PointsTransaction(
            payer = dannonPayer,
            originalBalance = 100,
            timestamp = Instant.now().minusSeconds(20)
        )
        val dannonTransaction_2 = PointsTransaction(
            payer = dannonPayer,
            originalBalance = 200,
            timestamp = Instant.now().minusSeconds(10)
        )
        val unileverTransaction = PointsTransaction(
            payer = unileverPayer,
            originalBalance = 500,
            timestamp = Instant.now().minusSeconds(5)
        )

        pointsTransactionRepository.save(dannonTransaction_1)
        pointsTransactionRepository.save(dannonTransaction_2)
        pointsTransactionRepository.save(unileverTransaction)

        val dannonTransactions = pointsTransactionRepository.findByPayer(dannonPayer)
        val unileverTransactions = pointsTransactionRepository.findByPayer(unileverPayer)

        assertEquals(2, dannonTransactions.size)
        assertEquals(1, unileverTransactions.size)
    }

}