// TaskManager.kt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ---------------------------
// Demonstrates: enum + data class (stretch: data class)
// ---------------------------
enum class Priority { LOW, MEDIUM, HIGH }

data class Task(
    val id: Int,
    var description: String,
    var dueDate: LocalDate? = null,
    var priority: Priority = Priority.MEDIUM,
    var completed: Boolean = false,
    val createdAt: LocalDate = LocalDate.now()
)

// ---------------------------
// TaskManager: demonstrates classes, functions, collections (mutableList), loops, conditionals
// ---------------------------
class TaskManager {
    private val tasks = mutableListOf<Task>()   // mutable collection
    private var nextId = 1                      // mutable variable

    // add task (shows use of var/val: nextId is var, id inside Task is val)
    fun addTask(description: String, dueDate: LocalDate?, priority: Priority) {
        val task = Task(nextId++, description, dueDate, priority)
        tasks.add(task)
        println("Task added: ${task.id} - ${task.description}")
    }

    // list tasks (shows loops and formatting)
    fun listTasks() {
        if (tasks.isEmpty()) {
            println("No tasks found.")
            return
        }

        val sentinel = LocalDate.of(9999, 12, 31)
        val sorted = tasks.sortedWith(compareBy({ it.dueDate ?: sentinel }, { it.priority.ordinal }))

        println("\n--- Task List (${tasks.size}) ---")
        for (task in sorted) {
            val status = if (task.completed) "✔ Done" else "❌ Pending"
            val due = task.dueDate?.toString() ?: "No due date"
            println("${task.id}. ${task.description} | due: $due | priority: ${task.priority} | $status")
        }
    }

    // complete a task
    fun completeTask(id: Int) {
        val task = tasks.find { it.id == id }
        if (task != null) {
            task.completed = true
            println("Task '${task.description}' marked as complete.")
        } else {
            println("Task not found.")
        }
    }

    // remove a task (shows collection modification)
    fun removeTask(id: Int) {
        val removed = tasks.removeIf { it.id == id }
        if (removed) println("Task removed.")
        else println("Task not found.")
    }

    // search tasks by keyword (shows filter)
    fun searchTasks(keyword: String) {
        val results = tasks.filter { it.description.contains(keyword, ignoreCase = true) }
        if (results.isEmpty()) {
            println("No tasks matching '$keyword' found.")
        } else {
            println("\n--- Search Results ---")
            results.forEach { println("${it.id}. ${it.description} | ${it.priority} | due: ${it.dueDate ?: "none"}") }
        }
    }

    // edit task: description / due date / priority (demonstrates functions & mutation)
    fun editTask(id: Int, newDescription: String?, newDue: LocalDate?, newPriority: Priority?) {
        val task = tasks.find { it.id == id }
        if (task == null) {
            println("Task not found.")
            return
        }
        if (!newDescription.isNullOrBlank()) task.description = newDescription
        task.dueDate = newDue // allow null to clear due date
        if (newPriority != null) task.priority = newPriority
        println("Task ${task.id} updated.")
    }

    // demonstrates use of 'when' as expression and with ranges
    fun workloadSummary(): String {
        return when (tasks.size) {
            0 -> "No tasks — you're free!"
            in 1..3 -> "Light load"
            in 4..7 -> "Moderate load"
            else -> "Heavy load — prioritize!"
        }
    }

    // demonstrates a 'when' with boolean conditions (no subject)
    fun upcomingOverdueInfo(today: LocalDate = LocalDate.now()) {
        val overdue = tasks.filter { it.dueDate != null && !it.completed && it.dueDate!!.isBefore(today) }
        when {
            overdue.isEmpty() -> println("No overdue tasks.")
            overdue.size == 1 -> println("1 overdue task — fix it soon.")
            overdue.size > 1 -> println("${overdue.size} overdue tasks — take action.")
        }
    }
}

// ---------------------------
// Helper functions (parsing, mapping) demonstrate when and error handling
// ---------------------------
fun parseDate(input: String?): LocalDate? {
    if (input.isNullOrBlank()) return null
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return try {
        LocalDate.parse(input.trim(), fmt)
    } catch (e: DateTimeParseException) {
        println("Invalid date format. Use yyyy-MM-dd (e.g., 2025-10-15). Date ignored.")
        null
    }
}

fun priorityFromInt(i: Int): Priority = when (i) {
    1 -> Priority.LOW
    2 -> Priority.MEDIUM
    3 -> Priority.HIGH
    else -> {
        println("Unknown priority; defaulting to MEDIUM.")
        Priority.MEDIUM
    }
}

// ---------------------------
// Main: menu-driven loop demonstrating input handling + conditionals (when), loops
// ---------------------------
fun main() {
    val tm = TaskManager()
    loop@ while (true) {
        println(
            """
            ========================
            Task Manager
            ========================
            1. Add Task
            2. List Tasks
            3. Complete Task
            4. Remove Task
            5. Search Tasks
            6. Edit Task
            7. Workload Summary
            8. Check Overdue
            9. Exit
            Enter your choice:
            """.trimIndent()
        )
        val choice = readLine()?.trim()?.toIntOrNull() ?: -1
        when (choice) {
            1 -> {
                print("Description: ")
                val desc = readLine()?.trim().orEmpty()
                print("Due date (yyyy-MM-dd) or leave blank: ")
                val due = parseDate(readLine())
                println("Priority: 1=LOW, 2=MEDIUM, 3=HIGH (default 2)")
                val p = readLine()?.trim()?.toIntOrNull() ?: 2
                tm.addTask(desc, due, priorityFromInt(p))
            }
            2 -> tm.listTasks()
            3 -> {
                print("Task ID to mark complete: ")
                val id = readLine()?.trim()?.toIntOrNull() ?: -1
                tm.completeTask(id)
            }
            4 -> {
                print("Task ID to remove: ")
                val id = readLine()?.trim()?.toIntOrNull() ?: -1
                tm.removeTask(id)
            }
            5 -> {
                print("Enter search keyword: ")
                val k = readLine()?.trim().orEmpty()
                tm.searchTasks(k)
            }
            6 -> {
                print("Task ID to edit: ")
                val id = readLine()?.trim()?.toIntOrNull() ?: -1
                print("New description (leave blank to keep): ")
                val nd = readLine()
                print("New due date (yyyy-MM-dd) or blank to clear/keep: ")
                val newDue = parseDate(readLine())
                println("New priority: 1=LOW, 2=MEDIUM, 3=HIGH or blank to keep current")
                val npLine = readLine()
                val np = npLine?.trim()?.toIntOrNull()
                tm.editTask(id, if (nd.isNullOrBlank()) null else nd, newDue, np?.let { priorityFromInt(it) })
            }
            7 -> println("Workload: ${tm.workloadSummary()}")
            8 -> tm.upcomingOverdueInfo()
            9 -> {
                println("Goodbye!")
                break@loop
            }
            else -> println("Invalid choice — try again.")
        }
        println() // blank line for readability
    }
}
