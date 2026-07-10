<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.back()" class="patient-back">返回</span>
      <span class="patient-title">修改个人信息</span>
      <span></span>
    </div>

    <div class="patient-content change-wrap">
      <section class="change-card patient-card">
        <div class="change-hero">
          <div class="change-icon">改</div>
          <div>
            <h2>个人信息维护</h2>
            <p>手机号、地址和登录密码都可以在这里修改</p>
          </div>
        </div>

        <div class="form-block">
          <div class="section-title">联系方式</div>
          <label class="form-item">
            <span>手机号</span>
            <input v-model.trim="profileForm.phone" type="text" class="form-input" placeholder="请输入手机号" />
          </label>
          <label class="form-item">
            <span>紧急联系人</span>
            <input v-model.trim="profileForm.emergencyContact" type="text" class="form-input" placeholder="未设置" />
          </label>
          <label class="form-item">
            <span>紧急联系电话</span>
            <input v-model.trim="profileForm.emergencyPhone" type="text" class="form-input" placeholder="未设置" />
          </label>
          <label class="form-item">
            <span>地址</span>
            <textarea v-model.trim="profileForm.address" class="form-input textarea-input" placeholder="请输入地址"></textarea>
          </label>
          <button class="patient-button submit-btn" :disabled="profileSubmitting" @click="submitProfile">
            {{ profileSubmitting ? '保存中...' : '保存个人信息' }}
          </button>

          <div class="section-title password-title">登录密码</div>
          <label class="form-item">
            <span>原密码</span>
            <input v-model="form.oldPassword" type="password" class="form-input" placeholder="请输入原密码" />
          </label>
          <label class="form-item">
            <span>新密码</span>
            <input v-model="form.newPassword" type="password" class="form-input" placeholder="请输入新密码" />
          </label>
          <label class="form-item">
            <span>确认新密码</span>
            <input v-model="form.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" />
          </label>
          <button class="patient-button submit-btn" :disabled="submitting" @click="submitPassword">
            {{ submitting ? '提交中...' : '确认修改' }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import feedback from '@/utils/feedback'

export default {
  data() {
    return {
      submitting: false,
      profileSubmitting: false,
      patientInfo: null,
      profileForm: {
        phone: '',
        emergencyContact: '',
        emergencyPhone: '',
        address: ''
      },
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  async mounted() {
    await this.loadPatientInfo()
  },
  methods: {
    async loadPatientInfo() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) {
        feedback.toast('登录信息已失效，请重新登录')
        this.$router.push('/')
        return
      }
      try {
        const res = await axios.get(`/api/patient/info/${userId}`)
        if (res.data.success) {
          this.patientInfo = res.data.data || {}
          this.profileForm.phone = this.patientInfo.phone || ''
          this.profileForm.emergencyContact = this.patientInfo.emergencyContact || ''
          this.profileForm.emergencyPhone = this.patientInfo.emergencyPhone || ''
          this.profileForm.address = this.patientInfo.address || ''
        } else {
          feedback.toast(res.data.message || '获取个人信息失败')
        }
      } catch (e) {
        feedback.toast(e.response?.data?.message || '获取个人信息失败，请稍后再试')
      }
    },
    async submitProfile() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) {
        feedback.toast('登录信息已失效，请重新登录')
        this.$router.push('/')
        return
      }
      if (!this.patientInfo?.patientId) return feedback.toast('未找到患者信息')
      if (!this.profileForm.phone) return feedback.toast('请输入手机号')

      this.profileSubmitting = true
      try {
        const res = await axios.put('/api/patient/update', {
          patientId: this.patientInfo.patientId,
          userId: Number(userId),
          phone: this.profileForm.phone,
          emergencyContact: this.profileForm.emergencyContact || null,
          emergencyPhone: this.profileForm.emergencyPhone || null,
          address: this.profileForm.address || null
        })
        if (res.data.success) {
          feedback.toast(res.data.message || '保存成功')
          await this.loadPatientInfo()
        } else {
          feedback.toast(res.data.message || '保存失败')
        }
      } catch (e) {
        feedback.toast(e.response?.data?.message || '保存失败，请稍后再试')
      } finally {
        this.profileSubmitting = false
      }
    },
    async submitPassword() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) {
        feedback.toast('登录信息已失效，请重新登录')
        this.$router.push('/')
        return
      }
      if (!this.form.oldPassword.trim()) return feedback.toast('请输入原密码')
      if (!this.form.newPassword.trim()) return feedback.toast('请输入新密码')
      if (this.form.newPassword !== this.form.confirmPassword) return feedback.toast('两次新密码不一致')

      this.submitting = true
      try {
        const res = await axios.post('/api/patient/change-password', {
          userId: Number(userId),
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        })
        if (res.data.success) {
          feedback.toast(res.data.message || '密码修改成功，请重新登录')
          sessionStorage.clear()
          this.$router.push('/')
        } else {
          feedback.toast(res.data.message || '修改失败')
        }
      } catch (e) {
        feedback.toast(e.response?.data?.message || '修改失败，请稍后再试')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.change-card {
  padding: 0;
  overflow: hidden;
  margin: 0;
}

.change-hero {
  padding: 22px 18px 18px;
  color: #fff;
  background: linear-gradient(135deg, var(--medical-blue), var(--medical-blue-dark));
  display: flex;
  align-items: center;
  gap: 14px;
}

.change-icon {
  width: 58px;
  height: 58px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  font-size: 24px;
  font-weight: 800;
  flex: 0 0 auto;
}

.change-hero h2 {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 6px;
}

.change-hero p {
  font-size: 13px;
  opacity: 0.86;
}

.form-block {
  padding: 18px 14px 18px;
}

.section-title {
  margin: 2px 0 12px;
  font-size: 15px;
  color: var(--ink-main);
  font-weight: 800;
}

.password-title {
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px solid var(--line-soft);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 7px;
  font-size: 13px;
  color: var(--ink-muted);
  font-weight: 700;
  margin-bottom: 12px;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 13px;
  border: 1px solid rgba(226, 237, 249, 0.95);
  border-radius: 12px;
  background: rgba(251, 253, 255, 0.88);
  font-size: 15px;
  color: var(--ink-main);
  outline: none;
}

.textarea-input {
  min-height: 76px;
  padding-top: 12px;
  resize: none;
  line-height: 1.5;
}

.form-input:focus,
.textarea-input:focus {
  border-color: var(--medical-blue);
  box-shadow: 0 0 0 3px rgba(47, 128, 209, 0.12);
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
}

.change-wrap {
  padding-top: 16px;
}
</style>
