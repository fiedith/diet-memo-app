package kr.ac.tukorea.dietmemoapp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {

    val dataModelList = mutableListOf<DataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Realtime Database: get data from database START
        val database = Firebase.database
        val myRef = database.getReference("myMemo")

        val listView = findViewById<ListView>(R.id.mainLV)

        val adapter_list = ListViewAdapter(dataModelList)

        listView.adapter = adapter_list

//        Log.d("DataModel ----- ", dataModelList.toString())

        // iterate data
        // add added .child(Firebase.auth.currentUser!!.uid) to get from uid user's own database
        myRef.child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                // clear list each time to prevent the whole list from stacking up
                dataModelList.clear()

                for(dataModel in snapshot.children){
                    Log.d("Data", dataModel.toString())
                    dataModelList.add(dataModel.getValue(DataModel::class.java)!!)
                }
                adapter_list.notifyDataSetChanged()
                Log.d("DataModel", dataModelList.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        // END


        val writeButton = findViewById<ImageView>(R.id.writeBtn)
        writeButton.setOnClickListener{

            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Workout memo dialog")

            val mAlertDialog = mBuilder.show()

            val dateSelectBtn = mAlertDialog.findViewById<Button>(R.id.dateSelectBtn)

            var dateText = ""

            dateSelectBtn?.setOnClickListener {

                val today = GregorianCalendar()
                val year : Int = today.get(Calendar.YEAR)
                val month : Int = today.get(Calendar.MONTH)
                val date : Int = today.get(Calendar.DATE)


                val dlg = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        Log.d("MAIN", "${year}, ${month + 1}, ${dayOfMonth}")
                        dateSelectBtn.setText("${year}. ${month + 1}. ${dayOfMonth}")

                        dateText = "${year}. ${month + 1}. ${dayOfMonth}"
                    }

                }, year, month, date)
                dlg.show()
            }


            // Realtime Database: save memo and date to database
            val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)
            saveBtn?.setOnClickListener {

                val workoutMemo = mAlertDialog.findViewById<EditText>(R.id.workoutMemo)?.text.toString()

                val database = Firebase.database

                // added .child(Firebase.auth.currentUser!!.uid) to save to each uid user's own database

                val myRef = database.getReference("myMemo").child(Firebase.auth.currentUser!!.uid)

                // using implemented DataModel data class
                val model = DataModel(dateText, workoutMemo)

                // push model to database
                myRef.push().setValue(model)

                // close dialog after pushing to databas
                mAlertDialog.dismiss()


            }

        }


    }
}