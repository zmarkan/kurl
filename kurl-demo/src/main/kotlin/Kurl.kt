package kurl

import okhttp3.*



//Make the implementation more verbose
typealias PairList = ArrayList<Pair<String, String>>
typealias StringPair = Pair<String, String>

//This is our annotation that prevents us from nesting the wrong things
@DslMarker
annotation class KurlMarker

//Methods!
fun get(path: String, init: KurlRequest.() -> Unit): KurlRequest {
    val request = KurlRequest(
            method = "GET",
            path = path)
    request.init()
    return request
}

fun post(path: String, body: String, init: KurlRequest.() -> Unit): KurlRequest {
    val request = KurlRequest(method= "POST",
            body = body,
            path = path)
    request.init()
    return request
}

@KurlMarker
class KurlRequest(method: String, var body: String? = null, path: String) {

    private var _path: String = path
    private var _headers: KurlHeaders = KurlHeaders()
    private val _method: String = method

    val kurlUrl = KurlUrl(_path)

    //Headers and url components
    fun url (init: (KurlUrl.() -> Unit)): KurlUrl {
        kurlUrl.init()
        return kurlUrl
    }

    fun headers (init: KurlHeaders.() -> Unit): KurlHeaders {
        _headers.init()
        return _headers
    }

    // add header authorization infix
    val add
        get(): KurlRequest = this

    infix fun header(add: StringPair): Unit {
        _headers.headers.add(add)
    }


    //The main logic part that takes all the components and executes the OKhttpRequest
    fun execute(client: OkHttpClient = OkHttpClient()): Response? {

        var requestBuilder = okhttp3.Request.Builder()
        requestBuilder.url(kurlUrl.url)

        for (header in _headers.headers){
            requestBuilder.addHeader(header.first, header.second)
        }

        var requestBody: RequestBody? = null

        body?.let {
            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body)
        }

        requestBuilder.method(_method, requestBody)
        return client.newCall(requestBuilder.build()).execute()
    }
}


//Headers
@KurlMarker
class KurlHeaders{

    val headers = PairList()
        get

    // This allows us to do:
    // "Content-type"["application/json"]
    //Because we put it in the scope of KurlHeaders class it only works in here, and our query params still work.
    operator fun String.get(other:String){
        headers.add(Pair(this,other))
    }

    //BAD - don't do that :)
    operator fun String.minus(other: String) {
        headers.add(Pair(this, other))
    }
}




//URL appending, Query params, and path:

@KurlMarker
class KurlUrl(base: String) {
    var base: String = base
    var urlBuilder = HttpUrl.parse(base)?.newBuilder()

    val url
        get() = urlBuilder!!.build()

    fun query(init: (KurlQueryParams.() -> Unit)): KurlQueryParams {

        val queryParams = KurlQueryParams()
        queryParams.init()

        for (param in queryParams.params){
            urlBuilder?.addQueryParameter(param.first, param.second)
        }
        return queryParams
    }

    fun path(init: (KurlPath.() -> Unit)): KurlPath {

        val path = KurlPath()
        path.init()
        for(segment in path.pathParams) urlBuilder?.addPathSegment(segment)

        return path
    }
}

@KurlMarker
class KurlQueryParams {
    private val _params = PairList()
    val params
        get() = _params

    operator fun String.get(value: String): Unit {
        _params.add(this to value)
    }
}

class KurlPath {
    private val _pathParams = arrayListOf<String>()
    val pathParams
        get() = _pathParams

    operator fun String.div(next: String): String {

        if(_pathParams.isEmpty()) _pathParams.add(this)
        _pathParams.add(next)

        return next
    }
}
