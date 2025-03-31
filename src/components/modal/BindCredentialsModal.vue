<template>
    <n-modal v-model:show="props.bindCredentialsModalVisible" class="custom-card" preset="card" style="width:370px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div class="card_main" v-if="currentStep == 0">
            <n-icon :size="48">
                <IosLink />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">绑定凭证</n-text>
            <span style="width:90%" class="description-text">
                将凭证绑定到已被 Terminal Air 管理的服务器，以安全访问。请确保凭证的安全性，避免泄露给他人。
                <br>
            </span>
            <span style="width:90%" class="description-text">
                <span class="warning">服务器端执行以下命令，需要 bash 和 curl 命令支持</span>
            </span>
            <n-code :code="curlCode" language="bash" word-wrap></n-code>
            <div style="width: 100%;margin-top: 20px">
                <div style="width: 100%;display: flex;gap:8px">
                    <n-button type="primary" style="flex: 1" :loading="onloading" @click="handleCompletedSuccess">
                        {{ buttonText }}
                    </n-button>
                    <n-button secondary style="flex:1" @click="handleCopy">
                        复制到剪贴板
                    </n-button>
                </div>

                <n-button tertiary style="width: 100%;margin-top: 10px;" color="#7f7e7a" @click="handleIssue">
                    无法执行命令？
                </n-button>
            </div>
        </div>
        <div class="card_main" v-if="currentStep == 1">
            <n-icon :size="48">
                <IosLink />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">绑定凭证</n-text>
            <span style="width:90%" class="description-text">
                请选择执行命令的服务器进行绑定。
            </span>
            <n-form ref="bindCredentialFormRef" :model="bindCredentialForm" label-placement="top"
                :rules="bindCredentialFormRules" style="width: 100% !important;margin-top: 20px">
                <n-form-item label="选择服务器" path="cid">
                    <n-select v-model:value="bindCredentialForm.cid" placeholder="请选择服务器"
                        :options="connectionSelectOptions" />
                </n-form-item>
                <n-form-item>
                    <n-button type="primary" style="width: 100%;" @click="handleBindCredential">
                        {{ buttonText }}
                    </n-button>
                </n-form-item>
            </n-form>
        </div>
    </n-modal>
</template>

<script setup>
import { computed,ref,defineEmits,defineProps } from "vue";
import { IosLink } from "@vicons/ionicons4";
import serverConfig from "@/utils/config";
import { list } from "@/api/connection"
import { getCredentialsStatus,bindCredentials } from "@/api/credentials";
import { useMessage } from "naive-ui";
import { debounce } from "lodash";
import store from "@/store";

const message = useMessage();
const hasShownError = ref(store.getters.hasShownError);


const bindCredentialFormRef = ref(null);
const bindCredentialForm = ref({
    cid: '',
});



const bindCredentialFormRules = {
    cid: [
        {
            validator: (rule, value) => {
                if (!value) {
                    return new Error('请选择服务器');
                }
                const selectedOption = connectionSelectOptions.value.find(option => option.value === value);
                if (!selectedOption) {
                    return new Error('无效的服务器选择');
                }
                return true;
            },
            trigger: 'blur'
        }
    ]
};

const emit = defineEmits(["close", "refresh"]);
const currentStep = ref(0);
const onloading = ref(false)
const buttonText = ref("我已执行命令")

const connectionList = ref([])
const connectionSelectOptions = ref([])

async function fetchConnectionList() {
    try {
        const res = await list();
        if (res.status === '200') {
            connectionList.value = [...res.data];
            connectionSelectOptions.value = connectionList.value.map((item) => {
                return {
                    label: item.connectName,
                    value: item.cid
                }
            })
        } else {
            if (!hasShownError.value) {
                message.error(res.message || '获取连接列表失败');
                store.commit('setHasShownError', true);
                hasShownError.value = true;
            }
        }
    } catch (error) {
        if (!hasShownError.value) {
            message.error('请求连接列表时出错');
            store.commit('setHasShownError', true);
            hasShownError.value = true;
        }
    }
}

