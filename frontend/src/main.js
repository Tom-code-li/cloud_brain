import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router/index.js';
import './styles/global.css';

createApp(App).use(router).use(ElementPlus, { size: 'default', locale: zhCn }).mount('#app');
