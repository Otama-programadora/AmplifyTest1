package com.example.amplifytest1

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.Response
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.regions.RegionUtils.init
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.exception.ApolloException
import javax.annotation.Nonnull


class MainActivity : AppCompatActivity() {
    internal lateinit var mListView:ListView
    internal lateinit var mButton:Button
    internal lateinit var mEditText:EditText

    private var mText: ArrayList<ListTexsQuery.Item>? = null
    private var Adapter: ArrayAdapter<ListTextsQuery.Item>? = null
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mListView = findViewById(R.id.TestList)
        mButton = findViewById(R.id.addText)
        mEditText = findViewById(R.id.editText)

        ClientTest.init(this)

        mButton.setOnClickListener(object: DialogInterface.OnClickListener() {
            fun onClick(v: View?) {
                save()
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                query()
            }

            override fun onClick(dialog: DialogInterface?, which: Int) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onStart() {
        super.onStart()

        // Query list data when we start application
        query()
    }

    fun query() {
        ClientTest.appSyncClient().query(ListTextsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback)
    }

    private val queryCallback = object : GraphQLCall.Callback<ListTextsQuery.Data?>() {
        fun onResponse(@Nonnull response: Response<ListTextsQuery.Data?>) {
            mText = ArrayList(response.Data().listTexts().items())
            Log.i(TAG, "Retrieved list items: " + mText.toString())
            Adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, mText)
            runOnUiThread {
                mListView.adapter = Adapter
                Toast.makeText(this@MainActivity, "Print text", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(@Nonnull e: ApolloException) {
            Log.e(TAG, e.toString())
        }
    }

    // データ保存
    private fun save() {
        val text = mEditText.text.toString()
        val input = CreateTextInput.builder().text(text).build()
        val addTextMutation = CreateTextMutation.builder().input(input).build()
        ClientTest.appSyncClient().mutate(addTextMutation).enqueue(mutateCallback)
        mEditText.setText("")
    }

    private val mutateCallback = object : GraphQLCall.Callback<CreateTextMutation.Data?>() {
        fun onResponse(@Nonnull response: Response<CreateTextMutation.Data?>?) {
            runOnUiThread { Toast.makeText(this@MainActivity, "Added text", Toast.LENGTH_SHORT).show() }
        }

        override fun onFailure(@Nonnull e: ApolloException) {
            runOnUiThread {
                Log.e("", "Failed to perform AddPetMutation", e)
                Toast.makeText(this@MainActivity, "Failed to add pet", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}