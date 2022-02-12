package com.iammonk.htmlspanner.css;

import android.graphics.Color;
import android.util.Log;

import com.iammonk.htmlspanner.FontFamily;
import com.iammonk.htmlspanner.HtmlSpanner;
import com.iammonk.htmlspanner.style.Style;
import com.iammonk.htmlspanner.style.StyleValue;

import org.htmlcleaner.TagNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiler for CSS Rules.
 * <p>
 * The compiler takes the raw parsed form (a Rule) of a CSS rule
 * and transforms it into an executable CompiledRule where all
 * the parsing of values has already been done.
 */
public class CSSCompiler {

    public interface StyleUpdater {
        Style updateStyle(Style style, HtmlSpanner spanner);
    }

    public interface TagNodeMatcher {
        boolean matches(TagNode tagNode);
    }

    public static CompiledRule compile(Rule rule, HtmlSpanner spanner) {

        List<List<TagNodeMatcher>> matchers = new ArrayList<>();
        List<StyleUpdater> styleUpdaters = new ArrayList<>();

        for (Selector selector : rule.getSelectors()) {
            List<CSSCompiler.TagNodeMatcher> selMatchers = CSSCompiler.createMatchersFromSelector(selector);
            matchers.add(selMatchers);
        }

        Style blank = new Style();

        for (PropertyValue propertyValue : rule.getPropertyValues()) {
            CSSCompiler.StyleUpdater updater = CSSCompiler.getStyleUpdater(propertyValue.getProperty(),
                    propertyValue.getValue());

            if (updater != null) {
                styleUpdaters.add(updater);
                blank = updater.updateStyle(blank, spanner);
            }
        }

        String asText = rule.toString();

        return new CompiledRule(spanner, matchers, styleUpdaters, asText);
    }

    public static Integer parseCSSColor(String colorString) {

        //Check for CSS short-hand notation: #0fc -> #00ffcc
        if (colorString.length() == 4 && colorString.startsWith("#")) {
            StringBuilder builder = new StringBuilder("#");
            for (int i = 1; i < colorString.length(); i++) {
                //Duplicate each char
                builder.append(colorString.charAt(i));
                builder.append(colorString.charAt(i));
            }

            colorString = builder.toString();
        }

        return Color.parseColor(colorString);
    }

    public static List<TagNodeMatcher> createMatchersFromSelector(Selector selector) {
        List<TagNodeMatcher> matchers = new ArrayList<>();

        String selectorString = selector.toString();

        String[] parts = selectorString.split("\\s");

        //Create a reversed matcher list
        for (int i = parts.length - 1; i >= 0; i--) {
            matchers.add(createMatcherFromPart(parts[i]));
        }

        return matchers;
    }

    private static TagNodeMatcher createMatcherFromPart(String selectorPart) {

        //Match by class
        if (selectorPart.indexOf('.') != -1) {
            return new ClassMatcher(selectorPart);
        }

        if (selectorPart.startsWith("#")) {
            return new IdMatcher(selectorPart);
        }

        return new TagNameMatcher(selectorPart);
    }


    private static class ClassMatcher implements TagNodeMatcher {

        private String tagName;
        private String className;

        private ClassMatcher(String selectorString) {

            String[] elements = selectorString.split("\\.");

            if (elements.length == 2) {
                tagName = elements[0];
                className = elements[1];
            }
        }

        @Override
        public boolean matches(TagNode tagNode) {

            if (tagNode == null) {
                return false;
            }

            //If a tag name is given it should match
            if (tagName != null && tagName.length() > 0 && !tagName.equals(tagNode.getName())) {
                return false;
            }

            String classAttribute = tagNode.getAttributeByName("class");
            return classAttribute != null && classAttribute.equals(className);
        }
    }

    private static class TagNameMatcher implements TagNodeMatcher {
        private final String tagName;

        private TagNameMatcher(String selectorString) {
            this.tagName = selectorString.trim();
        }

        @Override
        public boolean matches(TagNode tagNode) {
            return tagNode != null && tagName.equalsIgnoreCase(tagNode.getName());
        }
    }

    private static class IdMatcher implements TagNodeMatcher {
        private final String id;

