package org.trackit

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_view_habits.*
import kotlinx.android.synthetic.main.habit_details.view.*
import kotlinx.android.synthetic.main.habit.view.*
import org.trackit.data.Habit

class ViewHabitsActivity : AppCompatActivity() {
    private val habitsLiveData = MutableLiveData<List<Habit>>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_habits)
        val habits = listOf(Habit("programming"), Habit("gaming")).toMutableList()
        updateUI(habits)
        btn_add_habit.setOnClickListener { HabitPopup(this) { habits.add(it); updateUI(habits) }.show() }
        habitsLiveData.observe(this, Observer { initializeRecyclerView(it) })
    }

    private fun updateUI(habits: MutableList<Habit>) {
        habitsLiveData.postValue(habits)
        if (habits.isEmpty()) {
            rv_habits.visibility = View.INVISIBLE
            tv_no_habits.visibility = View.VISIBLE
        } else {
            rv_habits.visibility = View.VISIBLE
            tv_no_habits.visibility = View.INVISIBLE
        }
    }

    private fun initializeRecyclerView(habits: List<Habit>) {
        val habitAdapter = HabitAdapter(layout_view_habits, habits.toMutableList())
        viewManager = LinearLayoutManager(this)
        viewAdapter = habitAdapter
        recyclerView = rv_habits.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        val swipeHelper = ItemTouchHelper(SwipeToDeleteHabit(this, habitAdapter))
        swipeHelper.attachToRecyclerView(recyclerView)
    }
}

class HabitAdapter(private val parent: View, private val habits: MutableList<Habit>) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        return HabitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.habit, parent, false))
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.view.tv_habit_name.text = habit.name
        holder.view.setOnClickListener {
            HabitPopup(parent.context, habit) { notifyItemChanged(position) }.show()
        }
    }

    override fun getItemCount(): Int = habits.size

    fun deleteItem(position: Int) {
        val deletedHabit = habits.removeAt(position)
        notifyItemRemoved(position)
        showUndoSnackbar(position, deletedHabit)
    }

    private fun showUndoSnackbar(position: Int, deletedHabit: Habit) {
        Snackbar.make(parent, "Habit deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") { undoDelete(position, deletedHabit) }
            .show()
    }

    private fun undoDelete(position: Int, deletedHabit: Habit) {
        habits.add(position, deletedHabit)
        notifyItemInserted(position)
    }
}

class HabitPopup(private val context: Context, private val habit: Habit = Habit(), private val onSave: (Habit) -> Unit = { }) {
    fun show() {
        val dialogView = View.inflate(context, R.layout.habit_details, null)
        val alertDialog = AlertDialog.Builder(context).create()
        dialogView.txt_habit_name.setText(habit.name)

        dialogView.btn_save.setOnClickListener {
            habit.name = dialogView.txt_habit_name.text.toString()
            onSave(habit)
            alertDialog.dismiss()
        }
        dialogView.btn_cancel.setOnClickListener { alertDialog.dismiss() }
        alertDialog.setView(dialogView)
        alertDialog.show()
    }
}

class SwipeToDeleteHabit(context: Context, private val adapter: HabitAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (dX > 0) {
            val item = viewHolder.itemView
            val iconMargin = (item.height - icon.intrinsicHeight) / 2
            val iconTop = item.top + iconMargin
            val iconBottom = iconTop + icon.intrinsicHeight
            val iconLeft = item.left + iconMargin
            val iconRight = item.left + iconMargin + icon.intrinsicWidth
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        }
        icon.draw(c)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.deleteItem(viewHolder.adapterPosition)
    }
}