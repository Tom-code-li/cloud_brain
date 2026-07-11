<template>
  <div>
    <transition name="feedback-fade">
      <div v-if="toast.visible" class="feedback-toast" :class="toast.type">
        {{ toast.message }}
      </div>
    </transition>

    <transition name="feedback-fade">
      <div v-if="confirmState.visible" class="feedback-mask">
        <div class="feedback-dialog">
          <div class="feedback-dialog__message">{{ confirmState.message }}</div>
          <div class="feedback-dialog__actions">
            <button class="feedback-btn secondary" @click="resolveConfirm(false)">取消</button>
            <button class="feedback-btn primary" @click="resolveConfirm(true)">确定</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  data() {
    return {
      toast: {
        visible: false,
        message: '',
        type: 'info',
        timer: null
      },
      confirmState: {
        visible: false,
        message: '',
        resolve: null
      }
    }
  },
  methods: {
    show(message, type = 'info') {
      if (this.toast.timer) clearTimeout(this.toast.timer)
      this.toast.message = message
      this.toast.type = type
      this.toast.visible = true
      this.toast.timer = setTimeout(() => {
        this.toast.visible = false
      }, 2200)
    },
    async confirm(message) {
      this.confirmState.message = message
      this.confirmState.visible = true
      return new Promise(resolve => {
        this.confirmState.resolve = resolve
      })
    },
    resolveConfirm(value) {
      if (this.confirmState.resolve) {
        this.confirmState.resolve(value)
      }
      this.confirmState.resolve = null
      this.confirmState.visible = false
    }
  }
}
</script>
