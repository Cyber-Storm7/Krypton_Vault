package com.example.autofill

import android.app.assist.AssistStructure
import android.view.View
import android.view.autofill.AutofillId

data class ParsedAutofillStructure(
    val domain: String? = null,
    val packageName: String? = null,
    val usernameNodeId: AutofillId? = null,
    val passwordNodeId: AutofillId? = null,
    val creditCardNodeId: AutofillId? = null,
    val detectedSubmittedUsername: String? = null,
    val detectedSubmittedPassword: String? = null
)

/**
 * Recursively parses incoming Android AssistStructure to extract web domain,
 * package name, autofill IDs for username/password fields, and submitted values.
 */
object StructureParser {

    fun parse(structure: AssistStructure): ParsedAutofillStructure {
        var detectedDomain: String? = null
        val detectedPackageName = structure.activityComponent?.packageName
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null
        var cardId: AutofillId? = null
        var submittedUser: String? = null
        var submittedPass: String? = null

        val nodeQueue = ArrayDeque<AssistStructure.ViewNode>()
        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            nodeQueue.add(windowNode.rootViewNode)
        }

        while (nodeQueue.isNotEmpty()) {
            val node = nodeQueue.removeFirst()

            if (detectedDomain == null && !node.webDomain.isNullOrBlank()) {
                detectedDomain = node.webDomain
            }

            val hints = node.autofillHints
            val idEntry = node.idEntry?.lowercase() ?: ""
            val className = node.className?.lowercase() ?: ""
            val text = node.text?.toString()

            val isPasswordType = node.inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD != 0 ||
                    node.inputType and android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD != 0 ||
                    idEntry.contains("password") || idEntry.contains("pass") || idEntry.contains("pwd")

            val isUsernameType = idEntry.contains("user") || idEntry.contains("email") ||
                    idEntry.contains("login") || idEntry.contains("account")

            if (hints != null) {
                for (hint in hints) {
                    when (hint) {
                        View.AUTOFILL_HINT_PASSWORD -> {
                            passwordId = node.autofillId
                            if (!text.isNullOrBlank()) submittedPass = text
                        }
                        View.AUTOFILL_HINT_USERNAME, View.AUTOFILL_HINT_EMAIL_ADDRESS -> {
                            usernameId = node.autofillId
                            if (!text.isNullOrBlank()) submittedUser = text
                        }
                        View.AUTOFILL_HINT_CREDIT_CARD_NUMBER -> {
                            cardId = node.autofillId
                        }
                    }
                }
            }

            if (passwordId == null && isPasswordType) {
                passwordId = node.autofillId
                if (!text.isNullOrBlank()) submittedPass = text
            } else if (usernameId == null && isUsernameType) {
                usernameId = node.autofillId
                if (!text.isNullOrBlank()) submittedUser = text
            }

            for (c in 0 until node.childCount) {
                nodeQueue.add(node.getChildAt(c))
            }
        }

        return ParsedAutofillStructure(
            domain = detectedDomain,
            packageName = detectedPackageName,
            usernameNodeId = usernameId,
            passwordNodeId = passwordId,
            creditCardNodeId = cardId,
            detectedSubmittedUsername = submittedUser,
            detectedSubmittedPassword = submittedPass
        )
    }
}
