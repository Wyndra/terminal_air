const { defineConfig } = require('@vue/cli-service')
const { execSync } = require('child_process');
module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack(config) {
    // 获取当前 Git 提交哈希值
    const gitCommitHash = execSync('git rev-parse --short HEAD').toString().trim();

    // 将其注入为环境变量
    config.plugin('define').tap(args => {
      args[0]['process.env.VUE_APP_GIT_COMMIT_HASH'] = JSON.stringify(gitCommitHash);
      return args;
    });
  }
})
