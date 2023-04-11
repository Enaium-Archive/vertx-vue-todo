<script setup lang="ts">
import { useUserStore } from "@/store"
import { reactive, ref } from "vue"
import { FormInst } from "naive-ui"
import { put } from "@/util/request"

const userStore = useUserStore()

const loginRef = ref<FormInst | null>(null)

const loginForm = reactive({
  username: "",
  password: ""
})

const loginRule = {
  username: [
    {
      required: true,
      type: "string",
      message: "Please input your username",
      trigger: ["blur", "input"]
    }
  ],
  password: [
    {
      required: true,
      type: "string",
      message: "Please input your password",
      trigger: ["blur", "input"]
    }
  ]
}

const loginSubmit = () => {
  loginRef.value?.validate((errors) => {
    if (!errors) {
      put<{ id: string, token: string }>("/user/state", loginForm).then(r => {
        if (r.code == 200) {
          userStore.setId(r.metadata.id)
          userStore.setToken(r.metadata.token)
          window.$message.success("Login success")
          window.$router.push({ path: "/" })
        }
      })
    } else {
      window.$message.error(errors[0][0].message!)
    }
  })
}
</script>

<template>
  <NSpace justify="center" align="center" style="height:100vh">
    <NCard title="Login" style="width: 400px">
      <NForm ref="loginRef" :model="loginForm" :rules="loginRule">
        <NFormItem path="username" label="Username">
          <NInput v-model:value="loginForm.username" />
        </NFormItem>
        <NFormItem path="password" label="Password">
          <NInput type="password" v-model:value="loginForm.password" @keyup.enter="loginSubmit" />
        </NFormItem>
        <NButton style="width: 100%" type="primary" @click="loginSubmit">Login</NButton>
      </NForm>
    </NCard>
  </NSpace>
</template>

<style scoped>

</style>
