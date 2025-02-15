package com.iammonk.htmlspanner.handlers.attributes;

import android.text.SpannableStringBuilder;
import android.util.Log;

import com.iammonk.htmlspanner.SpanStack;
import com.iammonk.htmlspanner.css.CSSCompiler;
import com.iammonk.htmlspanner.handlers.StyledTextHandler;
import com.iammonk.htmlspanner.style.Style;

import org.htmlcleaner.TagNode;

/**
 * Handler which parses style attributes and modifies the style accordingly.
 */
public class StyleAttributeHandler extends WrappingStyleHandler {

    public StyleAttributeHandler(StyledTextHandler wrapHandler) {
        super(wrapHandler);
    }

    @Override
    public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, Style useStyle,
                              SpanStack spanStack) {

        String styleAttr = node.getAttributeByName("style");

        if (getSpanner().isAllowStyling() && styleAttr != null) {
            super.handleTagNode(node, builder, start, end,
                    parseStyleFromAttribute(useStyle, styleAttr),
                    spanStack);
        } else {
            super.handleTagNode(node, builder, start, end, useStyle, spanStack);
        }

    }

    private Style parseStyleFromAttribute(Style baseStyle, String attribute) {
        Style style = baseStyle;

        String[] pairs = attribute.split(";");
        for (String pair : pairs) {

            String[] keyVal = pair.split(":");

            if (keyVal.length != 2) {
                return baseStyle;
            }

            String key = keyVal[0].toLowerCase().trim();
            String value = keyVal[1].toLowerCase().trim();

            CSSCompiler.StyleUpdater updater = CSSCompiler.getStyleUpdater(key, value);

            if (updater != null) {
                style = updater.updateStyle(style, getSpanner());
            }

        }
        return style;
    }
}
