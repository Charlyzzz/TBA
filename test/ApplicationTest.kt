package com.example

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun respondsPing() {
        withTestApplication({ module(testing = true) }) {

            handleRequest(HttpMethod.Get, "/ping").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("pong", response.content)
                assert(response.contentType().match(ContentType.Text.Plain))
            }
        }
    }

    @Test
    fun authenticateReturnsNoWhenRequestIncorrect() {
        withTestApplication({ module(testing = true) }) {

            handleRequest(HttpMethod.Post, "/authenticate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"who": "erwin", "password": "123"}""")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("No", response.content)
            }
        }
    }

    @Test
    fun authenticateReturnsYesWhenRequestCorrect() {
        withTestApplication({ module(testing = true) }) {

            handleRequest(HttpMethod.Post, "/authenticate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"who": "erwin", "password": "password"}""")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Ok", response.content)
            }
        }
    }

    @Test
    @Ignore
    fun authenticateReturnsXXXWhenInvalid() {
        withTestApplication({ module(testing = true) }) {

            handleRequest(HttpMethod.Post, "/authenticate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{}""")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("No", response.content)
            }
        }
    }
}

