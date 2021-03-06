package models.block

import kotlinx.serialization.json.*
import schemas.block.BlockValue
import util.get
import util.toMap
import kotlin.collections.set

class PageBlock(blockValue: BlockValue, metadata: JsonObject) {
    var title: String? = null
    val properties: MutableMap<String, JsonElement> = mutableMapOf()

    init {
        blockValue.properties.toMap<Any>().entries.forEach { blockProperty ->
            try {
                val property = metadata.content["properties"]?.jsonObject?.content?.entries?.find { prop ->
                    prop.value.jsonObject["key"]?.content == blockProperty.key
                }
                when (property?.value?.let { (it["type"] as JsonLiteral).content }) {
                    "relation" -> {
                        println(blockProperty.value)
                        try {
                            properties[property.key] = jsonArray {
                                (blockProperty.value as JsonArray).forEach {
                                    +(it[1][0][1] as JsonLiteral)
                                }
                            }
                        } catch (e: Throwable) {
                        }
                    }
                    "text", "select" -> properties[property.key] = (blockProperty.value[0][0] as JsonLiteral)
                    "title" -> title = (blockProperty.value[0][0] as JsonLiteral).content
                    "checkbox" -> properties[property.key] =
                        JsonLiteral((blockProperty.value[0][0] as JsonLiteral).content == "Yes")
                    "date" -> {
                        val data = (blockProperty.value[0][1][0][1] as JsonObject?)?.content
                        properties[property.key] = json {
                            "type" to (data["type"] as JsonLiteral?)?.content
                            "start_date" to (data["start_date"] as JsonLiteral?)?.content
                        }
                    }
                    "file" -> {
                        properties[property.key] = jsonArray {
                            (blockProperty.value as JsonArray).forEach {
                                +json {
                                    "name" to (it[0] as JsonLiteral)
                                    "url" to (it[1][0][1] as JsonLiteral)
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                println(e)
            }
        }
    }
}