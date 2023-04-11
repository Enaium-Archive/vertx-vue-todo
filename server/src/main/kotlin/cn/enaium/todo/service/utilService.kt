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

package cn.enaium.todo.service

import cn.enaium.todo.App
import cn.enaium.todo.end
import cn.enaium.todo.model.Result
import cn.enaium.todo.toUUID
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler

/**
 * @author Enaium
 */
fun auth(context: RoutingContext) {
  if (context.request().path().equals("/user/state")) {
    context.next()
  } else {
    val token = context.request().getHeader(HttpHeaderNames.AUTHORIZATION)

    if (token.isNullOrBlank())
      context.response()
        .end(
          Result.Builder.fail<Nothing?>(
            code = 2001,
            message = "The token is blank, Please will be 'token' add to '${HttpHeaderNames.AUTHORIZATION}' in the request header."
          )
        )


    if (App.token.containsKey(token.toUUID())) {
      context.next()
    } else {
      context.response().end(Result.Builder.fail<Nothing?>(code = 2001, message = "The token doesn't exist"))
    }
  }
}

fun cors(context: RoutingContext) {
  CorsHandler.create()
    .allowCredentials(true)
    .allowedHeader("*")
    .allowedMethods(
      setOf(
        HttpMethod.GET,
        HttpMethod.POST,
        HttpMethod.PUT,
        HttpMethod.PATCH,
        HttpMethod.DELETE,
        HttpMethod.OPTIONS
      )
    )
    .addOrigin("*").handle(context)
}
