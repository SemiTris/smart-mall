import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

/** localStorage 存储键 */
const USER_KEY = 'smart_user'

/**
 * 用户状态：登录信息存 Pinia + localStorage（刷新不掉）
 * 本项目不使用 JWT，后端也不校验身份，所以这里就是唯一的"登录态"
 */
export const useUserStore = defineStore('user', () => {
    // 初始化时从 localStorage 恢复
    const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

    /** 是否已登录 */
    const isLoggedIn = computed(() => !!user.value)

    /** 是否管理员（role === 1） */
    const isAdmin = computed(() => user.value?.role === 1)

    /**
     * 保存登录信息
     * @param {object} u 后端返回的 User
     */
    function setUser(u) {
        user.value = u
        localStorage.setItem(USER_KEY, JSON.stringify(u))
    }

    /** 退出登录 */
    function logout() {
        user.value = null
        localStorage.removeItem(USER_KEY)
    }

    return { user, isLoggedIn, isAdmin, setUser, logout }
})
