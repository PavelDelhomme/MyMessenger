package com.delhomme.mymessenger.domain

import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity

object SpamFilter {
    private val spamPatterns = listOf(
        Regex("\\b(?:gagnez|gagner|prix|offre|promotion|urgent|important)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(?:cliquez|ici|lien|http|https|www\\.)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(?:gratuit|free|cadeau|bonus)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(?:bitcoin|crypto|investissement)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(?:prêt|crédit|argent rapide)\\b", RegexOption.IGNORE_CASE)
    )
    private val spamNumbers = mutableSetOf<String>()

    private val spamCache = mutableMapOf<String, Boolean>()

    @Synchronized
    fun isSpam(message: MessageEntity): Boolean {
        return spamPatterns.any { it.containsMatchIn(message.body) } ||
                spamNumbers.contains(message.address)
    }

    fun reportSpam(number: String) {
        spamNumbers.add(number)
    }

    fun removeFromSpam(number: String) {
        spamNumbers.remove(number)
    }

    fun clearCache() {
        spamCache.clear()
    }

    fun filterSpam(messages: List<MessageEntity>): List<MessageEntity> {
        return messages.filterNot { isSpam(it) }
    }
}