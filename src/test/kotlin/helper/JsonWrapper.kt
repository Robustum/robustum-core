package helper

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kotest.matchers.shouldBe

infix fun JsonElement.shouldBe(builder: GsonObjectWrapper.() -> Unit) {
    val expected = GsonObjectWrapper().apply(builder).jsonObject
    this shouldBe expected
}

class GsonObjectWrapper(val jsonObject: JsonObject = JsonObject()) {
    operator fun String.invoke(value: String) {
        jsonObject.addProperty(this, value)
    }

    operator fun String.invoke(value: Number) {
        jsonObject.addProperty(this, value)
    }

    operator fun String.invoke(value: Boolean) {
        jsonObject.addProperty(this, value)
    }

    operator fun String.invoke(builder: GsonObjectWrapper.() -> Unit) {
        val innerObject = GsonObjectWrapper()
        innerObject.builder()
        jsonObject.add(this, innerObject.jsonObject)
    }
}
