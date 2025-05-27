package com.dali.fuaiagent.agent;// 可以放在 ReActAgent.java 文件内部，或者一个单独的文件
// package com.dali.fuaiagent.agent; // 假设的包名

class ThinkResult {
    boolean shouldAct; // 是否需要调用工具 (act())
    String assistantThoughtOrResponse; // 当 shouldAct 为 false 时，这是AI的文本回复；
                                    // 当 shouldAct 为 true 时，这可能是AI决定使用工具前的思考文本。

    public ThinkResult(boolean shouldAct, String assistantThoughtOrResponse) {
        this.shouldAct = shouldAct;
        this.assistantThoughtOrResponse = assistantThoughtOrResponse;
    }

    public boolean booleanisShouldAct() {
        return shouldAct;
    }

    public String getAssistantThoughtOrResponse() {
        return assistantThoughtOrResponse;
    }
}