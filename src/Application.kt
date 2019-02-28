package com.example

import com.example.tos.AuthenticationRequest
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

object Cities : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 50)
}

data class City(val id: Int, val name: String)

suspend fun <T> dbOperation(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Cities)

        Cities.insert {
            it[name] = "St. Petersburg"
        }

        Cities.insert {
            it[name] = "Munich"
        }

        Cities.insert {
            it[name] = "Prague"
        }

    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        gson { }
    }

    routing {
        get("/ping") {
            call.respondText("pong")
        }

        get("/search") {
            val cities = cities()
            call.respond(cities)
        }

        post("/authenticate") {

            val request = call.receive<AuthenticationRequest>()
            when (request) {
                AuthenticationRequest("erwin", "password") -> call.respondText("Ok")
                else -> call.respondText("No")
            }
        }
    }
}

private suspend fun cities(): List<City> {
    return dbOperation {
        val r = Cities.join(Cities.alias(), JoinType.FULL, additionalConstraint = { Cities.id.eq(Cities.id) })
        Cities.select { Cities.id.lessEq(10) }.map { it.toCity() }
    }
}

fun ResultRow.toCity() = City(this[Cities.id], this[Cities.name])
