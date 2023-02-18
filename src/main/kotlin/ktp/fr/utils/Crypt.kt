package ktp.fr.utils

import org.mindrot.jbcrypt.BCrypt

fun String.hashPassword(): String {
    val salt = BCrypt.gensalt()
    return BCrypt.hashpw(this, salt)
}

fun String.checkPassword(password: String, hashedPassword: String): Boolean {
    return BCrypt.checkpw(password, hashedPassword)
}