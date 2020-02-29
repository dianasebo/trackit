package org.trackit

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.habits_fragment.*
import kotlinx.android.synthetic.main.habit.view.*
import kotlinx.android.synthetic.main.habit_color.view.*
import kotlinx.android.synthetic.main.habit_details.view.*
import org.trackit.data.Habit
import org.trackit.data.HabitColor
import org.trackit.data.HabitRepository
import org.trackit.database.HabitDbStore
import org.trackit.database.RoomDatabase
import org.trackit.viewmodel.HabitsViewModel
import org.trackit.viewmodel.ViewModelFactory
import java.time.LocalDate

class HabitsFragment : Fragment() {
    private lateinit var habitsViewModel: HabitsViewModel
    private lateinit var habitsAdapter: HabitAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.habits_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val habitRepository = HabitDbStore(RoomDatabase.getDb(this.context!!))
        initializeRecyclerView(habitRepository)
        updateUI(habitRepository)
        btn_add_habit.setOnClickListener {
            HabitPopup(this.context!!) {
                habitsViewModel.addHabit(it)
                updateUI(habitRepository)
            }.show()
        }
    }

    private fun updateUI(habitRepository: HabitRepository) =
        if (habitRepository.getAll().isEmpty()) {
            rv_habits.visibility = View.INVISIBLE
            tv_no_habits.visibility = View.VISIBLE
        } else {
            rv_habits.visibility = View.VISIBLE
            tv_no_habits.visibility = View.INVISIBLE
        }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeRecyclerView(habitRepository: HabitRepository) {
        habitsViewModel = ViewModelProvider(this, ViewModelFactory(habitRepository)).get(
            HabitsViewModel::class.java)
        habitsAdapter =
            HabitAdapter(habitRepository.getAll(), habitsViewModel::updateHabit)
        rv_habits.apply {
            layoutManager = LinearLayoutManager(this@HabitsFragment.context)
            adapter = habitsAdapter
        }
        habitsViewModel.habitsLiveData.observe(this.activity as FragmentActivity, Observer { habits ->
            habitsAdapter = HabitAdapter(habits, habitsViewModel::updateHabit)
            rv_habits.adapter = habitsAdapter
        })

        val swipeHelper = ItemTouchHelper(SwipeToDeleteHabit(this.context!!) {
            val deletedHabit = habitsAdapter.getHabitAtPosition(it)
            habitsViewModel.removeHabit(deletedHabit)
            updateUI(habitRepository)
            Snackbar.make(rv_habits, deletedHabit.title + " deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    habitsViewModel.addHabit(deletedHabit)
                    updateUI(habitRepository)
                }
                .show()
        })
        swipeHelper.attachToRecyclerView(rv_habits)
    }class HabitAdapter(
        private val habits: List<Habit>,
        private val onHabitChanged: (Habit) -> (Unit)
    ) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

        class HabitViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        private lateinit var parent: View

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
            this.parent = parent
            return HabitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.habit, parent, false))
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
            val habit = habits[position]
            holder.view.background =
                ContextCompat.getDrawable(parent.context, HabitColor.valueOf(habit.color).color)
            holder.view.tv_habit_name.text = habit.title
            holder.view.setOnClickListener {
                HabitPopup(parent.context, habit, onHabitChanged).show()
            }
        }

        override fun getItemCount(): Int = habits.size

        fun getHabitAtPosition(position: Int): Habit = habits[position]
    }

    class SwipeToDeleteHabit(context: Context, private val onSwipe: (Int) -> (Unit)) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        private val icon = ContextCompat.getDrawable(context, android.R.drawable.ic_delete)!!

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

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwipe(viewHolder.adapterPosition)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    class HabitPopup (
        private val context: Context,
        private val habit: Habit = Habit(
            title = "",
            color = HabitColor.Default.name,
            goal = 1,
            start = LocalDate.now()
        ),
        private val onSave: (Habit) -> Unit = { }
    ) {
        private val initialHabit = habit.copy()
        private val dialogView = View.inflate(context, R.layout.habit_details, null)
        fun show() {
            val alertDialog = AlertDialog.Builder(context).create()
            dialogView.txt_habit_title.setText(habit.title)
            dialogView.txt_habit_title.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    habit.title = s.toString()
                    setSaveButtonState()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            })

            val habitColors = HabitColor.values()
            object : ArrayAdapter<HabitColor>(context, R.layout.habit_color, habitColors) {
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return getHabitColorView(position, parent)
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return getHabitColorView(position, parent)
                }

                private fun getHabitColorView(position: Int, parent: ViewGroup): View {
                    val habitColor = habitColors[position]
                    val habitColorView = LayoutInflater.from(parent.context).inflate(R.layout.habit_color, parent, false)
                    habitColorView.name.text = habitColor.name
                    habitColorView.img.setImageDrawable(
                        ContextCompat.getDrawable(
                            parent.context,
                            habitColor.drawable
                        )
                    )
                    return habitColorView
                }
            }.also { adapter ->
                adapter.setDropDownViewResource(R.layout.habit_color)
                dialogView.spn_habit_color.adapter = adapter
            }
            dialogView.spn_habit_color.setSelection(habitColors.indexOf(HabitColor.valueOf(habit.color)))
            dialogView.spn_habit_color.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                    val habitColor = parent!!.getItemAtPosition(position) as HabitColor
                    habit.color = habitColor.name
                    setSaveButtonState()
                }
            }

            val habitGoals = (1..7).toList().map { "  $it  " }.toTypedArray()
            ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, habitGoals).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.spn_habit_goal.adapter = adapter
            }
            dialogView.spn_habit_goal.setSelection(habitGoals.indexOf("  ${habit.goal}  "))
            dialogView.spn_habit_goal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                    val goalString = parent!!.getItemAtPosition(position) as String
                    habit.goal = goalString.trim().toInt()
                    setSaveButtonState()
                }
            }

            dialogView.btn_save.setOnClickListener {
                onSave(habit)
                alertDialog.dismiss()
            }
            dialogView.btn_cancel.setOnClickListener { alertDialog.dismiss() }
            alertDialog.setView(dialogView)
            setSaveButtonState()
            alertDialog.show()
        }

        private fun setSaveButtonState() {
            dialogView.btn_save.isEnabled = habit != initialHabit && habit.title.isNotEmpty()
        }
    }
}