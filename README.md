# ChatGPT java client 
该项目基于 https://github.com/PlexPt/chatgpt-java 感谢作者。
由于要支持 chatGPT plus 帐号，改动很大，就没有提 PR 了
免费chatGPT 用户自测是用不了，因为 chatGPT 会拦截，即使用了代理，还没找到更好的方法。
有进展会在这里更新，现阶段测试过 plus 帐号完美支持。

由于 chatGPT 的规则一直在变，就不打成 maven 包了，大家直接集成到自己项目吧，有更新，或者自己也可以修改。
# 使用方法
- 集成代码到项目
- 参考 build.gradle 添加依赖
- 参考测试用例使用
```
    public void testPaidUser() {
        String sessionToken = "";
        String cfClearance = "";
        String userAgent = ""; 
        String paidUid = ""; // 付费用户id

        String ask = "请给出5个儿童粉红贸易的卖点，每个卖点不超过10个字";
        ChatBot chatbot = new ChatBot(sessionToken, cfClearance, userAgent, paidUid);

        Map<String, Object> chatResponse = chatbot.getChatResponse(ask);
        System.out.println(chatResponse.get("message"));
    }

```
- 一个ChatBot实例为一个对话上下文，问题之间会产生影响，如果需要对话互相隔离，创建多个ChatBot实例使用
- 做成个微服务就能对外提供 api 服务

# 绘画信息获取
1. 通过 https://chat.openai.com/chat 注册并登录。
2. 打开浏览器开发者工具，切换到 Application 标签页。
3. 在左侧的 Storage - Cookies 中找到 __Secure-next-auth.session-token 一行并复制其值
4. 找到 cf_clearance 复制
5. 找到 _puid 复制
6. 在network中获取 user-agent 复制

# 注册教程网
1. 网上搜一下
2. 或者关注微信公众号: 折腾几下， 有教程和共享的免费帐号

# chatGPT 交流
1. 添加微信 mainentry 交流，可以拉进 chatGPT 的交流群

# Disclaimers
这不是官方的 OpenAI 产品。这是一个个人项目，与 OpenAI 没有任何关联。