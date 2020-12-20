package com.example.concept2

data class User (val friendsList: Array<String>, val image: Int, val username: String, val online: Boolean, val ingame: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (!friendsList.contentEquals(other.friendsList)) return false

        return true
    }

    override fun hashCode(): Int {
        return friendsList.contentHashCode()
    }
}