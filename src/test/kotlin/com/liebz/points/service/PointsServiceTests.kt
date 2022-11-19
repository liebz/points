package com.liebz.points.service

import com.liebz.points.data.PointsTransactionRepository
import com.liebz.points.data.model.PointsTransaction
import com.liebz.points.exception.NotEnoughPointsException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.*
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PointsServiceTests {

    private val pointsTransactionRepository = mock<PointsTransactionRepository>()
    private val pointsService = PointsService(pointsTransactionRepository)

    @BeforeEach
    fun beforeEach() {
        whenever(pointsTransactionRepository.save(any())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun `Returns a summary of all the payer point totals`() {
        whenever(pointsTransactionRepository.findAllGroupedByPayer()).thenReturn(
            mutableMapOf(
                "DANNON" to mutableListOf(
                    PointsTransaction(payer = "DANNON", originalBalance = 500, timestamp = Instant.now()),
                    PointsTransaction(payer = "DANNON", originalBalance = 400, timestamp = Instant.now()),
                ),
                "UNILEVER" to mutableListOf(
                    PointsTransaction(payer = "UNILEVER", originalBalance = 700, timestamp = Instant.now()),
                    PointsTransaction(payer = "UNILEVER", originalBalance = 500, timestamp = Instant.now()),
                    PointsTransaction(payer = "UNILEVER", originalBalance = 100, timestamp = Instant.now()),
                ),
                "MILLER COORS" to mutableListOf(
                    PointsTransaction(payer = "MILLER COORS", originalBalance = 2000, timestamp = Instant.now())
                )
            )
        )

        val pointsTransactionSummary = pointsService.getPointsTransactionsSummary()

        assertEquals(900, pointsTransactionSummary["DANNON"])
        assertEquals(1300, pointsTransactionSummary["UNILEVER"])
        assertEquals(2000, pointsTransactionSummary["MILLER COORS"])
    }

    @Test
    fun `Adds a new PointTransaction with a positive points amount to the repository`() {
        val payer = "DANNON"
        val amount = 500
        val timestamp = Instant.now().minusSeconds(60)

        val resultPointsTransaction = pointsService.addTransactionForPayer(payer, amount, timestamp)

        val expectedPointsTransaction = PointsTransaction(
            payer = payer,
            originalBalance = amount,
            remainingBalance = amount,
            timestamp = timestamp
        )

        assertEquals(expectedPointsTransaction, resultPointsTransaction)
        verify(pointsTransactionRepository).save(expectedPointsTransaction)
    }

    @Test
    fun `Attempting to remove points when the payer has no PointTransactions throws a NotEnoughPointsException`() {
        val payer = "DANNON"
        val amount = -500

        whenever(pointsTransactionRepository.findByPayer(payer)).thenReturn(listOf())

        assertThrows<NotEnoughPointsException> {
            pointsService.addTransactionForPayer(payer, amount, Instant.now())
        }
    }

    @Test
    fun `Attempting to remove more points that the payer has throws a NotEnoughPointsException`() {
        val payer = "DANNON"
        val amount = -5000

        whenever(pointsTransactionRepository.findByPayer(payer)).thenReturn(
            listOf(
                PointsTransaction(payer = payer, originalBalance = 500, timestamp = Instant.now()),
                PointsTransaction(payer = payer, originalBalance = 100, timestamp = Instant.now())
            )
        )

        assertThrows<NotEnoughPointsException> {
            pointsService.addTransactionForPayer(payer, amount, Instant.now())
        }
    }

    @Test
    fun `Adding a new PointsTransaction with a negative points amount removes points amount from the oldest PointsTransaction`() {
        val payer = "DANNON"
        val amount = -200
        val timestamp = Instant.now().minusSeconds(60)

        val existingPointsTransaction_1 = PointsTransaction(payer = payer, originalBalance = 200, timestamp = Instant.now().minusSeconds(100))
        val existingPointsTransaction_2 = PointsTransaction(payer = payer, originalBalance = 500, timestamp = Instant.now().minusSeconds(30))
        val existingPointsTransaction_3 = PointsTransaction(payer = payer, originalBalance = 600, timestamp = Instant.now().minusSeconds(10))

        whenever(pointsTransactionRepository.findByPayer(payer)).thenReturn(
            listOf(
                existingPointsTransaction_3,
                existingPointsTransaction_1,
                existingPointsTransaction_2
            )
        )

        pointsService.addTransactionForPayer(payer, amount, timestamp)

        assertEquals(0, existingPointsTransaction_1.remainingBalance)
        assertEquals(existingPointsTransaction_2.originalBalance, existingPointsTransaction_2.remainingBalance)
        assertEquals(existingPointsTransaction_3.originalBalance, existingPointsTransaction_3.remainingBalance)

        val expectedPointsTransaction = PointsTransaction(
            payer = payer,
            originalBalance = amount,
            remainingBalance = 0,
            timestamp = timestamp
        )
        verify(pointsTransactionRepository).save(expectedPointsTransaction)
    }

    @Test
    fun `Adding a new PointsTransaction with a negative points amount removes points from multiple PointsTransactions starting with the oldest PointsTransaction`() {
        val payer = "DANNON"
        val amount = -1000
        val timestamp = Instant.now().minusSeconds(60)

        val existingPointsTransaction_1 = PointsTransaction(payer = payer, originalBalance = 200, timestamp = Instant.now().minusSeconds(100))
        val existingPointsTransaction_2 = PointsTransaction(payer = payer, originalBalance = 500, timestamp = Instant.now().minusSeconds(30))
        val existingPointsTransaction_3 = PointsTransaction(payer = payer, originalBalance = 600, timestamp = Instant.now().minusSeconds(10))

        whenever(pointsTransactionRepository.findByPayer(payer)).thenReturn(
            listOf(
                existingPointsTransaction_3,
                existingPointsTransaction_1,
                existingPointsTransaction_2
            )
        )

        pointsService.addTransactionForPayer(payer, amount, timestamp)

        assertEquals(0, existingPointsTransaction_1.remainingBalance)
        assertEquals(0, existingPointsTransaction_2.remainingBalance)
        assertEquals(300, existingPointsTransaction_3.remainingBalance)

        val expectedPointsTransaction = PointsTransaction(
            payer = payer,
            originalBalance = amount,
            remainingBalance = 0,
            timestamp = timestamp
        )
        verify(pointsTransactionRepository).save(expectedPointsTransaction)
    }

    @Test
    fun `Attempting to spend more points than exists throws a NotEnoughPointsException`() {
        val amount = 10000

        whenever(pointsTransactionRepository.findAll()).thenReturn(
            listOf(
                PointsTransaction(payer = "DANNON", originalBalance = 500, timestamp = Instant.now()),
                PointsTransaction(payer = "DANNON", originalBalance = 100, timestamp = Instant.now())
            )
        )

        assertThrows<NotEnoughPointsException> {
            pointsService.spendPoints(amount)
        }
    }

    @Test
    fun `Spending points removes points from the oldest PointsTransaction`() {
        val amount = 900

        val existingPointsTransaction_1 = PointsTransaction(payer = "DANNON", originalBalance = 1000, timestamp = Instant.now().minusSeconds(500))
        val existingPointsTransaction_2 = PointsTransaction(payer = "UNILEVER", originalBalance = 500, timestamp = Instant.now().minusSeconds(400))
        val existingPointsTransaction_3 = PointsTransaction(payer = "DANNON", originalBalance = 600, timestamp = Instant.now().minusSeconds(300))
        val existingPointsTransaction_4 = PointsTransaction(payer = "MILLER COORS", originalBalance = 600, timestamp = Instant.now().minusSeconds(200))

        whenever(pointsTransactionRepository.findAll()).thenReturn(
            listOf(
                existingPointsTransaction_3,
                existingPointsTransaction_4,
                existingPointsTransaction_1,
                existingPointsTransaction_2
            )
        )

        val pointsRemovalSummary = pointsService.spendPoints(amount)

        assertEquals(100, existingPointsTransaction_1.remainingBalance)
        assertEquals(existingPointsTransaction_2.originalBalance, existingPointsTransaction_2.remainingBalance)
        assertEquals(existingPointsTransaction_3.originalBalance, existingPointsTransaction_3.remainingBalance)
        assertEquals(existingPointsTransaction_4.originalBalance, existingPointsTransaction_4.remainingBalance)

        assertEquals(1, pointsRemovalSummary.size)
        assertEquals(-900, pointsRemovalSummary["DANNON"])
    }

    @Test
    fun `Spending points removes points from multiple PointsTransactions starting with the oldest PointsTransaction`() {
        val amount = 1800

        val existingPointsTransaction_1 = PointsTransaction(payer = "DANNON", originalBalance = 1000, timestamp = Instant.now().minusSeconds(500))
        val existingPointsTransaction_2 = PointsTransaction(payer = "UNILEVER", originalBalance = 500, timestamp = Instant.now().minusSeconds(400))
        val existingPointsTransaction_3 = PointsTransaction(payer = "DANNON", originalBalance = 600, timestamp = Instant.now().minusSeconds(300))
        val existingPointsTransaction_4 = PointsTransaction(payer = "MILLER COORS", originalBalance = 600, timestamp = Instant.now().minusSeconds(200))

        whenever(pointsTransactionRepository.findAll()).thenReturn(
            listOf(
                existingPointsTransaction_3,
                existingPointsTransaction_4,
                existingPointsTransaction_1,
                existingPointsTransaction_2
            )
        )

        val pointsRemovalSummary = pointsService.spendPoints(amount)

        assertEquals(0, existingPointsTransaction_1.remainingBalance)
        assertEquals(0, existingPointsTransaction_2.remainingBalance)
        assertEquals(300, existingPointsTransaction_3.remainingBalance)
        assertEquals(existingPointsTransaction_4.originalBalance, existingPointsTransaction_4.remainingBalance)

        assertEquals(2, pointsRemovalSummary.size)
        assertEquals(-1300, pointsRemovalSummary["DANNON"])
        assertEquals(-500, pointsRemovalSummary["UNILEVER"])
    }

    @Test
    fun `Spending 0 points does not update any point values`() {
        val pointsRemovalSummary = pointsService.spendPoints(0)

        assertEquals(mapOf<String, Int>(), pointsRemovalSummary)
        verifyNoInteractions(pointsTransactionRepository)
    }

    @Test
    fun `Spending negative points does not update any point values`() {
        val pointsRemovalSummary = pointsService.spendPoints(-1)

        assertEquals(mapOf<String, Int>(), pointsRemovalSummary)
        verifyNoInteractions(pointsTransactionRepository)
    }

}