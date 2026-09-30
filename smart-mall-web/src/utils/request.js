// 引入axios
import axios from 'axios'
// 创建axios实例
const http = axios.create({
    baseURL: 'http://localhost:8080/',
    timeout: 60000,
    headers: {
        'Content-Type': 'application/json; charset=utf-8'
    }
})
// 请求拦截器
http.interceptors.request.use(
    config => {
        return config
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)
// 响应拦截器
http.interceptors.response.use(
    // 处理后，把返回结果axios自动封装的promise中的data提取出来
    response => {
        return response.data
    },
    error => {
        return Promise.reject(error)
    }
)
// 导出axios实例
export default http
