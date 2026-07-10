const handlers = {
  toast: () => {},
  confirm: async () => false
}

const feedback = {
  toast(message, type = 'info') {
    return handlers.toast(message, type)
  },
  confirm(message) {
    return handlers.confirm(message)
  }
}

export function setFeedbackHandlers(nextHandlers = {}) {
  if (!nextHandlers) nextHandlers = {}
  handlers.toast = nextHandlers.toast || handlers.toast
  handlers.confirm = nextHandlers.confirm || handlers.confirm
}

export default feedback
