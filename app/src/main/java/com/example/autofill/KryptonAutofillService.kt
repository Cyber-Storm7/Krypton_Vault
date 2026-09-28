package com.example.autofill

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.KryptonApplication
import com.example.MainActivity
import com.example.crypto.CryptoManager
import com.example.crypto.wipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class KryptonAutofillService : AutofillService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            callback.onSuccess(null)
            return
        }

        val parsed = StructureParser.parse(structure)
        val targetQuery = parsed.domain ?: parsed.packageName ?: ""

        val responseBuilder = FillResponse.Builder()

        // Check if vault is locked
        if (!CryptoManager.isUnlocked()) {
            val authIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                1001,
                authIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val authPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(android.R.id.text1, "🔒 Unlock KryptonVault to Autofill")
            }

            if (parsed.passwordNodeId != null) {
                responseBuilder.setAuthentication(
                    arrayOf(parsed.passwordNodeId),
                    pendingIntent.intentSender,
                    authPresentation
                )
            }
            callback.onSuccess(responseBuilder.build())
            return
        }

        // Vault is unlocked - query matching credentials
        serviceScope.launch {
            try {
                val repository = KryptonApplication.instance.repository
                val dao = KryptonApplication.instance.database.vaultDao()
                val entities = dao.searchItems(targetQuery).firstOrNull() ?: emptyList()

                if (entities.isEmpty()) {
                    callback.onSuccess(null)
                    return@launch
                }

                var datasetCount = 0
                for (entity in entities) {
                    val decrypted = repository.getItemById(entity.id) ?: continue

                    val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_2).apply {
                        setTextViewText(android.R.id.text1, "${decrypted.title} (KryptonVault)")
                        setTextViewText(android.R.id.text2, decrypted.username)
                    }

                    val datasetBuilder = Dataset.Builder()

                    if (parsed.usernameNodeId != null && decrypted.username.isNotBlank()) {
                        datasetBuilder.setValue(
                            parsed.usernameNodeId,
                            AutofillValue.forText(decrypted.username),
                            presentation
                        )
                    }

                    if (parsed.passwordNodeId != null && decrypted.password.isNotEmpty()) {
                        val passString = String(decrypted.password)
                        datasetBuilder.setValue(
                            parsed.passwordNodeId,
                            AutofillValue.forText(passString),
                            presentation
                        )
                    }

                    responseBuilder.addDataset(datasetBuilder.build())
                    decrypted.wipeSensitiveFields()
                    datasetCount++

                    if (datasetCount >= 5) break // Limit top matches
                }

                // Register SaveInfo if fields are present
                if (parsed.passwordNodeId != null) {
                    val saveInfoBuilder = SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME,
                        arrayOf(parsed.passwordNodeId)
                    )
                    if (parsed.usernameNodeId != null) {
                        saveInfoBuilder.setOptionalIds(arrayOf(parsed.usernameNodeId))
                    }
                    responseBuilder.setSaveInfo(saveInfoBuilder.build())
                }

                callback.onSuccess(responseBuilder.build())
            } catch (_: Exception) {
                callback.onSuccess(null)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            callback.onSuccess()
            return
        }

        val parsed = StructureParser.parse(structure)
        val submittedUser = parsed.detectedSubmittedUsername
        val submittedPass = parsed.detectedSubmittedPassword

        if (!submittedPass.isNullOrBlank() && CryptoManager.isUnlocked()) {
            serviceScope.launch {
                try {
                    val repo = KryptonApplication.instance.repository
                    val title = parsed.domain ?: parsed.packageName ?: "Saved Login"
                    val item = com.example.model.VaultItemDecrypted(
                        id = java.util.UUID.randomUUID().toString(),
                        type = com.example.model.VaultItemType.LOGIN,
                        title = title,
                        username = submittedUser ?: "",
                        password = submittedPass.toCharArray(),
                        websiteUrl = parsed.domain ?: "",
                        notes = "Auto-captured by KryptonVault Autofill"
                    )
                    repo.saveItem(item)
                    item.wipeSensitiveFields()
                } catch (_: Exception) {}
            }
        }
        callback.onSuccess()
    }
}
