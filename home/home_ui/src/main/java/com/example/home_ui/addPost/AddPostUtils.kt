package com.example.home_ui.addPost

import android.content.Context
import android.graphics.Bitmap
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.example.home_ui.addPost.utils.AwsS3Helper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object AddPostUtils {

    suspend fun uploadToS3(context: Context, bitmap: Bitmap): String =
        suspendCancellableCoroutine { cont ->

            try {
                val fileName = "post_${UUID.randomUUID()}.jpg"
                val tempFile = File.createTempFile(fileName, null, context.cacheDir)
                val outStream = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                outStream.flush()
                outStream.close()

                val transferUtility = AwsS3Helper.getTransferUtility(context)
                val uploadObserver = transferUtility.upload(
                    AwsS3Helper.getBucketName(),
                    fileName,
                    tempFile
                )

                uploadObserver.setTransferListener(object : TransferListener {
                    override fun onStateChanged(id: Int, state: TransferState?) {
                        if (state == TransferState.COMPLETED) {
                            val url =
                                "https://${AwsS3Helper.getBucketName()}.s3.ap-southeast-2.amazonaws.com/$fileName"
                            cont.resume(url)
                        } else if (state == TransferState.FAILED) {
                            cont.resumeWithException(Exception("Upload failed"))
                        }
                    }

                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                        // Optional: Log or show progress
                    }

                    override fun onError(id: Int, ex: Exception?) {
                        cont.resumeWithException(ex ?: Exception("Unknown error"))
                    }
                })
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

}
