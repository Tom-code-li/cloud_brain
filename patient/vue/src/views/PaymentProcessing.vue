<template>
  <div class="patient-page payment-page">
    <div class="patient-topbar">
      <span class="patient-back"></span>
      <span class="patient-title">支付中</span>
      <span></span>
    </div>

    <div class="payment-wrap">
      <div class="payment-panel patient-card">
        <div class="pay-spinner"></div>
        <h2>{{ statusText }}</h2>
        <p>{{ channelName }} · ¥{{ amount }}</p>
        <div class="progress-track">
          <div class="progress-fill"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import feedback from '@/utils/feedback'

export default {
  data() {
    return {
      statusText: '正在支付',
      timer: null
    }
  },
  computed: {
    feeOrderId() {
      return this.$route.query.feeOrderId
    },
    channelName() {
      return this.$route.query.channelName || '在线支付'
    },
    amount() {
      return this.$route.query.amount || '0.00'
    }
  },
  mounted() {
    if (!this.feeOrderId) {
      feedback.toast('缺少缴费单号')
      this.$router.replace('/patient/fees')
      return
    }
    this.timer = setTimeout(this.finishPay, 3000)
  },
  beforeUnmount() {
    if (this.timer) clearTimeout(this.timer)
  },
  methods: {
    async finishPay() {
      try {
        const res = await axios.post(`/api/fee/pay/${this.feeOrderId}`)
        if (res.data.success) {
          this.statusText = '支付成功'
          feedback.toast('支付成功')
          this.$router.replace('/patient/fees')
        } else {
          feedback.toast(res.data.message || '支付失败')
          this.$router.replace('/patient/fees')
        }
      } catch (e) {
        feedback.toast(e.response?.data?.message || '支付失败')
        this.$router.replace('/patient/fees')
      }
    }
  }
}
</script>

<style scoped>
.payment-page {
  min-height: 100%;
}

.payment-wrap {
  min-height: calc(100vh - 54px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.payment-panel {
  width: 100%;
  max-width: 360px;
  padding: 28px 22px;
  text-align: center;
}

.payment-panel h2 {
  margin: 16px 0 8px;
  color: var(--ink-strong);
  font-size: 22px;
}

.payment-panel p {
  margin: 0;
  color: var(--ink-muted);
  font-size: 14px;
}

.pay-spinner {
  width: 58px;
  height: 58px;
  margin: 0 auto;
  border-radius: 50%;
  border: 5px solid var(--medical-blue-soft);
  border-top-color: var(--medical-blue);
  animation: spin 0.9s linear infinite;
}

.progress-track {
  height: 8px;
  margin-top: 24px;
  border-radius: 999px;
  background: #edf4fb;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, var(--medical-blue), var(--medical-green));
  animation: paying 3s linear forwards;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes paying {
  from { width: 0; }
  to { width: 100%; }
}
</style>