        private IdMatcher(String selectorString) {
            id = selectorString.substring(1);
        }

        @Override
        public boolean matches(TagNode tagNode) {

            if (tagNode == null) {
                return false;
            }

            String idAttribute = tagNode.getAttributeByName("id");
            return idAttribute != null && idAttribute.equals(id);
        }
    }

    public static StyleUpdater getStyleUpdater(final String key, final String value) {

        if ("color".equals(key)) {
            try {
                final Integer color = parseCSSColor(value);
                return (style, spanner) -> style.setColor(color);
            } catch (IllegalArgumentException ia) {
                return null;
            }
        }

        if ("background-color".equals(key)) {
            try {
                final Integer color = parseCSSColor(value);
                return (style, spanner) -> style.setBackgroundColor(color);
            } catch (IllegalArgumentException ia) {
                return null;
            }
        }

        if ("align".equals(key) || "text-align".equals(key)) {
            try {
                final Style.TextAlignment alignment = Style.TextAlignment.valueOf(value.toUpperCase());
                return (style, spanner) -> style.setTextAlignment(alignment);

            } catch (IllegalArgumentException i) {
                return null;
            }
        }

        if ("font-weight".equals(key)) {

            try {
                final Style.FontWeight weight = Style.FontWeight.valueOf(value.toUpperCase());
                return (style, spanner) -> style.setFontWeight(weight);

            } catch (IllegalArgumentException i) {
                return null;
            }
        }

        if ("font-style".equals(key)) {
            try {
                final Style.FontStyle fontStyle = Style.FontStyle.valueOf(value.toUpperCase());
                return (style, spanner) -> style.setFontStyle(fontStyle);
            } catch (IllegalArgumentException i) {
                return null;
            }
        }

        if ("font-family".equals(key)) {
            return (style, spanner) -> {
                FontFamily family = spanner.getFont(value);
                return style.setFontFamily(family);
            };

        }

        if ("font-size".equals(key)) {

            final StyleValue styleValue = StyleValue.parse(value);

            if (styleValue != null) {

                return (style, spanner) -> style.setFontSize(styleValue);

            } else {

                //Fonts have an extra legacy format where you just specify a plain number.
                try {
                    final float number = translateFontSize(Integer.parseInt(value));
                    return (style, spanner) -> style.setFontSize(new StyleValue(number, StyleValue.Unit.EM));
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
        }

        if ("margin-bottom".equals(key)) {
            final StyleValue styleValue = StyleValue.parse(value);
            if (styleValue != null) {
                return (style, spanner) -> style.setMarginBottom(styleValue);
            }
        }

        if ("margin-top".equals(key)) {

            final StyleValue styleValue = StyleValue.parse(value);

            if (styleValue != null) {
                return (style, spanner) -> style.setMarginTop(styleValue);
            }
        }

        if ("margin-left".equals(key)) {

            final StyleValue styleValue = StyleValue.parse(value);

            if (styleValue != null) {
                return (style, spanner) -> style.setMarginLeft(styleValue);
            }
        }

        if ("margin-right".equals(key)) {

            final StyleValue styleValue = StyleValue.parse(value);

            if (styleValue != null) {
                return (style, spanner) -> style.setMarginRight(styleValue);
            }
        }

        if ("margin".equals(key)) {
            return parseMargin(value);
        }

        if ("text-indent".equals(key)) {
            final StyleValue styleValue = StyleValue.parse(value);

            if (styleValue != null) {
                return (style, spanner) -> style.setTextIndent(styleValue);
            }
        }

        if ("display".equals(key)) {
            try {
                final Style.DisplayStyle displayStyle = Style.DisplayStyle.valueOf(value.toUpperCase());
                return (style, spanner) -> style.setDisplayStyle(displayStyle);
            } catch (IllegalArgumentException ia) {
                return null;
            }
        }

        if ("border-style".equals(key)) {
            try {
                final Style.BorderStyle borderStyle = Style.BorderStyle.valueOf(value.toUpperCase());
                return (style, spanner) -> style.setBorderStyle(borderStyle);
            } catch (IllegalArgumentException ia) {
                return null;
            }
        }

        if ("border-color".equals(key)) {
            try {
                final Integer borderColor = parseCSSColor(value);
                return (style, spanner) -> style.setBorderColor(borderColor);
            } catch (IllegalArgumentException ia) {
                return null;
            }
        }

        if ("border-width".equals(key)) {

            final StyleValue borderWidth = StyleValue.parse(value);
            if (borderWidth != null) {
                return (style, spanner) -> style.setBorderWidth(borderWidth);
            } else {
                return null;
            }
        }


        if ("border".equals(key)) {
            return parseBorder(value);
        }

        return null;
    }

    private static float translateFontSize(int fontSize) {

        switch (fontSize) {
            case 1:
                return 0.6f;
            case 2:
                return 0.8f;
            case 3:
                return 1.0f;
            case 4:
                return 1.2f;
            case 5:
                return 1.4f;
            case 6:
                return 1.6f;
            case 7:
                return 1.8f;
        }

        return 1.0f;
    }

    /**
     * Parses a border definition.
     * <p>
     * Border definitions are a complete mess, since the order is not set.
     */
    private static StyleUpdater parseBorder(String borderDefinition) {

        String[] parts = borderDefinition.split("\\s");

        StyleValue borderWidth = null;
        Integer borderColor = null;
        Style.BorderStyle borderStyle = null;

        for (String part : parts) {

            if (borderWidth == null) {

                borderWidth = StyleValue.parse(part);

                if (borderWidth != null) {
                    continue;
                }
            }

            if (borderColor == null) {
                try {
                    borderColor = parseCSSColor(part);
                    continue;
                } catch (IllegalArgumentException ia) {
                    //try next one
                }
            }

            if (borderStyle == null) {
                try {
                    borderStyle = Style.BorderStyle.valueOf(part.toUpperCase());
                } catch (IllegalArgumentException ia) {
                    //next loop iteration
                }
            }
        }

        final StyleValue finalBorderWidth = borderWidth;
        final Integer finalBorderColor = borderColor;
        final Style.BorderStyle finalBorderStyle = borderStyle;

        return (style, spanner) -> {

            if (finalBorderColor != null) {
                style = style.setBorderColor(finalBorderColor);
            }

            if (finalBorderWidth != null) {
                style = style.setBorderWidth(finalBorderWidth);
            }

            if (finalBorderStyle != null) {
                style = style.setBorderStyle(finalBorderStyle);
            }

            return style;
        };

    }

    private static StyleUpdater parseMargin(String marginValue) {

        String[] parts = marginValue.split("\\s");

        String bottomMarginString = "";
        String topMarginString = "";
        String leftMarginString = "";
        String rightMarginString = "";

        //See http://www.w3schools.com/css/css_margin.asp

        if (parts.length == 1) {
            bottomMarginString = parts[0];
            topMarginString = parts[0];
            leftMarginString = parts[0];
            rightMarginString = parts[0];
        } else if (parts.length == 2) {
            topMarginString = parts[0];
            bottomMarginString = parts[0];
            leftMarginString = parts[1];
            rightMarginString = parts[1];
        } else if (parts.length == 3) {
            topMarginString = parts[0];
            leftMarginString = parts[1];
            rightMarginString = parts[1];
            bottomMarginString = parts[2];
        } else if (parts.length == 4) {
            topMarginString = parts[0];
            rightMarginString = parts[1];
            bottomMarginString = parts[2];
            leftMarginString = parts[3];
        }

        final StyleValue marginBottom = StyleValue.parse(bottomMarginString);
        final StyleValue marginTop = StyleValue.parse(topMarginString);
        final StyleValue marginLeft = StyleValue.parse(leftMarginString);
        final StyleValue marginRight = StyleValue.parse(rightMarginString);


        return (style, spanner) -> {
            Style resultStyle = style;

            if (marginBottom != null) {
                resultStyle = resultStyle.setMarginBottom(marginBottom);
            }

            if (marginTop != null) {
                resultStyle = resultStyle.setMarginTop(marginTop);
            }

            if (marginLeft != null) {
                resultStyle = resultStyle.setMarginLeft(marginLeft);
            }

            if (marginRight != null) {
                resultStyle = resultStyle.setMarginRight(marginRight);
            }

            return resultStyle;
        };
    }

}
