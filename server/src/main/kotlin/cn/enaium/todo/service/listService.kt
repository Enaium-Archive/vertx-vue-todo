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
import cn.enaium.todo.model.input.TaskListInput
import io.vertx.ext.web.RoutingContext
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq

/**
 * @author Enaium
 */
fun findList(context: RoutingContext): Result<TaskList?> {
  val id = context.request().getParam("id").toUUID()
  val findById = sqlClient.entities.findById(TaskList::class, id)
  return Result.Builder.success(metadata = findById)
}

fun saveList(context: RoutingContext): Result<TaskList?> {
  val asType = context.body().asType(TaskListInput::class)
  checkOwner(context.request().token(), asType.userId)
  val save = sqlClient.entities.save(asType)
  return Result.Builder.success(metadata = save.modifiedEntity)
}

fun deleteList(context: RoutingContext): Result<Nothing?> {
  val id = context.request().getParam("id").toUUID()
  sqlClient.entities.delete(TaskList::class, id) {
    setDissociateAction(Task::list, DissociateAction.DELETE)
  }
  return Result.Builder.success()
}

fun findListTasks(context: RoutingContext): Result<List<Task>?> {
  val id = context.request().getParam("id").toUUID()
  return Result.Builder.success(metadata = sqlClient.createQuery(Task::class) {
    where(table.listId eq id)
    orderBy(table.important.desc(), table.finished.asc(), table.createdTime.asc())
    select(table)
  }.execute())
}
