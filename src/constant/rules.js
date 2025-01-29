const emailPattern = /^(([^<>()\[\]\\.,;:\s@"]+(.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

const userInfoRules = {
    username: [
        { required: true, message: '用户名不能为空', trigger: ['blur'] }
    ],
    nickname: [
        { required: true, message: '昵称不能为空', trigger: ['blur'] },
        { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: ['blur'] }
    ],
    phone: [
        { required: true, message: '手机号码不能为空', trigger: ['blur'] },
        {
            pattern: /^1[3-9][0-9]{9}$/, message: '请输入有效的手机号码', trigger: ['blur']
        }
    ],
    email: [
        { required: true, message: '邮箱不能为空', trigger: ['blur'] },
        { pattern: emailPattern, message: '请输入有效的邮箱地址', trigger: ['blur'] }
    ],
    salt: [
        { required: false, trigger: ['blur'] }
    ],
    isTwoFactorAuth: [
        { required: false, trigger: ['blur'] }
    ],
    twoFactorAuthSecret: [
        { required: false, trigger: ['blur'] }
    ]
};

export { emailPattern, userInfoRules };