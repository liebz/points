package com.liebz.points.data

import com.liebz.points.data.model.PointsTransaction
import org.springframework.stereotype.Component

/**
 * Attempting to mimic a JpaRepository that would normally be used with a durable data store
 */
@Component
class PointsTransactionRepository {

    private val payerTransactionsStore = mutableMapOf<String, MutableList<PointsTransaction>>()

    fun findAllGroupedByPayer() = payerTransactionsStore

    fun findAll() = payerTransactionsStore.values.flatten()

    fun findByPayer(payer: String): List<PointsTransaction> {
        return payerTransactionsStore[payer] ?: listOf()
    }

    fun save(pointsTransaction: PointsTransaction): PointsTransaction {
        payerTransactionsStore[pointsTransaction.payer]?.add(pointsTransaction)
            ?: run { payerTransactionsStore[pointsTransaction.payer] = mutableListOf(pointsTransaction) }

        return pointsTransaction
    }

}