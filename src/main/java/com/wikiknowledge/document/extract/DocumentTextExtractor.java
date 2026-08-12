package com.wikiknowledge.document.extract;

import java.io.InputStream;

/** 文档文本提取接口 */
public interface DocumentTextExtractor {

    String extract(InputStream inputStream, String filename) throws Exception;
}
