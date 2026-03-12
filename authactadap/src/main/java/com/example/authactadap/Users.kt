package com.example.authactadap

data class Users(
    val name: String,
    val age: Int,
    val email: String,
    val login: String,
    val password: String
)

object InMemoryUserRepository {
//    private val users = listOf(
//        Users("Иван Иванов", 28, "ivan@example.com", "ivan", "1234"),
//        Users("Мария Петрова", 32, "maria@example.com", "maria", "abcd"),
//        Users("Сергей Смирнов", 25, "sergey@example.com", "sergey", "qwerty")
//    )
    private val users = mutableListOf<Users>()

    fun getAllUsers(): List<Users> = users
    fun getUserByLogin(login: String): Users? = users.find { it.login == login }
    fun registerUser(
        name: String,
        age: Int,
        email: String,
        login: String,
        password: String
    ): Users? {
        if (getUserByLogin(login) != null) {
            return null // логин занят
        }
        val user = Users(name, age, email, login, password)
        users.add(user)
        return user
    }

}
