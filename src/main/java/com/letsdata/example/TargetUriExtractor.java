package com.letsdata.example;

import com.google.common.collect.ImmutableMap;
import com.resonance.letsdata.data.documents.implementation.ErrorDoc;
import com.resonance.letsdata.data.documents.interfaces.DocumentInterface;
import com.resonance.letsdata.data.documents.interfaces.ErrorDocInterface;
import com.resonance.letsdata.data.readers.interfaces.parsers.SingleFileParser;
import com.resonance.letsdata.data.readers.model.ParseDocumentResult;
import com.resonance.letsdata.data.readers.model.ParseDocumentResultStatus;
import com.resonance.letsdata.data.readers.model.RecordHintType;
import com.resonance.letsdata.data.readers.model.RecordParseHint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TargetUriExtractor implements SingleFileParser {
    private static final Logger logger = LoggerFactory.getLogger(TargetUriExtractor.class);

    @Override
    public String getS3FileType() {
        return "WARC";
    }

    @Override
    public String getResolvedS3FileName(String s3FileType, String fileName) {
        return fileName;
    }

    @Override
    public RecordParseHint getRecordStartPattern(String s3FileType) {
        return new RecordParseHint(RecordHintType.PATTERN, "WARC-Type: request", -1);
    }

    @Override
    public RecordParseHint getRecordEndPattern(String s3FileType) {
        return new RecordParseHint(RecordHintType.PATTERN, "WARC-Type: response", -1);
    }

    @Override
    public ParseDocumentResult parseDocument(String s3FileType, String s3Filename, long offsetBytes, byte[] byteArr, int startIndex, int endIndex) {
        logger.info("received record - offsetBytes {}, fileName: {}, startIndex: {}, endIndex: {} ", offsetBytes, s3Filename, startIndex, endIndex);
        String recordString = new String(byteArr,startIndex, endIndex-startIndex, StandardCharsets.UTF_8);
        String[] lines = recordString.split("\n");
        logger.info("split record lines - lines.length: {}", lines.length);
        for (String line : lines) {
            int index = line.indexOf("WARC-Target-URI: ");
            if (index == -1) {
                continue;
            }
            String[] lineParts = line.split("WARC-Target-URI: ");
            if (lineParts.length > 1) {
                String uri = lineParts[1].trim();
                if (StringUtils.isBlank(uri)) {
                    ErrorDocInterface errorDoc = new ErrorDoc(
                            ImmutableMap.of(s3FileType, Long.toString(offsetBytes)),
                            ImmutableMap.of(s3FileType, Long.toString(offsetBytes+endIndex-startIndex)),
                            "URI is null or empty",
                            null,
                            "WARC-Type: request",
                            null,
                            "URI is null or empty",
                            ""+s3Filename.hashCode());
                    logger.info("line uri is blank");
                    return new ParseDocumentResult(null,errorDoc, ParseDocumentResultStatus.ERROR);
                } else {
                    logger.info("line uri {}", uri);
                    return new ParseDocumentResult(null, new DocumentInterface() {
                        @Override
                        public String getDocumentId() {
                            return uri;
                        }

                        @Override
                        public String getRecordType() {
                            return "WARC-Type: request";
                        }

                        @Override
                        public Map<String, Object> getDocumentMetadata() {
                            return null;
                        }

                        @Override
                        public String serialize() {
                            return uri;
                        }

                        @Override
                        public String getPartitionKey() {
                            return uri;
                        }
                    }, ParseDocumentResultStatus.SUCCESS);
                }
            }
        }
        ErrorDocInterface errorDoc = new ErrorDoc(
                ImmutableMap.of(s3FileType, Long.toString(offsetBytes)),
                ImmutableMap.of(s3FileType, Long.toString(offsetBytes+endIndex-startIndex)),
                "'WARC-Target-URI: ' key not found",
                null,
                "WARC-Type: request",
                null,
                "'WARC-Target-URI: ' key not found",
                ""+s3Filename.hashCode());
        logger.info("line uri not found");
        return new ParseDocumentResult(null,errorDoc, ParseDocumentResultStatus.ERROR);
    }
}
