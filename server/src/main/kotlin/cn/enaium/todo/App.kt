package cn.enaium.todo

import cn.enaium.todo.model.Result
import cn.enaium.todo.service.*
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class App : CoroutineVerticle() {
  override suspend fun start() {
    val router = Router.router(vertx)
    router.route().handler { cors(it) }

    router.route().handler { auth(it) }

    router.get("/user/:id").coroutineResult { findUser(it) }
    router.put("/user").coroutineResult { saveUser(it) }
    router.put("/user/state").handler(BodyHandler.create()).coroutineResult { createState(it) }
    router.delete("/user/state").coroutineHandler { deleteState(it) }
    router.get("/user/:id/list").coroutineResult { findUserList(it) }

    router.get("/user/list/:id").coroutineResult { findList(it) }
    router.put("/user/list").handler(BodyHandler.create()).coroutineResult { saveList(it) }
    router.delete("/user/list/:id").coroutineResult { deleteList(it) }
    router.get("/user/list/:id/tasks").coroutineResult { findListTasks(it) }

    router.get("/user/list/task/:id").coroutineResult { findTask(it) }
    router.put("/user/list/task").handler(BodyHandler.create()).coroutineResult { saveTask(it) }
    router.delete("/user/list/task/:id").coroutineResult { deleteTask(it) }

    router.route().last().failureHandler { context ->
      val failure: Throwable = context.failure()
      failure.message?.let {
        context.response().end(Result.Builder.fail<Nothing>(message = it))
      } ?: context.next()
    }.failureHandler(ErrorHandler.create(vertx))

    vertx.createHttpServer()
      .requestHandler(router).exceptionHandler { it.printStackTrace() }
      .listen(8888).await()
    println("HTTP server started on port 8888")
  }

  companion object {
    val token = ConcurrentHashMap<UUID, UUID>()
  }
}
