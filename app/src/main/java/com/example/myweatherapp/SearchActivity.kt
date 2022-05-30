package com.example.myweatherapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError


class SearchActivity : AppCompatActivity(), SuggestSession.SuggestListener {
    private val MAPKIT_API_KEY = ""
    private val SUGGESTIONS_NUM_LIM = 5

    private lateinit var searchManager: SearchManager
    private lateinit var suggestSession: SuggestSession
    private lateinit var suggestResultView: ListView
    private lateinit var suggestsAdapter: ArrayAdapter<String>
    private lateinit var suggestions: MutableList<String>

    private val center = Point(55.75, 37.62)
    private val boxSize: Double = 0.2
    private val boundingBox = BoundingBox(
        Point(center.latitude - boxSize, center.longitude - boxSize),
        Point(center.latitude + boxSize, center.longitude + boxSize)
    )
    private val SUGGEST_OPTIONS : SuggestOptions = SuggestOptions().setSuggestTypes(SuggestType.GEO.value)


    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)
        setContentView(R.layout.activity_search)
        super.onCreate(savedInstanceState)

        suggestResultView = findViewById(R.id.suggest_result)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
        suggestSession = searchManager.createSuggestSession()
        val searchField = findViewById<EditText>(R.id.search_field)
        suggestions = ArrayList()
        suggestsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            suggestions
        )
        suggestResultView.adapter = suggestsAdapter
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                suggestResultView.visibility = View.INVISIBLE
                suggestSession.suggest(editable.toString(), boundingBox, SUGGEST_OPTIONS, this@SearchActivity)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onResponse(suggest: MutableList<SuggestItem>) {
        suggestions.clear()
        for (i in 0 until minOf(SUGGESTIONS_NUM_LIM, suggest.size))
            suggestions.add(suggest.get(i).displayText!!)
        suggestsAdapter.notifyDataSetChanged()
        suggestResultView.visibility = View.VISIBLE
    }

    override fun onError(error: Error) {
        if (error is RemoteError) {
            Toast.makeText(this, "Remote error: $error", Toast.LENGTH_LONG).show()
        } else if (error is NetworkError) {
            Toast.makeText(this, "Network error: $error", Toast.LENGTH_LONG).show()
        }
    }

}