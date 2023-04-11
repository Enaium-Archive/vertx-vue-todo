<script setup lang="ts">
import { ref } from "vue"
import { ITaskItem, ITaskListItem } from "@/util/model"
import { useUserStore } from "@/store"
import { get } from "@/util/request"
import TaskListItem from "@/components/TaskListItem.vue"
import TaskItem from "@/components/TaskItem.vue"

const userStore = useUserStore()

const list = ref<ITaskListItem[]>([])
const tasks = ref<ITaskItem[]>([])

get<ITaskListItem[]>(`/user/${userStore.id}/list`).then(r => {
  list.value = r.metadata
  if (0 != r.metadata?.length) {
    const value = r.metadata[0].id!
    currentList.value = value
    get<ITaskListItem[]>(`/user/list/${value}/tasks`).then(r => {
      tasks.value = r.metadata
    })
  }
})

const currentList = ref("")

const selectTaskListItem = (id: string) => {
  if (id) {
    currentList.value = id
    get<ITaskListItem[]>(`/user/list/${id}/tasks`).then(r => {
      tasks.value = r.metadata
    })
  }
}

const newList = () => {
  list.value?.push(<ITaskListItem>{})
}

const newTask = () => {
  if (currentList.value) {
    tasks.value?.push(<ITaskItem>{ listId: currentList.value })
  } else {
    window.$message.error("Please select a task-list")
  }
}
</script>

<template>
  <NSpace justify="center" align="center" style="height: 100vh">
    <NCard style="width:60rem;height: 40rem">
      <div style="position:relative;height:100%">
        <NScrollbar style="position: absolute; width: 20%; height:94%">
          <TaskListItem v-for="item in list" :key="item.id" :item="item" :current="currentList"
                        @click="selectTaskListItem(item.id!)" />
        </NScrollbar>
        <NButton type="primary" style="position:absolute;top:94%;width: 180px " @click="newList">New List</NButton>
        <NScrollbar style="position: absolute; left: 20%;width:80%;height: 94%" vertical>
          <TaskItem v-for="item in tasks" :key="item.id" :item="item" />
        </NScrollbar>
        <NButton type="primary" style="position:absolute;top:94%;left:80%;width: 180px " @click="newTask">
          New Task
        </NButton>
      </div>
    </NCard>
  </NSpace>
</template>

<style scoped>

</style>
