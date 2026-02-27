package com.myguruu.app.features.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.compose.ui.platform.LocalContext

data class NewsArticle(val title: String, val summary: String, val source: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentAffairsScreen() {
    val context = LocalContext.current
    
    // Hardcoded data simulating a daily feed
    val newsFeed = listOf(
        NewsArticle(
            "Economic Survey Highlights", 
            "The latest economic survey highlights steady growth and focuses on infrastructure funding for the upcoming fiscal year.",
            "The Hindu"
        ),
        NewsArticle(
            "International Relations Update", 
            "Bilateral talks focus on trade agreements and border security enhancements.",
            "Indian Express"
        ),
        NewsArticle(
            "Environment & Ecology", 
            "New policy proposed to increase forest cover and reduce carbon emissions by 2030.",
            "PIB"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daily Current Affairs") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Simulate manually queuing the background worker
                val workRequest = OneTimeWorkRequestBuilder<CurrentAffairsWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }) {
                Text("Sync")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(newsFeed) { article ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Source: ${article.source}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
