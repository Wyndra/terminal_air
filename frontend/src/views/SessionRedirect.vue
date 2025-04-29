<template>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

onMounted(() => {
    const session = route.query.session;

    
    if (session) {
        const title = encodeURIComponent('有 HR 访问了你的项目');
        const body = encodeURIComponent(`Session ID: ${session}`);
        const barkUrl = `https://api.day.app/XRUPuaqkUV9iLLizgFgLpS/${title}/${body}?icon=https://s2.loli.net/2024/10/03/XFMKy2ENeaAUIwY.jpg`;

        fetch(barkUrl).catch(() => {
        });
    }

    setTimeout(() => {
        router.push('/');
    }, 1000);
});
</script>

