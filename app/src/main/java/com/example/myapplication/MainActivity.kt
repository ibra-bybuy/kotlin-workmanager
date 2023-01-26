package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.*
import coil.compose.rememberImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build()
            )
            .build()
        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>()
            .build()

        val workManager = WorkManager.getInstance(applicationContext)


        setContent {
            MyApplicationTheme {
                val workInfos = workManager
                    .getWorkInfosForUniqueWorkLiveData("download")
                    .observeAsState()
                    .value
                val downloadInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == downloadRequest.id }
                }

                val filterInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == colorFilterRequest.id }
                }

                val imageUri by derivedStateOf {
                    val downloadUri = downloadInfo?.outputData?.getString(WorkerParams.IMAGE_URI)?.toUri()
                    val filterUri = filterInfo?.outputData?.getString(WorkerParams.FILTER_URI)?.toUri()

                    filterUri ?: downloadUri
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri?.let { uri ->
                        Image(painter = rememberImagePainter(data = uri), contentDescription = null, modifier = Modifier.fillMaxSize())
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Button(onClick = {
                        workManager.beginUniqueWork("download", ExistingWorkPolicy.KEEP, downloadRequest).then(colorFilterRequest).enqueue()
                    }, enabled = downloadInfo?.state != WorkInfo.State.RUNNING) {
                        Text(text = "Start downloading")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when(downloadInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Downloading...")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Downloading succeeded")
                        WorkInfo.State.FAILED -> Text(text = "Downloading failed")
                        WorkInfo.State.CANCELLED -> Text(text = "Downloading canceled")
                        WorkInfo.State.ENQUEUED -> Text(text = "Downloading ENQUEUED")
                        WorkInfo.State.BLOCKED -> Text(text = "Downloading blocked")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when(filterInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Applying filter...")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Applying succeeded")
                        WorkInfo.State.FAILED -> Text(text = "Applying failed")
                        WorkInfo.State.CANCELLED -> Text(text = "Applying canceled")
                        WorkInfo.State.ENQUEUED -> Text(text = "Applying ENQUEUED")
                        WorkInfo.State.BLOCKED -> Text(text = "Applying blocked")
                    }
                }
            }
        }
    }
}

