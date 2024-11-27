<template>
    <div id="terminal" class="xterm"></div>
</template>

<script setup>
import "xterm/css/xterm.css";
import { onMounted, ref, watch } from "vue";
import { Terminal } from "xterm";
import { FitAddon } from "xterm-addon-fit";
import { useStore } from "vuex";
import serverConfig from "@/utils/config";

const token = localStorage.getItem("token") || "";

// 开发环境
// const socketURI = "ws://localhost:8089/webssh" + "?token=" + token;
// 生产环境
const socketURI = serverConfig.wsURL + "?token=" + token;

const store = useStore();

const connectionInfo = ref({
    connectHost: store.state.host,
    connectPort: store.state.port,
    connectUsername: store.state.username,
    connectPassword: store.state.password,
});

// watch(store.state, (newVal) => {
//     connectionInfo.value.connectHost = newVal.host;
//     connectionInfo.value.connectPort = newVal.port;
//     connectionInfo.value.connectUsername = newVal.username;
//     connectionInfo.value.connectPassword = newVal.password;
//     // sendToggleConnect();
// }, { deep: true });

watch(
    () => ({
        host: store.state.host,
        port: store.state.port,
        username: store.state.username,
        password: store.state.password
    }),
    (newVal) => {
        connectionInfo.value.connectHost = newVal.host;
        connectionInfo.value.connectPort = newVal.port;
        connectionInfo.value.connectUsername = newVal.username;
        connectionInfo.value.connectPassword = newVal.password;
        // sendToggleConnect();
    },
    { deep: true }
);

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
    // fontFamily: "Meslo LG M for Powerline",
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
        sendToggleConnect();
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
</script>

<style>
#terminal {
    width: 100%;
    height: 100%;
}
</style>