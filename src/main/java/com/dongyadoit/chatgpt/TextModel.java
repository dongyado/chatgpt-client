package com.dongyadoit.chatgpt;
/**
 * 当前出现了三种 model
 * 免费版本用户使用：
 *    text-davinci-002-render
 * plus 用户使用以下两个 model:
 *   付费默认版本: text-davinci-002-render-paid
 *   turbo 版本(速度快)： text-davinci-002-render-sha
 *
 * @author  dongyado@gmail.com
 * */
public enum TextModel {
    TEXT_FREE_MODEL("text-davinci-002-render"),
    TEXT_PAID_MODEL("text-davinci-002-render-paid"),
    TEXT_TURBO_MODEL("text-davinci-002-render-sha");

    private String value = "";

    TextModel(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
