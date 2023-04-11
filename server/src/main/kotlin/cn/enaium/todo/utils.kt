/*
 * Copyright (c) 2023 Enaium
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package cn.enaium.todo

import cn.enaium.todo.model.Result
import cn.enaium.todo.model.entity.ENTITY_MANAGER
import cn.enaium.todo.model.interceptor.BaseEntityDraftInterceptor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RequestBody
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.babyfish.jimmer.jackson.ImmutableModule
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.mariadb.jdbc.MariaDbDataSource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Enaium
 */
val sqlClient = newKSqlClient {
  setConnectionManager {
    MariaDbDataSource("jdbc:mariadb://192.168.56.101/todo").getConnection("root", "root").use {
      proceed(it)
    }
  }
  setEntityManager(ENTITY_MANAGER)
  addScalarProvider(ScalarProvider.UUID_BY_STRING)
  addDraftInterceptor(BaseEntityDraftInterceptor())
}

val jackson: ObjectMapper =
  jacksonObjectMapper().registerModule(ImmutableModule()).registerModule(JavaTimeModule().apply {
    addSerializer(
      LocalDateTime::class.java,
      LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    )
  })

fun HttpServerResponse.end(result: Result<*>) {
  this.putHeader(HttpHeaderNames.CONTENT_TYPE, "${HttpHeaderValues.APPLICATION_JSON};charset=utf-8")
    .end(jackson.writeValueAsString(result))
}

fun <T : Any> RequestBody.asType(type: KClass<T>): T {
  return jackson.readValue(asString(), type.java)
}

@OptIn(DelicateCoroutinesApi::class)
fun Route.coroutineResult(fn: suspend (RoutingContext) -> Result<*>): Route {
  val v = Vertx.currentContext().dispatcher()
  handler { ctx ->
    GlobalScope.launch(v) {
      try {
        ctx.response().end(fn(ctx))
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
  return this
}

@OptIn(DelicateCoroutinesApi::class)
fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Route {
  val v = Vertx.currentContext().dispatcher()
  handler { ctx ->
    GlobalScope.launch(v) {
      try {
        fn(ctx)
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
  return this
}

fun String.toUUID(): UUID = UUID.fromString(this)

fun checkOwner(token: UUID, id: UUID): Boolean {
  return App.token[token] == id
}

fun HttpServerRequest.token(): UUID {
  return this.getHeader(HttpHeaderNames.AUTHORIZATION).toUUID()
}
