package com.liebz.points.service

import com.liebz.points.data.PointsTransactionRepository
import com.liebz.points.data.model.PointsTransaction
import com.liebz.points.exception.NotEnoughPointsException
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.math.absoluteValue

@Service
class PointsService(
    private val pointsTransactionRepository: PointsTransactionRepository
) {

    fun getPointsTransactionsSummary(): Map<String, Int> {
        val pointsTransactionsByPayer = pointsTransactionRepository.findAllGroupedByPayer()

        return pointsTransactionsByPayer.mapValues { pointsTransactions ->
            pointsTransactions.value.sumOf { it.remainingBalance }
        }
    }

    fun addTransactionForPayer(payer: String, points: Int, timestamp: Instant): PointsTransaction {
        var pointsTransaction = PointsTransaction(
            payer = payer.toUpperCase(),
            originalBalance = points,
            remainingBalance = points,
            timestamp = timestamp
        )

        if (points < 0) {
            pointsTransaction = pointsTransaction.copy(remainingBalance = 0)
            val payerPointsTransactions = pointsTransactionRepository.findByPayer(payer.toUpperCase())
            removePointsFromPointsTransactions(payerPointsTransactions, points.absoluteValue)
        }

        return pointsTransactionRepository.save(pointsTransaction)
    }

    fun spendPoints(points: Int): Map<String, Int> {
        if (points < 1) {
            return mapOf()
        }
        val allPointsTransactions = pointsTransactionRepository.findAll()

        return removePointsFromPointsTransactions(allPointsTransactions, points)
    }

    private fun removePointsFromPointsTransactions(pointsTransactions: List<PointsTransaction>, pointsToRemove: Int): Map<String, Int> {
        val pointsRemovalSummary = mutableMapOf<String, Int>()
        val totalPoints = pointsTransactions.sumOf { it.remainingBalance }

        if (totalPoints < pointsToRemove) {
            throw NotEnoughPointsException(totalPoints, pointsToRemove)
        }

        val sortedPointsTransactions = pointsTransactions
            .filter { it.remainingBalance > 0 }
            .sortedBy { it.timestamp }
        var remainingPointsToRemove = pointsToRemove

        loop@ for (oldestPointsTransaction in sortedPointsTransactions) {
            val currentPayer = oldestPointsTransaction.payer
            val pointsRemovedFromPayer = pointsRemovalSummary[currentPayer] ?: 0

            if (oldestPointsTransaction.remainingBalance >= remainingPointsToRemove) {
                pointsRemovalSummary[currentPayer] = pointsRemovedFromPayer - remainingPointsToRemove
                oldestPointsTransaction.remainingBalance -= remainingPointsToRemove
                break@loop
            } else {
                pointsRemovalSummary[currentPayer] = pointsRemovedFromPayer - oldestPointsTransaction.remainingBalance
                remainingPointsToRemove -= oldestPointsTransaction.remainingBalance
                oldestPointsTransaction.remainingBalance = 0
            }
        }

        return pointsRemovalSummary
    }

}