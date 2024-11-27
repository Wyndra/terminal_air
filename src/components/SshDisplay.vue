<template>
    <div id="terminal" class="xterm"></div>
</template>

<script setup>
import "xterm/css/xterm.css";
import { onMounted, ref, watch, nextTick } from "vue";
import { Terminal } from "xterm";
import { FitAddon } from "xterm-addon-fit";
import { useStore } from "vuex";
import serverConfig from "@/utils/config";

const token = localStorage.getItem("token") || "";

// 生产环境
const socketURI = serverConfig.wsURL + "?token=" + token;

const store = useStore();

const connectionInfo = ref({
    connectHost: store.state.host,
    connectPort: store.state.port,
    connectUsername: store.state.username,
    connectPassword: store.state.password,
});

const isFirstConnection = ref(true);  // 标志是否是第一次连接

const initTerminal = () => {
    const terminalElement = document.getElementById("terminal");
    const fitAddon = new FitAddon();
    terminal.loadAddon(fitAddon);

    terminal.open(terminalElement);
    fitAddon.fit();

    terminal.onData((data) => {
        sendCommand(data);
    });

    window.addEventListener('resize', () => {
        fitAddon.fit();
    });
};

const terminal = new Terminal({
    rendererType: "dom",
    convertEol: true,
    disableStdin: false,
    cursorBlink: true,
    encoding: "utf-8",
    fontFamily: "Menlo, Monaco, 'Courier New', monospace",
    theme: {
        foreground: "#ECECEC",
        background: "#000000",
        cursor: "help",
        blue: "#c4a9f4",
    },
});

let socket;

const initSocket = () => {
    socket = new WebSocket(socketURI);

    socket.onopen = () => {
        console.log("WebSocket connection established");

        if (!isFirstConnection.value) {
            sendToggleConnect(); // 只有在不是第一次连接时才发送连接命令
        } else {
            isFirstConnection.value = false;  // 第一次连接后，将标志设为 false
        }
    };

    socket.onmessage = (event) => {
        terminal.write(event.data);
        terminal.scrollToBottom();
        console.log(event.data);
    };

    socket.onclose = () => {
        console.log("WebSocket connection closed");
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
</script>

<style>
#terminal {
    width: 100%;
    height: 100%;
}
</style>
