<template>
    <div id="terminal-container">
        <div @click="refresh_fitAddon" id="terminal" class="xterm"></div>
    </div>
</template>

<script setup>
import "xterm/css/xterm.css";
import { onMounted, ref, watch, nextTick,defineEmits } from "vue";
import { Terminal } from "xterm";
import { FitAddon } from "xterm-addon-fit";
import { WebglAddon } from "xterm-addon-webgl";
import { useStore } from "vuex";
import serverConfig from "@/utils/config";
import { useMessage } from "naive-ui";

const token = localStorage.getItem("token") || "";

const store = useStore();
const { createMessage } = useMessage();

const socketURI = serverConfig.wsURL + "?token=" + token;

const connectionInfo = ref({
    connectHost: store.state.host,
    connectPort: store.state.port,
    connectUsername: store.state.username,
    connectPassword: store.state.password,
});

const isFirstConnection = ref(true);  // 标志是否是第一次连接
const fitAddon = new FitAddon();

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
        // console.log("WebSocket connection established");
        if (!isFirstConnection.value) {
            sendToggleConnect(); // 只有在不是第一次连接时才发送连接命令
        } else {
            isFirstConnection.value = false;  // 第一次连接后，将标志设为 false
        }
    };

    socket.onmessage = (event) => {
        let res = JSON.parse(event.data);
        terminal.write(res.data.payload);
        terminal.scrollToBottom();
        // console.log(event.data);
    };

    socket.onerror = (error) => {
        if (error.type === "error") {
            createMessage.error("WebSocket connection error");
        }
        console.error("WebSocket error: " + error);
    };

    socket.onclose = () => {
        // console.log("WebSocket connection closed");
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
        socket.send(JSON.stringify(datas));
        terminal.write(`Connecting to ${datas.host} on port ${datas.port}...\n\r`);
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

// 监听终端设置变化
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
    margin: 0; /* 确保没有外边距 */
    padding: 0; /* 确保没有内边距 */
}

#terminal {
    flex: 1;
    width: 100%;
    height: 100%;
    margin: 0; /* 确保没有外边距 */
    padding: 0; /* 确保没有内边距 */
}
</style>
