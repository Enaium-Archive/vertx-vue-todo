<script setup lang="ts">
import Initialize from "@/components/Initialize.vue"
</script>

<template>
  <NMessageProvider>
    <Initialize />
  </NMessageProvider>
  <RouterView />
</template>

<style scoped>
</style>
