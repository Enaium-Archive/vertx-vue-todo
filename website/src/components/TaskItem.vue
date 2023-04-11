<script setup lang="ts">
import { ref } from "vue"
import { ITaskItem } from "@/util/model"
import { put } from "@/util/request"
import { useUserStore } from "@/store"
import { NButton } from "naive-ui"

const props = defineProps<{
  item: ITaskItem
}>()

const userStore = useUserStore()
const content = ref("")
const showMenu = ref(false)
const editMode = ref(false)


const save = () => {
  put<ITaskItem>("/user/list/task", {
    id: props.item.id,
    userId: userStore.id,
    listId: props.item.listId,
    content: content.value || props.item.content,
    important: props.item.important,
    finished: props.item.finished
  }).then(r => {
    props.item.id = r.metadata.id
    props.item.content = r.metadata.content
  })
}

const enter = () => {
  save()
  editMode.value = false
}


const esc = () => {
  editMode.value = false
}

const edit = () => {
  editMode.value = true
  content.value = props.item.content!
  showMenu.value = false
}

const remove = () => {
  showMenu.value = false
}

const finish = () => {
  props.item.finished = !props.item.finished
  save()
}

const important = () => {
  props.item.important = !props.item.important
  save()
}
</script>

<template>
  <NCard @contextmenu.prevent.stop="showMenu = true">
    <div style="display: flex;align-items: center;gap: 5px" v-if="props.item.id && !editMode">
      <NButton @click="finish" :type="item.finished ? 'primary' : 'default'" circle />
      <NEllipsis style="flex-grow: 1">
        {{ props.item.content }}
      </NEllipsis>
      <NButton @click="important" :type="item.important ? 'warning' : 'default'" circle />
    </div>
    <NInput v-model:value="content" @keyup.enter="enter" @keyup.esc="esc" v-else />
  </NCard>
  <NModal
    v-model:show="showMenu"
    preset="dialog"
    title="Menu"
    content="Menu"
  >
    <NButtonGroup style="width: 100%" vertical>
      <NButton type="primary" @click="edit">Edit</NButton>
      <NButton type="error" @click="remove">Remove</NButton>
    </NButtonGroup>
  </NModal>
</template>

<style scoped>

</style>