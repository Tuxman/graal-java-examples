package instrument;

import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.tools.utils.json.JSONObject;
import org.graalvm.options.OptionType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

abstract class ProfilerCLI {

    static final OptionType<Object[]> WILDCARD_FILTER_TYPE = new OptionType<>("Expression",
            new Function<String, Object[]>() {
                @Override
                public Object[] apply(String filterWildcardExpression) {
                    if (filterWildcardExpression == null) {
                        return null;
                    }
                    String[] expressions = filterWildcardExpression.split(",");
                    Object[] builtExpressions = new Object[expressions.length];
                    for (int i = 0; i < expressions.length; i++) {
                        String expression = expressions[i];
                        expression = expression.trim();
                        Object result = expression;
                        if (expression.contains("?") || expression.contains("*")) {
                            try {
                                result = Pattern.compile(wildcardToRegex(expression));
                            } catch (PatternSyntaxException e) {
                                throw new IllegalArgumentException(
                                        String.format("Invalid wildcard pattern %s.", expression), e);
                            }
                        }
                        builtExpressions[i] = result;
                    }
                    return builtExpressions;
                }
            }, new Consumer<Object[]>() {
        @Override
        public void accept(Object[] objects) {

        }
    });

    static SourceSectionFilter buildFilter(boolean roots, boolean statements, boolean calls, boolean internals,
                                           Object[] filterRootName, Object[] filterFile, String filterMimeType, String filterLanguage) {
        SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        if (!internals || filterFile != null || filterMimeType != null || filterLanguage != null) {
            builder.sourceIs(new SourceSectionFilter.SourcePredicate() {
                @Override
                public boolean test(Source source) {
                    boolean internal = (internals || !source.isInternal());
                    boolean file = testWildcardExpressions(source.getPath(), filterFile);
                    boolean mimeType = filterMimeType.equals("") || filterMimeType.equals(source.getMimeType());
                    final boolean languageId = filterLanguage.equals("") || filterMimeType.equals(source.getLanguage());
                    return internal && file && mimeType && languageId;
                }
            });
        }

        List<Class<?>> tags = new ArrayList<>();
        if (roots) {
            tags.add(StandardTags.RootTag.class);
        }
        if (statements) {
            tags.add(StandardTags.StatementTag.class);
        }
        if (calls) {
            tags.add(StandardTags.CallTag.class);
        }

        if (!roots && !statements && !calls) {
            throw new IllegalArgumentException(
                    "No elements specified. Either roots, statements or calls must remain enabled.");
        }
        builder.tagIs(tags.toArray(new Class<?>[0]));
        builder.rootNameIs(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return testWildcardExpressions(s, filterRootName);
            }
        });

        return builder.build();
    }

    static String repeat(String s, int times) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < times; i++) {
            b.append(s);
        }
        return b.toString();
    }

    // custom version of SourceSection#getShortDescription
    static String getShortDescription(SourceSection sourceSection) {
        if (sourceSection.getSource() == null) {
            // TODO the source == null branch can be removed if the deprecated
            // SourceSection#createUnavailable has be removed.
            return "<Unknown>";
        }
        StringBuilder b = new StringBuilder();
        if (sourceSection.getSource().getPath() == null) {
            b.append(sourceSection.getSource().getName());
        } else {
            Path pathAbsolute = Paths.get(sourceSection.getSource().getPath());
            Path pathBase = new File("").getAbsoluteFile().toPath();
            try {
                Path pathRelative = pathBase.relativize(pathAbsolute);
                b.append(pathRelative.toFile());
            } catch (IllegalArgumentException e) {
                b.append(sourceSection.getSource().getName());
            }
        }

        b.append("~").append(formatIndices(sourceSection, true));
        return b.toString();
    }

    static String formatIndices(SourceSection sourceSection, boolean needsColumnSpecifier) {
        StringBuilder b = new StringBuilder();
        boolean singleLine = sourceSection.getStartLine() == sourceSection.getEndLine();
        if (singleLine) {
            b.append(sourceSection.getStartLine());
        } else {
            b.append(sourceSection.getStartLine()).append("-").append(sourceSection.getEndLine());
        }
        if (needsColumnSpecifier) {
            b.append(":");
            if (sourceSection.getCharLength() <= 1) {
                b.append(sourceSection.getCharIndex());
            } else {
                b.append(sourceSection.getCharIndex()).append("-").append(sourceSection.getCharIndex() + sourceSection.getCharLength() - 1);
            }
        }
        return b.toString();
    }

    static boolean testWildcardExpressions(String value, Object[] fileFilters) {
        if (fileFilters == null || fileFilters.length == 0) {
            return true;
        }
        if (value == null) {
            return false;
        }
        for (Object filter : fileFilters) {
            if (filter instanceof Pattern) {
                if (((Pattern) filter).matcher(value).matches()) {
                    return true;
                }
            } else if (filter instanceof String) {
                if (filter.equals(value)) {
                    return true;
                }
            } else {
                throw new AssertionError();
            }
        }
        return false;
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append("\\S*");
                    break;
                case '?':
                    s.append("\\S");
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return s.toString();
    }

    static JSONObject sourceSectionToJSON(SourceSection sourceSection) {
        JSONObject sourceSectionJson = new JSONObject();
        if (sourceSection != null) {
            Source source = sourceSection.getSource();
            if (source != null) {
                if (source.getLanguage() != null) {
                    sourceSectionJson.put("language", source.getLanguage().toString());
                }
                String path = source.getPath();
                if (path != null) {
                    sourceSectionJson.put("path", path);
                }
            }
            sourceSectionJson.put("source_name", sourceSection.getSource().getName());
            sourceSectionJson.put("start_line", sourceSection.getStartLine());
            sourceSectionJson.put("end_line", sourceSection.getEndLine());
            sourceSectionJson.put("start_column", sourceSection.getStartColumn());
            sourceSectionJson.put("end_column", sourceSection.getEndColumn());
        }
        return sourceSectionJson;
    }

    static class SourceLocation {

        private final SourceSection sourceSection;
        private final String rootName;

        SourceLocation(SourceSection sourceSection, String rootName) {
            this.sourceSection = sourceSection;
            this.rootName = rootName;
        }

        SourceSection getSourceSection() {
            return sourceSection;
        }

        public String getRootName() {
            return rootName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SourceLocation that = (SourceLocation) o;

            if (sourceSection != null ? !sourceSection.equals(that.sourceSection) : that.sourceSection != null) {
                return false;
            }
            return rootName != null ? rootName.equals(that.rootName) : that.rootName == null;
        }

        @Override
        public int hashCode() {
            int result = sourceSection != null ? sourceSection.hashCode() : 0;
            result = 31 * result + (rootName != null ? rootName.hashCode() : 0);
            return result;
        }
    }
}