const handleBindCredential = debounce(async () => {
    bindCredentialFormRef.value.validate(async (valid) => {
        if (!valid) {
            onloading.value = true
            buttonText.value = "正在绑定凭证..."
            await bindCredentials({
                connectId: Number(bindCredentialForm.value.cid),
                uuid: props.row.uuid
            }).then(async (res) => {
                if (res.status === '200') {
                    setTimeout(()=>{
                        onloading.value = false
                        message.success('凭证绑定成功');
                        currentStep.value = 0;
                        buttonText.value = "我已执行命令"
                        emit('close');
                        emit('refresh');
                    }, Math.floor(Math.random() * (1500 - 500 + 1)) + 500)
                } else {
                    message.error(res.message || '绑定凭证失败');
                }
            }).catch(() => {
                message.error('请求绑定凭证时出错');
            });
        }
    });
}, 1000, { leading: true, trailing: false });


const handleCopy = () => {
    navigator.clipboard.writeText(curlCode.value).then(() => {
        message.success("复制成功");
    }).catch(() => {
        message.error("复制失败");
    });
};

const handleIssue = () => {
    message.info("请 联系我们 或 查看文档 以获取帮助");
};

const handleCompletedSuccess = debounce(async () => {
    onloading.value = true
    buttonText.value = "正在检查..."
    await getCredentialsStatus(props.row.uuid).then(async (res) => {
        if (res.data === 1){
            // 凭证服务端绑定成功的情况
            setTimeout(()=>{
                onloading.value = false
                currentStep.value++;
                message.success("凭证服务端绑定成功");
                buttonText.value = "绑定凭证"
            }, Math.floor(Math.random() * (2000 - 1000 + 1)) + 1000)
            await fetchConnectionList()
        } else if(res.data === 2){
            // 凭证已被绑定的情况
            setTimeout(()=>{
                onloading.value = false
                buttonText.value = "我已执行命令"
                message.error("凭证已被绑定，无法重复绑定");
                currentStep.value = 0;
                emit('close');
            }, Math.floor(Math.random() * (2000 - 1000 + 1)) + 1000)
        }else {
            setTimeout(()=>{
                onloading.value = false
                buttonText.value = "我已执行命令"
                message.error("凭证服务端未绑定，请确认命令是否执行成功");
                buttonText.value = "我已执行命令"
                currentStep.value = 0;
            }, Math.floor(Math.random() * (2000 - 1000 + 1)) + 1000)
        }
    });
}, 1000, { leading: true, trailing: false });


const props = defineProps({
    bindCredentialsModalVisible: Boolean,
    row: Object
});

const token = localStorage.getItem("token") || "";

const curlCode = computed(() => {
    if (!token || !props.row?.id) {
        return "# Error: Missing token or credential ID";
    }
    const url = `${serverConfig.baseURL}/api/credentials/installation/${props.row.uuid}?endpoint=${serverConfig.baseURL}&token=${encodeURIComponent(token)}`;
    return `curl -sSL "${url}" | bash`;
});
</script>

<style scoped>
.card_main {
    display: flex;
    gap: 8px;
    flex-direction: column;
    align-items: center;
}

.description-text {
    color: #A1A1A1;
    font-size: 15px;
    text-align: center;
    line-height: 1.5;
    font-family: ui-sans-serif,
        -apple-system,
        system-ui;

    .important {
        color: #A1A1A1;
        font-size: 15px;
        text-align: center;
        font-weight: bold;
    }

    .warning {
        color: #e4a441;
        font-size: 15px;
        text-align: center;
        font-weight: bold;
    }
}



/* 强制修复主题未应用的问题 */
.hljs {
    background: #0d1117;
    /* github-dark 背景色 */
    color: #c9d1d9;
    /* github-dark 前景色 */
    padding: 1em;
}
</style>