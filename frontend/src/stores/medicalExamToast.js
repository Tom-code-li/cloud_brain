import { ElMessage } from 'element-plus';

export function showToast(message) {
  ElMessage({
    message,
    type: 'info',
    duration: 2600
  });
}
