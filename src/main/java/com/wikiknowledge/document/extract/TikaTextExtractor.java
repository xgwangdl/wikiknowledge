package com.wikiknowledge.document.extract;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.InputStream;/** 基于 Apache Tika 的文本提取实现 */


@Service
public class TikaTextExtractor implements DocumentTextExtractor {

    @Override
    public String extract(InputStream inputStream, String filename) throws Exception {
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
        new AutoDetectParser().parse(inputStream, handler, metadata, new ParseContext());
        return handler.toString();
    }
}
