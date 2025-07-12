package com.example.home_ui.addPost.utils

import android.content.Context
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.example.home_ui.BuildConfig
import java.util.Properties


object AwsS3Helper {

    private const val BUCKET_NAME = BuildConfig.AWS_BUCKET_NAME
    private const val ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY
    private const val SECRET_KEY = BuildConfig.AWS_SECRET_KEY

    fun getTransferUtility(context: Context):TransferUtility {
        val credentials by lazy {
            BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
        }
        val s3Client: AmazonS3 by lazy {
            AmazonS3Client(credentials).apply {
                setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
            }
        }

        return TransferUtility.builder()
            .context(context.applicationContext)
            .s3Client(s3Client)
            .build()
    }

    fun getBucketName(): String = BUCKET_NAME
}