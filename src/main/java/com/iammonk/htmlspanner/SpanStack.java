package com.iammonk.htmlspanner;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.iammonk.htmlspanner.css.CompiledRule;
import com.iammonk.htmlspanner.style.Style;

import org.htmlcleaner.TagNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Simple stack structure that Spans can be pushed on.
 * <p>
 * Handles the lookup and application of CSS styles.
 *
 * @author Alex Kuiper
 */
public class SpanStack {

    private final Stack<SpanCallback> spanItemStack = new Stack<SpanCallback>();

    private final Set<CompiledRule> rules = new HashSet<CompiledRule>();

    private final Map<TagNode, List<CompiledRule>> lookupCache = new HashMap<TagNode, List<CompiledRule>>();

    public void registerCompiledRule(CompiledRule rule) {
        this.rules.add(rule);
    }

    public Style getStyle(TagNode node, Style baseStyle) {

        if (!lookupCache.containsKey(node)) {
            List<CompiledRule> matchingRules = new ArrayList<CompiledRule>();
            for (CompiledRule rule : rules) {
                if (rule.matches(node)) {
                    matchingRules.add(rule);
                }
            }
            lookupCache.put(node, matchingRules);
        }

        Style result = baseStyle;

        for (CompiledRule rule : lookupCache.get(node)) {
            result = rule.applyStyle(result);
        }

        return result;
    }

    private static String option(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    public void pushSpan(final Object span, final int start, final int end) {

        if (end > start) {
            SpanCallback callback = (spanner, builder) -> builder.setSpan(span, start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spanItemStack.push(callback);
        }
    }

    public void pushSpan(SpanCallback callback) {
        spanItemStack.push(callback);
    }

    public void applySpans(HtmlSpanner spanner, SpannableStringBuilder builder) {
        while (!spanItemStack.isEmpty()) {
            spanItemStack.pop().applySpan(spanner, builder);
        }
    }
}
