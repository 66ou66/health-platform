//导入全局样式
import './assets/main.scss'
//导入路由
import router from '@/router'
//导入pinia状态管理
import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
//导入ElementPlus组件库  npm install element-plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

//创建应用
const app = createApp(App)
//使用插件（ElementPlus、Pinia、路由）
app.use(ElementPlus, { locale: zhCn })
app.use(createPinia())
app.use(router)
//挂载到页面
app.mount('#app')
