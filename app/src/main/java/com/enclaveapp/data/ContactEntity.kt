package com.enclaveapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val publicKeyBase64: String,     // their public key
    val fingerprint: String,         // human-readable key fingerprint for verification
    val isVerified: Boolean = false  // true = user has confirmed fingerprint in person
)
