import kurl.StringPair
import okhttp3.*

val requestPath: String = TODO("create a new postbin - http://postb.in/ ")
val authorization = StringPair("Authorization", "Bearer: 123456")
val username = "zan"

val payload: String = """
    {
        "name": "zan",
        "occupation": "Developer Evangelist",
        "company": "Pusher",

        "interests": [
            {
                "interest": "Kotlin",
                "description": "Kotlin is a powerful multi-purpose programming language from JetBrains"
            },
            {
                "interest": "beer",
                "description": "Beer is malt, hops, water, a little bit of yeast, and a whole lot of magic"
            }
        ]
    }
    """.trimIndent()

fun okHttpGetRequest(): Request {
    val request = okhttp3.Request.Builder()
            .addHeader("Authorization", "Bearer: 23344")
            .get()
            .url(requestPath)
            .build()

    return request
}



fun okHttpPostRequest(): Request {

    val JSON = MediaType.parse("application/json; charset=utf-8")
    val requestBody = RequestBody.create(JSON, payload)
    val request = okhttp3.Request.Builder()
            .addHeader("Authorization", "Bearer: 23344")
            .post(requestBody)
            .url(requestPath)
            .build()

    return request
}

fun main(args: Array<String>) {

    val client = OkHttpClient()

    //Non - DSL examples of GET and POST calls:
    println(client.newCall(okHttpGetRequest()).execute())
    println(client.newCall(okHttpPostRequest()).execute())


    //DSL example of GET call:

    val resp =

            kurl.get(requestPath) {

                url {
                    query {
                        "name"["zan"]
                        "message"["hello, world!"]
                    }
                    path {
                        "users" / username / "interests"
                    }
                }
                headers {
                    "Authorization"["Bearer 12345"]
                    "Message"["Hello, Kotlin!"]


                }
            }.execute()
    println(resp)


    //DSL example of post call:
    val postResp =

        kurl.post(requestPath, payload) {

            add header authorization

            url {
                query {
                    "name"["zan"]
                    "message"["hello, world!"]
                }
                path {
                    "this" / "is" / "a" / "path"
                }
            }
            headers {
                "Authorization"["Bearer 12345"]
                "Message"["Hello, Kotlin!"]
            }
    }.execute()


    println(postResp)






}




