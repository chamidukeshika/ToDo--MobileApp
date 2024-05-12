package com.example.todo

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import com.example.todo.databinding.ActivityMainBinding // Import ViewBinding class generated for your layout file
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // Declare ViewBinding variable
    private lateinit var database: myDatabase
    private lateinit var adapter: Adapter
    private var dataList = mutableListOf<CardInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflate layout using ViewBinding
        setContentView(binding.root)
        database = Room.databaseBuilder(
            applicationContext, myDatabase::class.java, "To_Do"
        ).build()

        binding.add.setOnClickListener { // Access views through ViewBinding variable
            val intent = Intent(this, CreateCard::class.java)
            startActivity(intent)
        }

        binding.deleteAll.setOnClickListener { // Access views through ViewBinding variable
            DataObject.deleteAll()
            GlobalScope.launch {
                database.dao().deleteAll()
            }
            setRecycler()
        }
        binding.deleteAll.setOnClickListener {
            showConfirmationDialog()
        }
        setupSearchView()
        setRecycler()
        loadData()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val filteredList = dataList.filter { cardInfo ->
                        cardInfo.title.contains(newText, ignoreCase = true)
                    }
                    adapter.setData(filteredList)
                }
                return true
            }
        })
    }
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete all tasks?")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            deleteAllData()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }
        builder.show()
    }
    private fun deleteAllData() {
        DataObject.deleteAll()
        GlobalScope.launch {
            database.dao().deleteAll()
        }
        setRecycler()
    }
    private fun setRecycler() {
        dataList = DataObject.getAllData().toMutableList()
        adapter = Adapter(dataList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Show/hide no tasks message
        if (dataList.isEmpty()) {
            binding.noTasksTextView.visibility = View.VISIBLE
        } else {
            binding.noTasksTextView.visibility = View.GONE
        }
    }
    private fun loadData() {
        dataList = DataObject.getAllData().toMutableList()
        adapter?.setData(dataList) // Use safe call operator to avoid NullPointerException
    }

}
