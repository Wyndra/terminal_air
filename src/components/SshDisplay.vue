<template>
    <div id="terminal-container">
        <div @click="refresh_fitAddon" id="terminal" class="xterm"></div>
    </div>
</template>

<script setup>
import "xterm/css/xterm.css";
import { onMounted, ref, watch, nextTick, defineEmits, isReactive } from "vue";
import { Terminal } from "xterm";
import { FitAddon } from "xterm-addon-fit";
import { WebglAddon } from "xterm-addon-webgl";
import { useStore } from "vuex";
import serverConfig from "@/utils/config";
import { useMessage } from "naive-ui";

const token = localStorage.getItem("token") || "";

const store = useStore();
const message = useMessage();

const socketURI = serverConfig.wsURL + "?token=" + token;

const connectionInfo = ref({
    connectHost: store.state.host,
    connectPort: store.state.port,
    connectUsername: store.state.username,
    connectPassword: store.state.password,
});

const isFirstConnection = ref(true);  // 标志是否是第一次连接

const fitAddon = new FitAddon();
const hasShownError = ref(store.getters.hasShownError);

const refresh_fitAddon = () => {
    fitAddon.fit();
}

const initTerminal = () => {
    requestAnimationFrame(() => {
        const terminalElement = document.getElementById("terminal");
        if (!terminalElement) return;

        const webglAddon = new WebglAddon();
        terminal.loadAddon(fitAddon);
        terminal.loadAddon(webglAddon);

        terminal.open(terminalElement);
        fitAddon.fit();

        terminal.onData((data) => {
            sendCommand(data);
            fitAddon.fit();
        });

        window.addEventListener('resize', () => {
            fitAddon.fit();
        });
    });
};

const terminal = new Terminal({
    rendererType: "dom",
    convertEol: true,
    disableStdin: false,
    cursorBlink: true,
    encoding: "utf-8",
    ...store.state.terminalSettings
});

let socket;

const initSocket = () => {
    socket = new WebSocket(socketURI);

    socket.onopen = () => {
        if (!isFirstConnection.value) {
            // 不是第一次连接时，发送连接命令
            sendToggleConnect();
        } else {
            isFirstConnection.value = false;  // 第一次连接后，将标志设为 false
        }
    };

    socket.onmessage = (event) => {
        let res = JSON.parse(event.data);
        if (res.type === "ping") {
            // pong back
            socket.send(JSON.stringify({ type: "pong", payload: "pong", timestamp: new Date().getTime() }));
            return;
        } else {
            if (res.type === "error") {
                // error message
                message.error(res.msg);
                terminal.write(res.data.payload);
                return;
            }
            terminal.write(res.data.payload);
            terminal.scrollToBottom();
        }
        // console.log(event.data);
    };

    socket.onerror = (error) => {
        if (error.type === "error") {
            if (!hasShownError.value) {
                message.error("WebSocket connection error");
                hasShownError.value = true;
            }
        } else {
            message.error("WebSocket connection error");
        }
        console.error("WebSocket error: " + error);
    };

    socket.onclose = () => {
        if (!hasShownError.value) {
            message.warning("连接已断开，正在尝试重新连接...");
            hasShownError.value = true;
        }
        setTimeout(() => {
            reconnect();
        }, 1000);
    };
};

const sendCommand = (command) => {
    const datas = {
        operate: "command",
        command: command,
    };
    socket.send(JSON.stringify(datas));
};

const sendToggleConnect = () => {
    terminal.clear();
    setTimeout(() => {
        const datas = {
            operate: "connect",
            host: connectionInfo.value.connectHost,
            port: Number(connectionInfo.value.connectPort),
            username: connectionInfo.value.connectUsername,
            password: connectionInfo.value.connectPassword,
            command: "",
        };
        if (isFirstConnection.value){
            terminal.write(`\r\n`)
        }
        terminal.write(`Connecting to ${datas.host} on port ${datas.port}...\n\r`);
        socket.send(JSON.stringify(datas));
    }, 100);
};

const reconnect = () => {
    initSocket();
};

onMounted(() => {
    if (!token) {
        console.error("Token is empty");
        return;
    }
    initSocket();
    initTerminal();
});

// 监听 store.state 的变化来更新连接信息
watch(
    () => ({
        host: store.state.host,
        port: store.state.port,
        username: store.state.username,
        password: store.state.password
    }),
    async (newVal) => {
        // 确保 Vuex 状态已经更新
        await nextTick();
        connectionInfo.value.connectHost = newVal.host;
        connectionInfo.value.connectPort = newVal.port;
        connectionInfo.value.connectUsername = newVal.username;
        connectionInfo.value.connectPassword = newVal.password;
        // 调用切换连接的函数
        sendToggleConnect();
    },
    { deep: true }
);

// 终端设置变化时更新终端设置
watch(() => store.state.terminalSettings, (newSettings) => {
    Object.keys(newSettings).forEach(key => {
        terminal.options[key] = newSettings[key];
    });
    // 重新适应大小
    const fitAddon = new FitAddon();
    terminal.loadAddon(fitAddon);
    fitAddon.fit();
}, { deep: true });
</script>

<style>
#terminal-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    margin: 0;
    /* 确保没有外边距 */
    padding: 0;
    /* 确保没有内边距 */
}

#terminal {
    flex: 1;
    width: 100%;
    height: 100%;
    margin: 0;
    /* 确保没有外边距 */
    padding: 0;
    /* 确保没有内边距 */
}

/* 修复终端底部空白的样式 */
.xterm {
    height: inherit;
}
</style>
