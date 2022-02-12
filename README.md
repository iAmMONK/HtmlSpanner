# Android HTML rendering library

[![](https://jitpack.io/v/iAmMONK/HtmlSpanner.svg)](https://jitpack.io/#iAmMONK/HtmlSpanner)

Inspired by:

* HTMLSpanner https://github.com/NightWhistler/HtmlSpanner.

With code from:

* CSSParser https://github.com/corgrath/osbcp-css-parser

Original project seems to be unmaintained so this is a fork to update dependencies.

Might transfer the code to Kotlin if that will be requested.

Note from original maintainer:

HtmlSpanner started as the HTML rendering library for PageTurner, but looking through some questions
on StackOverflow I noticed how many people were struggling with the infamous ``Html.fromHtml()`` and
getting its results to display properly in TextViews.

HtmlSpanner allows you full control over how tags are rendered and gives you all the data about the
location of a tag in the text. This allows proper handling of anchors, tables, numbered lists and
unordered lists.

The default link implementation just opens URLs, but it can be easily overridden to support anchors.

HtmlSpanner uses HtmlCleaner to do most of the heavy lifting for parsing HTML files.

# CSS support

HtmlSpanner now also supports the most common subset of CSS: both style tags and style attributes
are parsed by default, and the style of all built-in tags can be updated.

# Supported Tags

* i
* em
* cite
* dfn
* b
* strong
* blockquote
* ul
* ol
* tt
* code
* style
* br
* p
* div
* h1
* h2
* h3
* h4
* h5
* h6
* pre
* big
* small
* sub
* sup
* center
* li
* a
* img
* font
* span

# Supported Style Attributes

* font-family
* text-alignment
* font-size
* font-weight
* font-style
* color
* background-color
* display
* margin-top
* margin-bottom
* margin-left
* margin-right
* text-indent
* border-style
* border-color
* border-style

# Usage

1. In root ``build.gradle`` add ``allprojects { maven { url 'https://jitpack.io' } } }``

2. In app module ``build.gradle`` add following dependency

``implementation 'com.github.iAmMONK:HtmlSpanner:$spannerVersion'``

3. In its simplest form, just call ``(new HtmlSpanner()).fromHtml()``(Java)
   or ``HtmlSpanner().fromHtml()``(Kotlin) to get similar output as Android's ``Html.fromHtml()``.

# HTMLCleaner Source

see http://htmlcleaner.sourceforge.net/index.php

The fork under https://github.com/amplafi/htmlcleaner can not be used on Android <= 2.1 as it uses
java.lang.String.isEmpty

