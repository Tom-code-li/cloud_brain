<template>
  <router-view/>
  <FeedbackLayer ref="feedbackLayer" />
</template>

<script>
import FeedbackLayer from './components/FeedbackLayer.vue'
import { setFeedbackHandlers } from './utils/feedback'

export default {
  components: { FeedbackLayer },
  mounted() {
    setFeedbackHandlers({
      toast: (message, type) => this.$refs.feedbackLayer?.show(message, type),
      confirm: message => this.$refs.feedbackLayer?.confirm(message) || Promise.resolve(false)
    })
  }
}
</script>

<style>
html,
body,
div,
span,
h1,
h2,
h3,
h4,
h5,
h6,
ul,
ol,
li,
input,
button {
  margin: 0;
  padding: 0;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif;
}
</style>
