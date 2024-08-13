package meeruman

import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

enum class BodyType {
    NONE,
    FORMDATA,
    XWWWFORM,
    RAW,
    BINARY
}

data class Quadruple<A,B,C,D>(var first: A, var second: B, var third: C, var fourth: D) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

typealias MResponse = Quadruple<String, Int, Long, Map<String,List<String>>>

class HttpClient {
    private val client = OkHttpClient()

    private fun requestWithoutBody(url: String, header: Map<String, String>, method: String): MResponse {
        val request = Request.Builder()
                .url(url.toHttpUrl())
                .headers(header.toHeaders())
                .method(method, null)
                .build()

        val now = System.currentTimeMillis()
        client.newCall(request).execute().use {
            rep ->
            return MResponse(rep.body!!.string(), rep.code,System.currentTimeMillis() - now, rep.headers.toMultimap())
        }
    }

    private fun requestWithBody(url: String, header: Map<String, String>, body: Map<String, String>, bodyType: BodyType, method: String)
    : MResponse {
        val requestBody: RequestBody = when (bodyType) {
            BodyType.NONE -> FormBody.Builder().build()
            BodyType.FORMDATA -> {
                val multipartBodyBuilder = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                body.map {
                    val keys = it.key.split(":;")
                    if (keys.last() == "File") {
                        val path = Paths.get(it.value)
                        val mediaType = Files.probeContentType(path).toMediaTypeOrNull()
                        val file = File(it.value)
                        multipartBodyBuilder.addFormDataPart( keys.dropLast(1).joinToString(""),
                                path.fileName.toString(),
                                file.asRequestBody(mediaType))
                    } else {
                        multipartBodyBuilder.addFormDataPart(keys.dropLast(1).joinToString(""),
                                it.value)
                    }
                }
                multipartBodyBuilder.build()
            }
            BodyType.XWWWFORM -> {
                val formBodyBuilder = FormBody.Builder()
                body.map { formBodyBuilder.add(it.key, it.value) }
                formBodyBuilder.build()
            }
            BodyType.RAW -> body["raw"]!!.toRequestBody("text/plain".toMediaTypeOrNull())
            BodyType.BINARY -> {
                val file = File(body["binary"]!!)
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            }
        }

        val request = Request.Builder()
                .url(url.toHttpUrl())
                .headers(header.toHeaders())
                .method(method, requestBody)
                .build()
        val now = System.currentTimeMillis()
        client.newCall(request).execute().use {
            rep ->
            return Quadruple(rep.body!!.string(), rep.code,System.currentTimeMillis() - now, rep.headers.toMultimap())
        }
    }

    fun get(url: String, header: Map<String, String>): MResponse {
        return requestWithoutBody(url, header, "GET")
    }

    fun head(url: String, header: Map<String, String>): MResponse {
        return requestWithoutBody(url, header, "HEAD")
    }

    fun options(url: String, header: Map<String, String>): MResponse {
        return requestWithoutBody(url, header, "OPTIONS")
    }

    fun post(url: String, header: Map<String, String>, body: Map<String, String>, bodyType: BodyType):
            Quadruple<String, Int, Long, Map<String,List<String>>> {
        return requestWithBody(url, header, body, bodyType, "POST")
    }

    fun put(url: String, header: Map<String, String>, body: Map<String, String>, bodyType: BodyType):
            Quadruple<String, Int, Long, Map<String,List<String>>> {
        return requestWithBody(url, header, body, bodyType, "PUT")
    }

    fun patch(url: String, header: Map<String, String>, body: Map<String, String>, bodyType: BodyType):
            Quadruple<String, Int, Long, Map<String,List<String>>> {
        return requestWithBody(url, header, body, bodyType, "PATCH")
    }

    fun delete(url: String, header: Map<String, String>, body: Map<String, String>, bodyType: BodyType):
            Quadruple<String, Int, Long, Map<String,List<String>>> {
        return requestWithBody(url, header, body, bodyType, "DELETE")
    }
}