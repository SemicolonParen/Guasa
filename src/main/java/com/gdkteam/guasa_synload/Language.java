package com.gdkteam.guasa_synload;

import java.util.Set;

public class Language {
    private final String name;
    private final Set<String> keywords;
    private final String singleLineComment;
    private final String multiLineCommentStart;
    private final String multiLineCommentEnd;

    public Language(String name, Set<String> keywords,
                   String singleLineComment,
                   String multiLineCommentStart,
                   String multiLineCommentEnd) {
        this.name = name;
        this.keywords = keywords;
        this.singleLineComment = singleLineComment;
        this.multiLineCommentStart = multiLineCommentStart;
        this.multiLineCommentEnd = multiLineCommentEnd;
    }

    public String getName() {
        return name;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public String getSingleLineComment() {
        return singleLineComment;
    }

    public String getMultiLineCommentStart() {
        return multiLineCommentStart;
    }

    public String getMultiLineCommentEnd() {
        return multiLineCommentEnd;
    }
}
