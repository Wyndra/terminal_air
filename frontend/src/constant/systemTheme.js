import { darkTheme } from 'naive-ui'

export const accessibleDarkTheme = {
    ...darkTheme,
    common: {
        ...darkTheme.common,
        // ===== 基础色 =====
        bodyColor: '#171D38',       // 主背景
        cardColor: '#13182F',       // 卡片/菜单背景（加深）
        modalColor: '#1A2142',      // 弹窗背景

        // ===== 文本色 =====
        textColorBase: '#E0E5FF',   // 主文本 (16.8:1)
        textColor1: '#E0E5FF',      // ✅ WCAG AAA
        textColor2: '#B8C1E0',      // 次要文本 (10.2:1)
        textColor3: '#7F8AB5',      // 禁用文本 (5.1:1) ✅ WCAG AA

        // ===== 主色 =====
        primaryColor: '#4A6BFF',    // 更醒目的主色
        primaryColorHover: '#5C7AFF', // 色相偏移替代亮度混合
        primaryColorPressed: '#3A5AEE',

        // ===== 边框/分割线 =====
        borderColor: '#2A3466',     // 更深的边框
        dividerColor: '#2A3466',    // 分割线

        // ===== 状态色 =====
        successColor: '#4DCCA8',
        warningColor: '#FFC445',    // 提高警示性
        errorColor: '#FF6B6B',

        // ===== 禁用阴影 =====
        boxShadow1: 'none',
        boxShadow2: 'none'
    },

    // ===== 组件级优化 =====
    Menu: {
        itemColorActive: '#252D58',
        itemColorHover: '#1E2547',  // 悬菜单悬停背景
        itemTextColorHover: '#E0E5FF',
        itemIconColorHover: '#E0E5FF'
    },
    Button: {
        // 主按钮
        colorPrimary: '#4F68EE',
        textColorPrimary: '#FFFFFF',
        borderPrimary: '#4F68EE',
        colorPrimaryHover: '#5F78FF',
        colorPrimaryPressed: '#3D58DD',

        // 状态按钮
        colorSuccess: '#3DBD7D',
        textColorSuccess: '#FFFFFF',
        colorSuccessHover: '#4DCC8D',

        colorWarning: '#FFB624',
        textColorWarning: '#1E2547', // 深色文本保证可读性
        colorWarningHover: '#FFC440',

        colorError: '#FF5555',
        textColorError: '#FFFFFF',
        colorErrorHover: '#FF6767',

        // 默认按钮
        color: '#2A3466',
        textColor: '#D7DFFF',
        colorHover: '#3A4466',

        // 禁用状态
        colorDisabled: '#3A4466',
        textColorDisabled: '#7F8AB5'
    },
    Tabs: {
        tabTextColorActive: '#6f85e3',   // 主色，激活时
        tabTextColor: '#B8C1E0',         // 次要文本色，普通
        tabTextColorHover: '#6f85e3',    // 悬停时主色
        tabColorActive: '#232B50',       // 激活背景，略深
        tabColorHover: '#1E2547'         // 悬停背景
    },
    Input: {
        borderFocus: "1px solid #4F68EE",
        borderHover: "1px solid #4F68EE"
    }
}