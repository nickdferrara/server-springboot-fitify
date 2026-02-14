package com.nickdferrara.fitify.shared.crypto

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class NonDeterministicEncryptedStringConverter : AttributeConverter<String, String> {

    private val encryptor: AesEncryptor
        get() = ApplicationContextProvider.getBean(AesEncryptor::class.java)

    override fun convertToDatabaseColumn(attribute: String?): String? {
        return attribute?.let { encryptor.encryptNonDeterministic(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        return dbData?.let { encryptor.decrypt(it) }
    }
}
