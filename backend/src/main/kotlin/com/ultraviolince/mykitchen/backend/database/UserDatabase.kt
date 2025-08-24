package com.ultraviolince.mykitchen.backend.database

import com.ultraviolince.mykitchen.backend.model.User
import kotlinx.serialization.Serializable
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

@Serializable
data class UsersData(
    val users: List<User> = emptyList()
)

class UserDatabase(private val file: Path, createBackup: Boolean = false) {
    private var data: UsersData
    private val yaml = Yaml(
        DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
    )

    init {
        // Create file if it doesn't exist
        if (!file.exists()) {
            file.writeText(yaml.dump(mapOf("users" to emptyList<Map<String, Any>>())))
        } else if (createBackup) {
            createBackup()
        }

        // Load data from file
        data = loadFromFile()
    }

    private fun createBackup() {
        val backupDirectory = file.parent.resolve("backup")
        backupDirectory.createDirectories()
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))
        val backupFile = backupDirectory.resolve("${file.fileName.toString().substringBeforeLast(".")}-$date.yaml")
        File(file.toString()).copyTo(File(backupFile.toString()), overwrite = true)
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadFromFile(): UsersData {
        return try {
            val yamlContent = yaml.load<Map<String, Any>>(file.toFile().inputStream())
            val usersList = yamlContent["users"] as? List<Map<String, String>> ?: emptyList()
            val users = usersList.map { userMap ->
                User(
                    name = userMap["name"] ?: "",
                    email = userMap["email"] ?: "",
                    password = userMap["password"] ?: ""
                )
            }
            UsersData(users)
        } catch (e: Exception) {
            println("Found database file cannot be validated! Creating a backup and a new database")
            createBackupOnError()
            val emptyData = UsersData()
            saveToFile(emptyData)
            emptyData
        }
    }

    private fun createBackupOnError() {
        val backupDirectory = file.parent.resolve("backup")
        backupDirectory.createDirectories()
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))
        val backupFile = backupDirectory.resolve(
            "${file.fileName.toString().substringBeforeLast(".")}-incompatible-$date.yaml"
        )
        File(file.toString()).copyTo(File(backupFile.toString()), overwrite = true)
    }

    private fun saveToFile(usersData: UsersData) {
        val yamlMap = mapOf(
            "users" to usersData.users.map { user ->
                mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "password" to user.password
                )
            }
        )
        file.writeText(yaml.dump(yamlMap))
        data = usersData
    }

    fun validateLoginRequest(email: String?, password: String?): Pair<User?, String?> {
        if (email == null || password == null) {
            return null to "Must provide user email and password"
        }

        val users = data.users.filter { it.email == email }
        if (users.isEmpty() || users.size != 1) {
            return null to "Could not find user with email $email"
        }

        val user = users.first()
        return if (user.password == password) {
            user to null
        } else {
            null to "Bad credentials"
        }
    }

    fun create(email: String, username: String, password: String): User {
        val newUser = User(
            name = username,
            email = email,
            password = password
        )

        val updatedData = data.copy(users = data.users + newUser)
        saveToFile(updatedData)

        return newUser
    }

    fun getAll(): List<User> = data.users

    fun getByUsername(username: String): User? = data.users.find { it.name == username }
}
