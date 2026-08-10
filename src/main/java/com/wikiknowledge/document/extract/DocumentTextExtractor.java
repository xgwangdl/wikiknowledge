package com.wikiknowledge.document.extract;

import java.io.InputStream;

public interface DocumentTextExtractor {

    String extract(InputStream inputStream, String filename) throws Exception;
}
