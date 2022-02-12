/*
 * Copyright (C) 2011 Alex Kuiper <http://www.nightwhistler.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iammonk.htmlspanner.handlers;

import android.text.SpannableStringBuilder;

import com.iammonk.htmlspanner.SpanStack;
import com.iammonk.htmlspanner.TagNodeHandler;

import org.htmlcleaner.TagNode;

/**
 * Adds a specified number of newlines.
 * <p>
 * Used to implement p and br tags.
 *
 * @author Alex Kuiper
 */
public class NewLineHandler extends WrappingHandler {

    private final int numberOfNewLines;

    /**
     * Creates this handler for a specified number of newlines.
     */
    public NewLineHandler(int howMany, TagNodeHandler wrappedHandler) {
        super(wrappedHandler);
        this.numberOfNewLines = howMany;
    }

    public void handleTagNode(TagNode node, SpannableStringBuilder builder,
                              int start, int end, SpanStack spanStack) {

        super.handleTagNode(node, builder, start, end, spanStack);

        for (int i = 0; i < numberOfNewLines; i++) {
            appendNewLine(builder);
        }
    }
}
