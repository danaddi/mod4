package com.example.kt1

import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import kotlin.system.measureTimeMillis

fun main() {

    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))

    runBlocking {

        println("Поиск дубликатов JSON файлов")

        val directoryPath = "C:\\Test\\json_files"
        val timeoutSeconds = 10L

        println("Директория: $directoryPath")
        println("Таймаут: $timeoutSeconds сек")

        val totalTime = measureTimeMillis {
            val result = withTimeoutOrNull(timeoutSeconds * 1000) {
                findJsonDuplicates(directoryPath)
            }

            if (result == null) {
                println("\nПОИСК ПРЕРВАН ПО ТАЙМАУТУ ($timeoutSeconds сек)")
                println("Все корутины отменены")
            } else {
                printResults(result)
            }
        }
        println("Общее время выполнения: ${totalTime}мс")
    }
}

suspend fun findJsonDuplicates(rootPath: String): Map<String, List<File>> {
    return withContext(Dispatchers.IO) {
        val rootDir = File(rootPath)

        if (!rootDir.exists() || !rootDir.isDirectory) {
            println("Ошибка: Директория не существует или не является папкой")
            return@withContext emptyMap()
        }

        println("Поиск JSON файлов...")
        val jsonFiles = findJsonFiles(rootDir)
        println("Найдено JSON файлов: ${jsonFiles.size}")

        if (jsonFiles.isEmpty()) {
            return@withContext emptyMap()
        }

        println("\nВычисление SHA-256 хешей...")

        val hashToFiles = mutableMapOf<String, MutableList<File>>()

        jsonFiles.map { file ->
            async {
                file to computeSha256(file)
            }
        }.awaitAll().forEach { (file, hash) ->
            hashToFiles.getOrPut(hash) { mutableListOf() }.add(file)
        }

        println("Хеши вычислены")

        hashToFiles.filter { it.value.size > 1 }
    }
}

fun findJsonFiles(directory: File): List<File> {
    val result = mutableListOf<File>()

    directory.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            result.addAll(findJsonFiles(file))
        } else if (file.isFile && file.extension.equals("json", ignoreCase = true)) {
            result.add(file)
        }
    }
    return result
}


suspend fun computeSha256(file: File): String = withContext(Dispatchers.IO) {
    try {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)

                yield()
            }
        }

        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        println("Ошибка при чтении файла ${file.name}: ${e.message}")
        "ERROR_${file.path}"
    }
}

fun printResults(duplicates: Map<String, List<File>>) {
    println("РЕЗУЛЬТАТЫ ПОИСКА ДУБЛИКАТОВ:")

    if (duplicates.isEmpty()) {
        println("\nДубликаты не найдены")
        return
    }

    println("\nНайдено групп дубликатов: ${duplicates.size}\n")

    duplicates.entries.forEachIndexed { index, (hash, files) ->
        println("Группа ${index + 1}:")
        println("   Хеш: $hash")
        println("   Файлы (${files.size}):")

        files.forEachIndexed { fileIndex, file ->
            val marker = if (fileIndex == 0) "Оригинал?" else "Копия"
            println("      $marker ${file.path} (${file.length()} байт)")
        }
        println()
    }
}