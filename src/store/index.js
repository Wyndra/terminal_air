import { createStore } from 'vuex'

export default createStore({
  state: {
    isLoggedIn: localStorage.getItem('token') ? true : false, // 从 localStorage 获取登录状态
    host: '',
    port: '',
    username: '',
    password: '',
    showAddNewConnectionDrawer: false,
    showEditConnectionDrawer: false,
    hasShownError: false,
    editConnectionInfo: {}
  },
  getters: {
    isModalVisible: (state) => state.showModal,
    modalType: (state) => state.currentOpenModalType,
    hasShownError: (state) => state.hasShownError,
    isLoggedIn: (state) => state.isLoggedIn, // 获取登录状态
  },
  mutations: {
    setLoginState(state, isLoggedIn) {
      state.isLoggedIn = isLoggedIn
      if (isLoggedIn) {
        localStorage.setItem('token', 'your-token') // 登录时存储 token
      } else {
        localStorage.removeItem('token') // 登出时移除 token
      }
    },
    setHasShownError(state, hasShownError) {
      state.hasShownError = hasShownError
    },
    resetHasShownError(state) {
      state.hasShownError = false
    },
    setHost(state, host) {
      state.host = host
    },
    setPort(state, port) {
      state.port = port
    },
    setUsername(state, username) {
      state.username = username
    },
    setPassword(state, password) {
      state.password = password
    },
    setShowAddNewConnectionDrawer(state, value) {
      state.showAddNewConnectionDrawer = value
    },
    setShowEditConnectionDrawer(state, value) {
      state.showEditConnectionDrawer = value
    },
    setEditConnectionInfo(state, value) {
      state.editConnectionInfo = value
    } 
  },
  actions: {
    login({ commit }) {
      // 模拟登录逻辑
      commit('setLoginState', true)
    },
    logout({ commit }) {
      // 模拟登出逻辑
      commit('setLoginState', false)
    },
    openModal({ commit }, type) {
      commit('setModalType', type);
      commit('setShowModal', true);
    },
    closeModal({ commit }) {
      commit('setShowModal', false);
    },
    resetHasShownError({ commit }) {
      commit('resetHasShownError');
    }
  },
  modules: {},
})
