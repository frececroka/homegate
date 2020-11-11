package ch.homegate

import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Type
import java.nio.file.Path

class JsonDb(private val root: Path) {

    private val gson = Gson()

    init {
        root.toFile().mkdirs()
    }

    fun child(name: String) = JsonDb(root.resolve(name))

    fun <T> get(id: String, type: Type): T? =
        try {
            val representative = representative(id)
            fromFile<T>(representative, type)
        } catch (_: FileNotFoundException) {
            null
        }

    fun <T> set(id: String, value: T) =
        representative(id).writer().use { gson.toJson(value, it) }

    fun <T> find(predicate: (T) -> Boolean, type: Type): Pair<String, T>? =
        findAll(type, predicate).singleOrNull()

    fun <T> findAll(type: Type, predicate: (T) -> Boolean): List<Pair<String, T>> =
        root.toFile().listFiles()!!.toList()
            .map { Pair(it.name, fromFile<T>(it, type)) }
            .filter { (_, file) -> predicate(file) }

    private fun representative(id: String) = root.resolve(id).toFile()

    private fun <T> fromFile(representative: File, type: Type) =
        representative.reader().use { gson.fromJson<T>(it, type) }

}
