package com.github.okwrtdsh.amplifytest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.amplify.generated.graphql.CreateTodoMutation
import com.amazonaws.amplify.generated.graphql.ListTodosQuery
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import type.CreateTodoInput
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.amplify.generated.graphql.OnCreateTodoSubscription
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mAWSAppSyncClient: AWSAppSyncClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAWSAppSyncClient = AWSAppSyncClient.builder()
            .context(applicationContext)
            .awsConfiguration(AWSConfiguration(applicationContext))
            .build()
        btnMutation.setOnClickListener {
            runMutation()
        }
        btnQuery.setOnClickListener {
            runQuery()
        }
    }

    fun runMutation() {
        mAWSAppSyncClient.mutate(
            CreateTodoMutation.builder()
                .input(
                    CreateTodoInput.builder()
                        .name(editTextName.text.toString())
                        .description(editTextDescription.text.toString())
                        .build()
                ).build()
        ).enqueue(mutationCallback)
    }

    private val mutationCallback = object : GraphQLCall.Callback<CreateTodoMutation.Data>() {
        override fun onResponse(response: Response<CreateTodoMutation.Data>) {
            Log.i("Results", "Added Todo")
        }

        override fun onFailure(e: ApolloException) {
            Log.e("Error", e.toString())
        }
    }

    fun runQuery() {
        mAWSAppSyncClient.query(ListTodosQuery.builder().apply { limit(10000) }.build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(todosCallback)
    }

    private val todosCallback = object : GraphQLCall.Callback<ListTodosQuery.Data>() {
        override fun onResponse(response: Response<ListTodosQuery.Data>) {
            Log.i("Results", response.data()?.listTodos()?.items().toString())
            Log.d("###############", response.data()?.listTodos()?.items()?.count().toString())
            response.data()?.listTodos()?.items()?.map {
                Log.d(">>>", "name: ${it.name()},\tdescription: ${it.description()}")
            }
        }

        override fun onFailure(e: ApolloException) {
            Log.e("ERROR", e.toString())
        }
    }

    private lateinit var subscriptionWatcher: AppSyncSubscriptionCall<*>



    private val subCallback = object : AppSyncSubscriptionCall.Callback<OnCreateTodoSubscription.Data> {
        override fun onResponse(response: Response<OnCreateTodoSubscription.Data>) {
            Log.i("Response", response.data()!!.toString())
        }

        override fun onFailure(e: ApolloException) {
            Log.e("Error", e.toString())
        }

        override fun onCompleted() {
            Log.i("Completed", "Subscription completed")
        }
    }
}
