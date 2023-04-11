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

import cn.enaium.todo.*
import cn.enaium.todo.model.Result
import cn.enaium.todo.model.entity.*
import cn.enaium.todo.model.input.UserInput
import io.vertx.ext.web.RoutingContext
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

/**
 * @author Enaium
 */
fun findUser(context: RoutingContext): Result<User?> {
  context.data()
  val userId = context.request().getParam("id").toUUID()
  return Result.Builder.success(metadata = sqlClient.entities.findById(User::class, userId))
}

fun saveUser(context: RoutingContext): Result<Nothing?> {
  val asType = context.body().asType(UserInput::class)
  sqlClient.entities.save(asType) {
    setMode(SaveMode.INSERT_ONLY)
  }
  return Result.Builder.success()
}

fun updateUser(context: RoutingContext): Result<Nothing?> {
  val asType = context.body().asType(UserInput::class)
  checkOwner(context.request().token(), asType.id!!)
  sqlClient.entities.save(asType) {
    setMode(SaveMode.UPDATE_ONLY)
  }
  return Result.Builder.success()
}

fun createState(context: RoutingContext): Result<Any?> {
  val asType = context.body().asType(UserInput::class)

  if (asType.username == null) {
    return Result.Builder.fail(message = "The username can't be null")
  }

  if (asType.password == null) {
    return Result.Builder.fail(message = "The password can't be null")
  }

  sqlClient.createQuery(User::class) {
    where(table.username eq asType.username, table.password eq asType.password)
    select(table)
  }.fetchOneOrNull()?.let {

    val token = UUID.randomUUID()

    App.token[token] = it.id

    return Result.Builder.success(
      metadata = mapOf(
        "id" to it.id,
        "token" to token
      )
    )
  } ?: return Result.Builder.fail(message = "The user doesn't exist")
}

fun deleteState(context: RoutingContext): Result<Nothing?> {
  App.token.remove(context.request().token())
  return Result.Builder.success()
}

fun findUserList(context: RoutingContext): Result<List<TaskList>?> {
  val id = context.request().getParam("id").toUUID()
  return Result.Builder.success(metadata = sqlClient.createQuery(TaskList::class) {
    where(table.userId eq id)
    orderBy(table.createdTime.asc())
    select(table)
  }.execute())
}
