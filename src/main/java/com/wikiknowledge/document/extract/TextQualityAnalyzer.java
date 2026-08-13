package com.wikiknowledge.document.extract;

/**
 * 提取文本质量检测：识别常见乱码（替换符、控制字符、Latin 扩展字符占多数的 mojibake）。
 */
public final class TextQualityAnalyzer {

    private static final double MAX_REPLACEMENT_RATIO = 0.002;
    private static final double MAX_CONTROL_RATIO = 0.05;
    private static final double MAX_LATIN_EXTENDED_RATIO = 0.2;
    private static final double MIN_CJK_RATIO_FOR_LATIN_HEAVY_TEXT = 0.05;
    private static final double MIN_VALID_RATIO = 0.4;

    private TextQualityAnalyzer() {
    }

    public static boolean isLikelyGarbled(String text) {
        if (text == null || text.isBlank() || text.length() < 20) {
            return false;
        }

        int total = text.length();
        int replacement = 0;
        int control = 0;
        int latinExtended = 0;
        int cjk = 0;
        int valid = 0;

        for (int i = 0; i < total; i++) {
            char c = text.charAt(i);
            if (c == '\uFFFD') {
                replacement++;
            }
            if (Character.isISOControl(c)) {
                control++;
            }
            if (isLatinExtended(c)) {
                latinExtended++;
            }
            if (isCjk(c)) {
                cjk++;
            }
            if (isValidChar(c)) {
                valid++;
            }
        }

        double replacementRatio = (double) replacement / total;
        double controlRatio = (double) control / total;
        double latinExtendedRatio = (double) latinExtended / total;
        double cjkRatio = (double) cjk / total;
        double validRatio = (double) valid / total;

        if (replacementRatio > MAX_REPLACEMENT_RATIO) {
            return true;
        }
        if (controlRatio > MAX_CONTROL_RATIO) {
            return true;
        }
        if (latinExtendedRatio > MAX_LATIN_EXTENDED_RATIO && cjkRatio < MIN_CJK_RATIO_FOR_LATIN_HEAVY_TEXT) {
            return true;
        }
        return validRatio < MIN_VALID_RATIO;
    }

    private static boolean isLatinExtended(char c) {
        return (c >= 0x0080 && c <= 0x02FF) || (c >= 0x1E00 && c <= 0x1EFF);
    }

    private static boolean isCjk(char c) {
        return (c >= 0x3400 && c <= 0x4DBF)
                || (c >= 0x4E00 && c <= 0x9FFF)
                || (c >= 0xF900 && c <= 0xFAFF)
                || (c >= 0x3040 && c <= 0x30FF)
                || (c >= 0xAC00 && c <= 0xD7AF);
    }

    private static boolean isValidChar(char c) {
        if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
            return true;
        }
        return (c >= 0x3000 && c <= 0x303F)
                || (c >= 0x2018 && c <= 0x201F)
                || ",.;:!?-_/()[]{}'\"`~@#$%^&*+=|\\<>·—…《》，。；：、（）！？".indexOf(c) >= 0;
    }
}
