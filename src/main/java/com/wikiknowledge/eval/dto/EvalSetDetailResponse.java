package com.wikiknowledge.eval.dto;

import java.util.List;

public record EvalSetDetailResponse(
        /** 评估集信息 */
        EvalSetResponse set,
        /** 评估题目列表 */
        List<EvalQuestionResponse> questions) {
}
