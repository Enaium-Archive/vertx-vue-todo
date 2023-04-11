<!--
  - Copyright (c) 2023 Enaium
  -
  - Permission is hereby granted, free of charge, to any person obtaining a copy
  - of this software and associated documentation files (the "Software"), to deal
  - in the Software without restriction, including without limitation the rights
  - to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  - copies of the Software, and to permit persons to whom the Software is
  - furnished to do so, subject to the following conditions:
  -
  - The above copyright notice and this permission notice shall be included in all
  - copies or substantial portions of the Software.
  -
  - THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  - IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  - FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  - AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  - LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  - OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  - SOFTWARE.
  -
  -->

<script setup lang="ts">
import { ITaskListItem } from "@/util/model"
import { NButton, NEllipsis } from "naive-ui"
import { computed, ref } from "vue"
import { put } from "@/util/request"
import { useUserStore } from "@/store"
import http from "@/util/http"

const props = defineProps<{
  item: ITaskListItem
  current: string
}>()

const userStore = useUserStore()
const title = ref("")
const showMenu = ref(false)
const editMode = ref(false)

const select = computed(() => props.current == props.item.id)

const enter = () => {
  put<ITaskListItem>("/user/list", { id: props.item.id, userId: userStore.id, title: title.value }).then(r => {
    props.item.id = r.metadata.id
    props.item.title = r.metadata.title
  })
  editMode.value = false
}

const esc = () => {
  editMode.value = false
}

const edit = () => {
  editMode.value = true
  title.value = props.item.title!
  showMenu.value = false
}

const remove = () => {
  http.delete(`/user/list/${props.item.id}`).then(r => {
    if (r.data.code === 200) {
      window.$message.success("Success")
    }
  })
  showMenu.value = false
}
</script>

<template>
  <div>
    <NButton :type="select ? 'primary' : 'default'" style="width: 180px"
             @contextmenu.prevent.stop="showMenu = true" v-if="props.item.id && !editMode">
      <NEllipsis>{{ props.item.title }}</NEllipsis>
    </NButton>
    <NInput v-model:value="title" @keyup.enter="enter" @keyup.esc="esc" v-else />
    <NModal
      v-model:show="showMenu"
      preset="dialog"
      title="Menu"
      content="Menu?"
    >
      <NButtonGroup style="width: 100%" vertical>
        <NButton type="primary" @click="edit">Edit</NButton>
        <NPopconfirm @positive-click="remove" @negative-click="showMenu = false">
          <template #trigger>
            <NButton type="error">Remove</NButton>
          </template>
          Do you want to remove this task-list?
        </NPopconfirm>
      </NButtonGroup>
    </NModal>
  </div>
</template>

<style scoped>

</style>
