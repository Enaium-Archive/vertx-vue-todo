import { createRouter, createWebHistory } from "vue-router"
import Login from "@/pages/Login.vue"
import Register from "@/pages/Register.vue"
import Todo from "@/pages/Todo.vue"

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      name: "login",
      path: "/login",
      component: Login
    },
    {
      name: "register",
      path: "/register",
      component: Register
    },
    {
      name: "todo",
      path: "/",
      component: Todo
    }
  ]
})