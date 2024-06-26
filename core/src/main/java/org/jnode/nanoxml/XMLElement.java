/* XMLElement.java
 *
 * $Revision: 1950 $
 * $Date: 2005-07-28 12:29:12 +0300 (Thu, 28 Jul 2005) $
 * $Name$
 *
 * This file is part of NanoXML 2 Lite.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 *****************************************************************************/

package org.jnode.nanoxml;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XMLElement is a representation of an XML object. The object is able to parse
 * XML code.
 * <P><DL>
 * <DT><B>Parsing XML Data</B></DT>
 * <DD>
 * You can parse XML data using the following code:
 * <UL><CODE>
 * XMLElement xml = new XMLElement();<BR>
 * FileReader reader = new FileReader("filename.xml");<BR>
 * xml.parseFromReader(reader);
 * </CODE></UL></DD></DL>
 * <DL><DT><B>Retrieving Attributes</B></DT>
 * <DD>
 * You can get the attributes of an element using the method
 * {@link #attributeNames() attributeNames}.
 * The attribute values can be retrieved using the method
 * {@link #getStringAttribute(String) getStringAttribute}.
 * The following example shows how to list the attributes of an element:
 * <UL><pre>
 *    XMLElement element = ...;
 *    for (String key : element.getAttributeNames()) {
 *       String value = element.getStringAttribute(key);
 *       System.out.println(key + " = " + value);
 *    }
 * </pre></UL></DD></DL>
 * <DL><DT><B>Retrieving Child Elements</B></DT>
 * <DD>
 * You can enumerate the children of an element using
 * {@link #iteratorChildren() iteratorChildren}.
 * The number of child elements can be retrieved using
 * {@link #countChildren() countChildren}.
 * </DD></DL>
 * <DL><DT><B>Elements Containing Character Data</B></DT>
 * <DD>
 * If an element contains character data, as in the following example:
 * <UL><CODE>
 * &lt;title&gt;The Title&lt;/title&gt;
 * </CODE></UL>
 * you can retrieve that data using the method
 * {@link #getContent() getContent}.
 * </DD></DL>
 * <DL><DT><B>Subclassing XMLElement</B></DT>
 * <DD>
 * When subclassing XMLElement, you need to override the method
 * {@link #createAnotherElement() createAnotherElement}
 * which has to return a new copy of the receiver.
 * </DD></DL>
 * <P>
 *
 * @see XMLParseException
 *
 * @author Marc De Scheemaecker
 *         &lt;<A href="mailto:cyberelf@mac.com">cyberelf@mac.com</A>&gt;
 * @version $Name$, $Revision: 1950 $
 */
public class XMLElement implements Serializable {

    /**
     * Serialization serial version ID.
     */
    @Serial
    private static final long serialVersionUID = 6685035139346394777L;

    /**
     * Major version of NanoXML. Classes with the same major and minor
     * version are binary compatible. Classes with the same major version
     * are source compatible. If the major version is different, you may
     * need to modify the client source code.
     *
     * @see XMLElement#NANOXML_MINOR_VERSION
     */
    public static final int NANOXML_MAJOR_VERSION = 2;

    /**
     * Minor version of NanoXML. Classes with the same major and minor
     * version are binary compatible. Classes with the same major version
     * are source compatible. If the major version is different, you may
     * need to modify the client source code.
     *
     * @see XMLElement#NANOXML_MAJOR_VERSION
     */
    public static final int NANOXML_MINOR_VERSION = 2;

    /**
     * The attributes given to the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field can be empty.
     *     <li>The field is never <code>null</code>.
     *     <li>The keys and the values are strings.
     * </ul></dd></dl>
     */
    private Map<String, String> attributes;

    /**
     * Child elements of the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field can be empty.
     *     <li>The field is never <code>null</code>.
     *     <li>The elements are instances of <code>XMLElement</code>
     *         or a subclass of <code>XMLElement</code>.
     * </ul></dd></dl>
     */
    private List<XMLElement> children;

    /**
     * The name of the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is <code>null</code> iff the element is not
     *         initialized by either parse or setName.
     *     <li>If the field is not <code>null</code>, it's not empty.
     *     <li>If the field is not <code>null</code>, it contains a valid
     *         XML identifier.
     * </ul></dd></dl>
     */
    private String name;

    /**
     * The #PCDATA content of the object.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is <code>null</code> iff the element is not a
     *         #PCDATA element.
     *     <li>The field can be any string, including the empty string.
     * </ul></dd></dl>
     */
    private String contents;

    /**
     * Conversion table for &amp;...; entities. The keys are the entity names
     * without the &amp; and ; delimiters.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is never <code>null</code>.
     *     <li>The field always contains the following associations:
     *         "lt"&nbsp;=&gt;&nbsp;"&lt;", "gt"&nbsp;=&gt;&nbsp;"&gt;",
     *         "quot"&nbsp;=&gt;&nbsp;"\"", "apos"&nbsp;=&gt;&nbsp;"'",
     *         "amp"&nbsp;=&gt;&nbsp;"&amp;"
     *     <li>The keys are strings
     *     <li>The values are char arrays
     * </ul></dd></dl>
     */
    private final Map<String, Object> entities;

    /**
     * The line number where the element starts.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li><code>lineNr &gt= 0</code>
     * </ul></dd></dl>
     */
    private final int lineNr;

    /**
     * <code>true</code> if the case of the element and attribute names
     * are case insensitive.
     */
    private final boolean ignoreCase;

    /**
     * <code>true</code> if the leading and trailing whitespace of #PCDATA
     * sections have to be ignored.
     */
    private final boolean ignoreWhitespace;

    /**
     * Character read too much.
     * This character provides push-back functionality to the input reader
     * without having to use a PushbackReader.
     * If there is no such character, this field is '\0'.
     */
    private char charReadTooMuch;

    /**
     * The reader provided by the caller of the parse method.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is not <code>null</code> while the parse method
     *         is running.
     * </ul></dd></dl>
     */
    private Reader reader;

    /**
     * The current line number in the source content.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>parserLineNr &gt; 0 while the parse method is running.
     * </ul></dd></dl>
     */
    private int parserLineNr;

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XMLElement(new Hashtable(), false, true)
     * </code></ul>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>iteratorChildren() => empty iteration
     *     <li>attributeNames() => empty set
     *     <li>getChildren() => empty list
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl>
     *
     * @see XMLElement#XMLElement(java.util.Map)
     *         XMLElement(Hashtable)
     * @see XMLElement#XMLElement(boolean)
     * @see XMLElement#XMLElement(java.util.Map,boolean)
     *         XMLElement(Hashtable, boolean)
     */
    public XMLElement()
    {
        this(new HashMap<>(), false, true, true);
    }

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XMLElement(entities, false, true)
     * </code></ul>
     *
     * @param entities The entity conversion table.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>iteratorChildren() => empty iteration
     *     <li>attributeNames() => empty set
     *     <li>getChildren() => empty list
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#XMLElement()
     * @see XMLElement#XMLElement(boolean)
     * @see XMLElement#XMLElement(java.util.Map,boolean)
     *         XMLElement(Hashtable, boolean)
     */
    public XMLElement(Map<String, Object> entities)
    {
        this(entities, false, true, true);
    }

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XMLElement(new Hashtable(), skipLeadingWhitespace, true)
     * </code></ul>
     *
     * @param skipLeadingWhitespace the skipLeadingWhitespace
     *     <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     *
     * </dl><dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>iteratorChildren() => empty iteration
     *     <li>attributeNames() => empty set
     *     <li>getChildren() => empty list
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#XMLElement()
     * @see XMLElement#XMLElement(java.util.Map)
     *         XMLElement(Hashtable)
     * @see XMLElement#XMLElement(java.util.Map,boolean)
     *         XMLElement(Hashtable, boolean)
     */
    public XMLElement(boolean skipLeadingWhitespace)
    {
        this(new HashMap<>(), skipLeadingWhitespace, true, true);
    }

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XMLElement(entities, skipLeadingWhitespace, true)
     * </code></ul>
     *
     * @param entities The entity conversion table.
     * @param skipLeadingWhitespace <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>iteratorChildren() => empty iteration
     *     <li>attributeNames() => empty set
     *     <li>getChildren() => empty list
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#XMLElement()
     * @see XMLElement#XMLElement(boolean)
     * @see XMLElement#XMLElement(java.util.Map)
     *         XMLElement(Hashtable)
     */
    public XMLElement(Map<String, Object> entities, boolean skipLeadingWhitespace) {
        this(entities, skipLeadingWhitespace, true, true);
    }

    /**
     * Creates and initializes a new XML element.
     *
     * @param entities The entity conversion table.
     * @param skipLeadingWhitespace <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     * @param ignoreCase <code>true</code> if the case of element and attribute names have
     *     to be ignored.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>iteratorChildren() => empty iteration
     *     <li>attributeNames() => empty set
     *     <li>getChildren() => empty list
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#XMLElement()
     * @see XMLElement#XMLElement(boolean)
     * @see XMLElement#XMLElement(java.util.Map)
     *         XMLElement(Hashtable)
     * @see XMLElement#XMLElement(java.util.Map,boolean)
     *         XMLElement(Hashtable, boolean)
     */
    public XMLElement(Map<String, Object> entities,
                      boolean skipLeadingWhitespace,
                      boolean ignoreCase) {
        this(entities, skipLeadingWhitespace, true, ignoreCase);
    }

    /**
     * Creates and initializes a new XML element.
     * <P>
     * This constructor should <I>only</I> be called from
     * {@link #createAnotherElement()}
     * to create child elements.
     *
     * @param entities The entity conversion table.
     * @param skipLeadingWhitespace <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     * @param fillBasicConversionTable <code>true</code> if the basic entities need to be added to
     *     the entity list.
     * @param ignoreCase <code>true</code> if the case of element and attribute names have
     *     to be ignored.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     *     <li>if <code>fillBasicConversionTable == false</code>
     *         then <code>entities</code> contains at least the following
     *         entries: <code>amp</code>, <code>lt</code>, <code>gt</code>,
     *         <code>apos</code> and <code>quot</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>iteratorChildren() => empty iteration
     *     <li>attributeNames() => empty set
     *     <li>getChildren() => empty list
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#createAnotherElement()
     */
    protected XMLElement(Map<String, Object> entities,
                         boolean   skipLeadingWhitespace,
                         boolean   fillBasicConversionTable,
                         boolean   ignoreCase) {
        this.ignoreWhitespace = skipLeadingWhitespace;
        this.ignoreCase = ignoreCase;
        this.name = null;
        this.contents = "";
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.entities = entities;
        this.lineNr = 0;
        for (String key : this.entities.keySet()) {
            Object value = this.entities.get(key);
            if (value instanceof String) {
                value = ((String) value).toCharArray();
                this.entities.put(key, value);
            }
        }
        if (fillBasicConversionTable) {
            this.entities.put("amp", new char[] { '&' });
            this.entities.put("quot", new char[] { '"' });
            this.entities.put("apos", new char[] { '\'' });
            this.entities.put("lt", new char[] { '<' });
            this.entities.put("gt", new char[] { '>' });
        }
    }

    /**
     * Adds a child element.
     *
     * @param child The child element to add.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>child != null</code>
     *     <li><code>child.getName() != null</code>
     *     <li><code>child</code> does not have a parent element
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => old.countChildren() + 1
     *     <li>iteratorChildren() => old.iteratorChildren() + child
     *     <li>getChildren() => old.iteratorChildren() + child
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#countChildren()
     * @see XMLElement#iteratorChildren()
     * @see XMLElement#getChildren()
     * @see XMLElement#removeChild(XMLElement)
     *         removeChild(XMLElement)
     */
    public void addChild(XMLElement child)
    {
        this.children.add(child);
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>value != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>attributeNames() => old.ttributeNames() + name
     *     <li>getAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getAttribute(String)
     *         getAttribute(String)
     * @see XMLElement#getAttribute(String, Object)
     *         getAttribute(String, Object)
     * @see XMLElement#getAttribute(String, java.util.Hashtable, String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getStringAttribute(String)
     *         getStringAttribute(String)
     * @see XMLElement#getStringAttribute(String, String)
     *         getStringAttribute(String, String)
     * @see XMLElement#getStringAttribute(String, java.util.Hashtable, String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     */
    public void setAttribute(String name, Object value) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        this.attributes.put(name, value.toString());
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     *
     * @deprecated Use {@link #setAttribute(String, Object)} instead.
     */
    public void addProperty(String name, Object value) {
        this.setAttribute(name, value);
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>attributeNames() => old.attributeNames() + name
     *     <li>getIntAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getIntAttribute(String)
     *         getIntAttribute(String)
     * @see XMLElement#getIntAttribute(String, int)
     *         getIntAttribute(String, int)
     * @see XMLElement#getIntAttribute(String, java.util.Hashtable, String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public void setIntAttribute(String name, int value) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        this.attributes.put(name, Integer.toString(value));
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     *
     * @deprecated Use {@link #setIntAttribute(String, int)} instead.
     */
    public void addProperty(String name, int value) {
        this.setIntAttribute(name, value);
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>attributeNames() => old.attributeNames() + name
     *     <li>getDoubleAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getDoubleAttribute(String)
     *         getDoubleAttribute(String)
     * @see XMLElement#getDoubleAttribute(String, double)
     *         getDoubleAttribute(String, double)
     * @see XMLElement#getDoubleAttribute(String, java.util.Hashtable, String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     */
    public void setDoubleAttribute(String name, double value) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        this.attributes.put(name, Double.toString(value));
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     *
     * @deprecated Use {@link #setDoubleAttribute(String, double)} instead.
     */
    public void addProperty(String name, double value) {
        this.setDoubleAttribute(name, value);
    }

    /**
     * Returns the number of child elements of the element.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result >= 0</code>
     * </ul></dd></dl>
     *
     * @see XMLElement#addChild(XMLElement)
     *         addChild(XMLElement)
     * @see XMLElement#iteratorChildren()
     * @see XMLElement#getChildren()
     * @see XMLElement#removeChild(XMLElement)
     *         removeChild(XMLElement)
     */
    public int countChildren()
    {
        return this.children.size();
    }

    /**
     * Enumerates the attribute names.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     *
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#getAttribute(String)
     *         getAttribute(String)
     * @see XMLElement#getAttribute(String, Object)
     *         getAttribute(String, String)
     * @see XMLElement#getAttribute(String, java.util.Hashtable, String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getStringAttribute(String)
     *         getStringAttribute(String)
     * @see XMLElement#getStringAttribute(String, String)
     *         getStringAttribute(String, String)
     * @see XMLElement#getStringAttribute(String, java.util.Hashtable, String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getIntAttribute(String)
     *         getIntAttribute(String)
     * @see XMLElement#getIntAttribute(String, int)
     *         getIntAttribute(String, int)
     * @see XMLElement#getIntAttribute(String, java.util.Hashtable, String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getDoubleAttribute(String)
     *         getDoubleAttribute(String)
     * @see XMLElement#getDoubleAttribute(String, double)
     *         getDoubleAttribute(String, double)
     * @see XMLElement#getDoubleAttribute(String, java.util.Hashtable, String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getBooleanAttribute(String, String, String, boolean)
     *         getBooleanAttribute(String, String, String, boolean)
     */
    public Set<String> attributeNames()
    {
        return Collections.unmodifiableSet(this.attributes.keySet());
    }

    /**
     * Enumerates the child elements.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     *
     * @see XMLElement#addChild(XMLElement)
     *         addChild(XMLElement)
     * @see XMLElement#countChildren()
     * @see XMLElement#getChildren()
     * @see XMLElement#removeChild(XMLElement)
     *         removeChild(XMLElement)
     */
    public Iterator<XMLElement> iteratorChildren()
    {
        return this.children.iterator();
    }

    /**
     * Returns the child elements as a Vector. It is safe to modify this
     * Vector.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     *
     * @see XMLElement#addChild(XMLElement)
     *         addChild(XMLElement)
     * @see XMLElement#countChildren()
     * @see XMLElement#iteratorChildren()
     * @see XMLElement#removeChild(XMLElement)
     *         removeChild(XMLElement)
     */
    public List<XMLElement> getChildren()
    {
        return new ArrayList<>(this.children);
    }

    /**
     * Returns the PCDATA content of the object. If there is no such content,
     * <CODE>null</CODE> is returned.
     *
     * @deprecated Use {@link #getContent()} instead.
     */
    public String getContents()
    {
        return this.getContent();
    }

    /**
     * Returns the PCDATA content of the object. If there is no such content,
     * <CODE>null</CODE> is returned.
     *
     * @see XMLElement#setContent(String)
     *         setContent(String)
     */
    public String getContent()
    {
        return this.contents;
    }

    /**
     * Returns the line nr in the source data on which the element is found.
     * This method returns <code>0</code> there is no associated source data.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result >= 0</code>
     * </ul></dd></dl>
     */
    public int getLineNr()
    {
        return this.lineNr;
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>null</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getAttribute(String, Object)
     *         getAttribute(String, Object)
     * @see XMLElement#getAttribute(String, java.util.Hashtable, String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     */
    public Object getAttribute(String name)
    {
        return this.getAttribute(name, null);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getAttribute(String)
     *         getAttribute(String)
     * @see XMLElement#getAttribute(String, java.util.Hashtable, String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     */
    public Object getAttribute(String name,
                               Object defaultValue)
    {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        Object value = this.attributes.get(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     * If the attribute doesn't exist, the value corresponding to defaultKey
     * is returned.
     * <P>
     * As an example, if valueSet contains the mapping <code>"one" =>
     * "1"</code>
     * and the element contains the attribute <code>attr="one"</code>, then
     * <code>getAttribute("attr", mapping, defaultKey, false)</code> returns
     * <code>"1"</code>.
     *
     * @param name The name of the attribute.
     * @param valueSet Hashtable mapping keys to values.
     * @param defaultKey Key to use if the attribute is missing.
     * @param allowLiterals <code>true</code> if literals are valid.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>valueSet</code> != null
     *     <li>the keys of <code>valueSet</code> are strings
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getAttribute(String)
     *         getAttribute(String)
     * @see XMLElement#getAttribute(String, Object)
     *         getAttribute(String, Object)
     */
    public Object getAttribute(String    name,
                               Hashtable<?, ?> valueSet,
                               String    defaultKey,
                               boolean   allowLiterals)
    {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        String key = this.attributes.get(name);
        Object result;
        if (key == null) {
            key = defaultKey;
        }
        result = valueSet.get(key);
        if (result == null) {
            if (allowLiterals) {
                result = key;
            } else {
                throw this.invalidValue(name, key);
            }
        }
        return result;
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>null</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getStringAttribute(String, String)
     *         getStringAttribute(String, String)
     * @see XMLElement#getStringAttribute(String, java.util.Hashtable, String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     */
    public String getStringAttribute(String name)
    {
        return this.getStringAttribute(name, null);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getStringAttribute(String)
     *         getStringAttribute(String)
     * @see XMLElement#getStringAttribute(String, java.util.Hashtable, String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     */
    public String getStringAttribute(String name, String defaultValue) {
        return (String) this.getAttribute(name, defaultValue);
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     * If the attribute doesn't exist, the value corresponding to defaultKey
     * is returned.
     * <P>
     * As an example, if valueSet contains the mapping <code>"one" =>
     * "1"</code>
     * and the element contains the attribute <code>attr="one"</code>, then
     * <code>getAttribute("attr", mapping, defaultKey, false)</code> returns
     * <code>"1"</code>.
     *
     * @param name The name of the attribute.
     * @param valueSet Hashtable mapping keys to values.
     * @param defaultKey Key to use if the attribute is missing.
     * @param allowLiterals <code>true</code> if literals are valid.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>valueSet</code> != null
     *     <li>the keys of <code>valueSet</code> are strings
     *     <li>the values of <code>valueSet</code> are strings
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getStringAttribute(String)
     *         getStringAttribute(String)
     * @see XMLElement#getStringAttribute(String,
     *                                            String)
     *         getStringAttribute(String, String)
     */
    public String getStringAttribute(String    name,
                                     Hashtable<?, ?> valueSet,
                                     String    defaultKey,
                                     boolean   allowLiterals)
    {
        return (String) this.getAttribute(name, valueSet, defaultKey,
                                          allowLiterals);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>0</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getIntAttribute(String, int)
     *         getIntAttribute(String, int)
     * @see XMLElement#getIntAttribute(String, java.util.Hashtable, String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public int getIntAttribute(String name)
    {
        return this.getIntAttribute(name, 0);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getIntAttribute(String)
     *         getIntAttribute(String)
     * @see XMLElement#getIntAttribute(String,java.util.Hashtable,String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public int getIntAttribute(String name, int defaultValue) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        String value = this.attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, value);
            }
        }
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     * If the attribute doesn't exist, the value corresponding to defaultKey
     * is returned.
     * <P>
     * As an example, if valueSet contains the mapping <code>"one" => 1</code>
     * and the element contains the attribute <code>attr="one"</code>, then
     * <code>getIntAttribute("attr", mapping, defaultKey, false)</code> returns
     * <code>1</code>.
     *
     * @param name The name of the attribute.
     * @param valueSet Hashtable mapping keys to values.
     * @param defaultKey Key to use if the attribute is missing.
     * @param allowLiteralNumbers <code>true</code> if literal numbers are valid.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>valueSet</code> != null
     *     <li>the keys of <code>valueSet</code> are strings
     *     <li>the values of <code>valueSet</code> are Integer objects
     *     <li><code>defaultKey</code> is either <code>null</code>, a
     *         key in <code>valueSet</code> or an integer.
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getIntAttribute(String)
     *         getIntAttribute(String)
     * @see XMLElement#getIntAttribute(String, int)
     *         getIntAttribute(String, int)
     */
    public int getIntAttribute(String    name,
                               Hashtable<?, ?> valueSet,
                               String    defaultKey,
                               boolean   allowLiteralNumbers) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        String key = this.attributes.get(name);
        Integer result;
        if (key == null) {
            key = defaultKey;
        }
        try {
            result = (Integer) valueSet.get(key);
        } catch (ClassCastException e) {
            throw this.invalidValueSet(name);
        }
        if (result == null) {
            if (! allowLiteralNumbers) {
                throw this.invalidValue(name, key);
            }
            try {
                result = Integer.valueOf(key);
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, key);
            }
        }
        return result;
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>0.0</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getDoubleAttribute(String, double)
     *         getDoubleAttribute(String, double)
     * @see XMLElement#getDoubleAttribute(String,java.util.Hashtable,String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     */
    public double getDoubleAttribute(String name)
    {
        return this.getDoubleAttribute(name, 0.);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getDoubleAttribute(String)
     *         getDoubleAttribute(String)
     * @see XMLElement#getDoubleAttribute(String,java.util.Hashtable,String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     */
    public double getDoubleAttribute(String name, double defaultValue) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        String value = this.attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, value);
            }
        }
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     * If the attribute doesn't exist, the value corresponding to defaultKey
     * is returned.
     * <P>
     * As an example, if valueSet contains the mapping <code>"one" =&gt;
     * 1.0</code>
     * and the element contains the attribute <code>attr="one"</code>, then
     * <code>getDoubleAttribute("attr", mapping, defaultKey, false)</code>
     * returns <code>1.0</code>.
     *
     * @param name The name of the attribute.
     * @param valueSet Hashtable mapping keys to values.
     * @param defaultKey Key to use if the attribute is missing.
     * @param allowLiteralNumbers <code>true</code> if literal numbers are valid.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>valueSet != null</code>
     *     <li>the keys of <code>valueSet</code> are strings
     *     <li>the values of <code>valueSet</code> are Double objects
     *     <li><code>defaultKey</code> is either <code>null</code>, a
     *         key in <code>valueSet</code> or a double.
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#attributeNames()
     * @see XMLElement#getDoubleAttribute(String)
     *         getDoubleAttribute(String)
     * @see XMLElement#getDoubleAttribute(String, double)
     *         getDoubleAttribute(String, double)
     */
    public double getDoubleAttribute(String name,
                                     Hashtable<?, ?> valueSet,
                                     String defaultKey,
                                     boolean allowLiteralNumbers) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        String key = this.attributes.get(name);
        Double result;
        if (key == null) {
            key = defaultKey;
        }
        try {
            result = (Double) valueSet.get(key);
        } catch (ClassCastException e) {
            throw this.invalidValueSet(name);
        }
        if (result == null) {
            if (! allowLiteralNumbers) {
                throw this.invalidValue(name, key);
            }
            try {
                result = Double.valueOf(key);
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, key);
            }
        }
        return result;
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     * If the value of the attribute is equal to <code>trueValue</code>,
     * <code>true</code> is returned.
     * If the value of the attribute is equal to <code>falseValue</code>,
     * <code>false</code> is returned.
     * If the value doesn't match <code>trueValue</code> or
     * <code>falseValue</code>, an exception is thrown.
     *
     * @param name         The name of the attribute.
     * @param trueValue    The value associated with <code>true</code>.
     * @param falseValue   The value associated with <code>true</code>.
     * @param defaultValue Value to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>trueValue</code> and <code>falseValue</code>
     *         are different strings.
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#removeAttribute(String)
     *         removeAttribute(String)
     * @see XMLElement#attributeNames()
     */
    public boolean getBooleanAttribute(String  name,
                                       String  trueValue,
                                       String  falseValue,
                                       boolean defaultValue) {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        String value = this.attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else if (value.equals(trueValue)) {
            return true;
        } else if (value.equals(falseValue)) {
            return false;
        } else {
            throw this.invalidValue(name, value);
        }
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     *
     * @deprecated Use {@link #getIntAttribute(String, java.util.Hashtable, String, boolean)} instead.
     */
    public int getIntProperty(String name, Hashtable<?, ?> valueSet, String defaultKey) {
        return this.getIntAttribute(name, valueSet, defaultKey, false);
    }

    /**
     * Returns an attribute.
     *
     * @deprecated Use {@link #getStringAttribute(String)} instead.
     */
    public String getProperty(String name)
    {
        return this.getStringAttribute(name);
    }

    /**
     * Returns an attribute.
     *
     * @deprecated Use {@link #getStringAttribute(String, String)} instead.
     */
    public String getProperty(String name, String defaultValue) {
        return this.getStringAttribute(name, defaultValue);
    }

    /**
     * Returns an attribute.
     *
     * @deprecated Use {@link #getIntAttribute(String, int)} instead.
     */
    public int getProperty(String name, int defaultValue) {
        return this.getIntAttribute(name, defaultValue);
    }

    /**
     * Returns an attribute.
     *
     * @deprecated Use {@link #getDoubleAttribute(String, double)} instead.
     */
    public double getProperty(String name, double defaultValue) {
        return this.getDoubleAttribute(name, defaultValue);
    }

    /**
     * Returns an attribute.
     *
     * @deprecated Use {@link #getBooleanAttribute(String, String, String, boolean)} instead.
     */
    public boolean getProperty(String  key,
                               String  trueValue,
                               String  falseValue,
                               boolean defaultValue) {
        return this.getBooleanAttribute(key, trueValue, falseValue, defaultValue);
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     *
     * @deprecated Use {@link #getAttribute(String, java.util.Hashtable, String, boolean)} instead.
     */
    public Object getProperty(String    name,
                              Hashtable<?, ?> valueSet,
                              String    defaultKey)
    {
        return this.getAttribute(name, valueSet, defaultKey, false);
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     *
     * @deprecated Use {@link #getStringAttribute(String,
     *             java.util.Hashtable, String, boolean)} instead.
     */
    public String getStringProperty(String    name,
                                    Hashtable<?, ?> valueSet,
                                    String    defaultKey)
    {
        return this.getStringAttribute(name, valueSet, defaultKey, false);
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     *
     * @deprecated Use {@link #getIntAttribute(String,
     *             java.util.Hashtable, String, boolean)} instead.
     */
    public int getSpecialIntProperty(String    name,
                                     Hashtable<?, ?> valueSet,
                                     String    defaultKey)
    {
        return this.getIntAttribute(name, valueSet, defaultKey, true);
    }

    /**
     * Returns an attribute by looking up a key in a hashtable.
     *
     * @deprecated Use {@link #getDoubleAttribute(String,
     *             java.util.Hashtable, String, boolean)} instead.
     */
    public double getSpecialDoubleProperty(String    name,
                                           Hashtable<?, ?> valueSet,
                                           String    defaultKey)
    {
        return this.getDoubleAttribute(name, valueSet, defaultKey, true);
    }

    /**
     * Returns the name of the element.
     *
     * @see XMLElement#setName(String) setName(String)
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the name of the element.
     *
     * @deprecated Use {@link #getName()} instead.
     */
    public String getTagName()
    {
        return this.getName();
    }

    /**
     * Reads one XML element from a java.io.Reader and parses it.
     *
     * @param reader the reader
     *     The reader from which to retrieve the XML data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>reader != null</code>
     *     <li><code>reader</code> is not closed
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     *     <li>the reader points to the first character following the last
     *         '&gt;' character of the XML element
     * </ul></dd></dl><dl>
     *
     * @throws java.io.IOException when an error occurs
     *     If an error occured while reading the input.
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the read data.
     */
    public void parseFromReader(Reader reader)
    throws IOException, XMLParseException
    {
        this.parseFromReader(reader, /*startingLineNr*/ 1);
    }

    /**
     * Reads one XML element from a java.io.Reader and parses it.
     *
     * @param reader the reader
     *     The reader from which to retrieve the XML data.
     * @param startingLineNr the startingLineNr
     *     The line number of the first line in the data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>reader != null</code>
     *     <li><code>reader</code> is not closed
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     *     <li>the reader points to the first character following the last
     *         '&gt;' character of the XML element
     * </ul></dd></dl><dl>
     *
     * @throws java.io.IOException when an error occurs
     *     If an error occured while reading the input.
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the read data.
     */
    public void parseFromReader(Reader reader,
                                int    startingLineNr)
        throws IOException, XMLParseException
    {
        this.name = null;
        this.contents = "";
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.charReadTooMuch = '\0';
        this.reader = reader;
        this.parserLineNr = startingLineNr;

        for (;;) {
            char ch = this.scanWhitespace();

            if (ch != '<') {
                throw this.expectedInput("<");
            }

            ch = this.readChar();

            if ((ch == '!') || (ch == '?')) {
                this.skipSpecialTag(0);
            } else {
                this.unreadChar(ch);
                this.scanElement(this);
                return;
            }
        }
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param string the string
     *     The string from which to read the XML data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>string.length() &gt; 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the string.
     */
    public void parseString(String string)
        throws XMLParseException
    {
        try {
            this.parseFromReader(new StringReader(string),
                                 /*startingLineNr*/ 1);
        } catch (IOException e) {
            // Java exception handling suxx
        }
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param string the string
     *     The String from which to read the XML data.
     * @param offset the offset
     *     The first character in <code>string</code> to scan.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>offset &lt; string.length()</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the string.
     */
    public void parseString(String string,
                            int    offset)
        throws XMLParseException
    {
        this.parseString(string.substring(offset));
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param string the string
     *     The String from which to read the XML data.
     * @param offset the offset
     *     The first character in <code>string</code> to scan.
     * @param end the end
     *     The character where to stop scanning.
     *     This character is not scanned.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>end &lt;= string.length()</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the string.
     */
    public void parseString(String string,
                            int    offset,
                            int    end)
        throws XMLParseException
    {
        this.parseString(string.substring(offset, end));
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param string the string
     *     The String from which to read the XML data.
     * @param offset the offset
     *     The first character in <code>string</code> to scan.
     * @param end the end
     *     The character where to stop scanning.
     *     This character is not scanned.
     * @param startingLineNr the startingLineNr
     *     The line number of the first line in the data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>end &lt;= string.length()</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the string.
     */
    public void parseString(String string,
                            int    offset,
                            int    end,
                            int    startingLineNr)
        throws XMLParseException
    {
        string = string.substring(offset, end);
        try {
            this.parseFromReader(new StringReader(string), startingLineNr);
        } catch (IOException e) {
            // Java exception handling suxx
        }
    }

    /**
     * Reads one XML element from a char array and parses it.
     *
     * @param input the input
     *     The array from which to read the XML data.
     * @param offset the offset
     *     The first character in <code>string</code> to scan.
     * @param end the end
     *     The character where to stop scanning.
     *     This character is not scanned.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>input != null</code>
     *     <li><code>end &lt;= input.length</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the string.
     */
    public void parseCharArray(char[] input,
                               int    offset,
                               int    end)
        throws XMLParseException
    {
        this.parseCharArray(input, offset, end, /*startingLineNr*/ 1);
    }

    /**
     * Reads one XML element from a char array and parses it.
     *
     * @param input the input
     *     The array from which to read the XML data.
     * @param offset the offset
     *     The first character in <code>string</code> to scan.
     * @param end the end
     *     The character where to stop scanning.
     *     This character is not scanned.
     * @param startingLineNr the startingLineNr
     *     The line number of the first line in the data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>input != null</code>
     *     <li><code>end &lt;= input.length</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws XMLParseException when an error occurs
     *     If an error occured while parsing the string.
     */
    public void parseCharArray(char[] input,
                               int    offset,
                               int    end,
                               int    startingLineNr)
        throws XMLParseException
    {
        try {
            Reader reader = new CharArrayReader(input, offset, end);
            this.parseFromReader(reader, startingLineNr);
        } catch (IOException e) {
            // This exception will never happen.
        }
    }

    /**
     * Removes a child element.
     *
     * @param child the child
     *     The child element to remove.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>child != null</code>
     *     <li><code>child</code> is a child element of the receiver
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => old.countChildren() - 1
     *     <li>iteratorChildren() => old.iteratorChildren() - child
     *     <li>getChildren() => old.iteratorChildren() - child
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#addChild(XMLElement)
     *         addChild(XMLElement)
     * @see XMLElement#countChildren()
     * @see XMLElement#iteratorChildren()
     * @see XMLElement#getChildren()
     */
    public void removeChild(XMLElement child)
    {
        this.children.remove(child);
    }

    /**
     * Removes an attribute.
     *
     * @param name the name
     *     The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>ttributeNames() => old.attributeNames() - name
     *     <li>getAttribute(name) => <code>null</code>
     * </ul></dd></dl><dl>
     *
     * @see XMLElement#attributeNames()
     * @see XMLElement#setDoubleAttribute(String, double)
     *         setDoubleAttribute(String, double)
     * @see XMLElement#setIntAttribute(String, int)
     *         setIntAttribute(String, int)
     * @see XMLElement#setAttribute(String, Object)
     *         setAttribute(String, Object)
     * @see XMLElement#getAttribute(String)
     *         getAttribute(String)
     * @see XMLElement#getAttribute(String, Object)
     *         getAttribute(String, Object)
     * @see XMLElement#getAttribute(String,
     *                                      java.util.Hashtable,
     *                                      String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getStringAttribute(String)
     *         getStringAttribute(String)
     * @see XMLElement#getStringAttribute(String,
     *                                            String)
     *         getStringAttribute(String, String)
     * @see XMLElement#getStringAttribute(String,
     *                                            java.util.Hashtable,
     *                                            String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getIntAttribute(String)
     *         getIntAttribute(String)
     * @see XMLElement#getIntAttribute(String, int)
     *         getIntAttribute(String, int)
     * @see XMLElement#getIntAttribute(String,
     *                                         java.util.Hashtable,
     *                                         String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getDoubleAttribute(String)
     *         getDoubleAttribute(String)
     * @see XMLElement#getDoubleAttribute(String, double)
     *         getDoubleAttribute(String, double)
     * @see XMLElement#getDoubleAttribute(String,
     *                                            java.util.Hashtable,
     *                                            String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     * @see XMLElement#getBooleanAttribute(String,
     *                                             String,
     *                                             String, boolean)
     *         getBooleanAttribute(String, String, String, boolean)
     */
    public void removeAttribute(String name)
    {
        if (this.ignoreCase) {
            name = name.toUpperCase();
        }
        this.attributes.remove(name);
    }

    /**
     * Removes an attribute.
     *
     * @param name the name
     *     The name of the attribute.
     *
     * @deprecated Use {@link #removeAttribute(String)
     *             removeAttribute} instead.
     */
    public void removeProperty(String name)
    {
        this.removeAttribute(name);
    }

    /**
     * Removes an attribute.
     *
     * @param name the name
     *     The name of the attribute.
     *
     * @deprecated Use {@link #removeAttribute(String)
     *             removeAttribute} instead.
     */
    public void removeChild(String name)
    {
        this.removeAttribute(name);
    }

    /**
     * Creates a new similar XML element.
     * <P>
     * You should override this method when subclassing XMLElement.
     */
    protected XMLElement createAnotherElement()
    {
        return new XMLElement(this.entities,
                              this.ignoreWhitespace,
                              false,
                              this.ignoreCase);
    }

    /**
     * Changes the content string.
     *
     * @param content the content
     *     The new content string.
     */
    public void setContent(String content)
    {
        this.contents = content;
    }

    /**
     * Changes the name of the element.
     *
     * @param name the name
     *     The new name.
     *
     * @deprecated Use {@link #setName(String) setName} instead.
     */
    public void setTagName(String name)
    {
        this.setName(name);
    }

    /**
     * Changes the name of the element.
     *
     * @param name the name
     *     The new name.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * @see XMLElement#getName()
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Writes the XML element to a string.
     *
     * @see XMLElement#write(java.io.Writer) write(Writer)
     */
    public String toString()
    {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            this.write(writer);
            writer.flush();
            return out.toString();
        } catch (IOException e) {
            // Java exception handling suxx
            return super.toString();
        }
    }

    /**
     * Writes the XML element to a writer.
     *
     * @param writer the writer
     *     The writer to write the XML data to.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     * </ul></dd></dl>
     *
     * @throws java.io.IOException when an error occurs
     *      If the data could not be written to the writer.
     *
     * @see XMLElement#toString()
     */
    public void write(Writer writer)
        throws IOException
    {
        if (this.name == null) {
            this.writeEncoded(writer, this.contents);
            return;
        }
        writer.write('<');
        writer.write(this.name);
        if (! this.attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : this.attributes.entrySet()) {
                writer.write(' ');
                String key = entry.getKey();
                String value = entry.getValue();
                writer.write(key);
                writer.write('='); writer.write('"');
                this.writeEncoded(writer, value);
                writer.write('"');
            }
        }
        if ((this.contents != null) && (!this.contents.isEmpty())) {
            writer.write('>');
            this.writeEncoded(writer, this.contents);
            writer.write('<'); writer.write('/');
            writer.write(this.name);
            writer.write('>');
        } else if (this.children.isEmpty()) {
            writer.write('/'); writer.write('>');
        } else {
            writer.write('>');
            for (XMLElement child : this.children) {
                child.write(writer);
            }
            writer.write('<'); writer.write('/');
            writer.write(this.name);
            writer.write('>');
        }
    }

    /**
     * Writes a string encoded to a writer.
     *
     * @param writer the writer
     *     The writer to write the XML data to.
     * @param str the str
     *     The string to write encoded.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     *     <li><code>str != null</code>
     * </ul></dd></dl>
     */
    protected void writeEncoded(Writer writer,
                                String str)
        throws IOException
    {
        for (int i = 0; i < str.length(); i += 1) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    writer.write('&'); writer.write('l'); writer.write('t');
                    writer.write(';');
                    break;
                case '>':
                    writer.write('&'); writer.write('g'); writer.write('t');
                    writer.write(';');
                    break;
                case '&':
                    writer.write('&'); writer.write('a'); writer.write('m');
                    writer.write('p'); writer.write(';');
                    break;
                case '"':
                    writer.write('&'); writer.write('q'); writer.write('u');
                    writer.write('o'); writer.write('t'); writer.write(';');
                    break;
                case '\'':
                    writer.write('&'); writer.write('a'); writer.write('p');
                    writer.write('o'); writer.write('s'); writer.write(';');
                    break;
                default:
                    int unicode = ch;
                    if ((unicode < 32) || (unicode > 126)) {
                        writer.write('&'); writer.write('#');
                        writer.write('x');
                        writer.write(Integer.toString(unicode, 16));
                        writer.write(';');
                    } else {
                        writer.write(ch);
                    }
            }
        }
    }

    /**
     * Scans an identifier from the current reader.
     * The scanned identifier is appended to <code>result</code>.
     *
     * @param result the result
     *     The buffer in which the scanned identifier will be put.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     *     <li>The next character read from the reader is a valid first
     *         character of an XML identifier.
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>The next character read from the reader won't be an identifier
     *         character.
     * </ul></dd></dl><dl>
     */
    protected void scanIdentifier(StringBuffer result)
        throws IOException
    {
        for (;;) {
            char ch = this.readChar();
            if (((ch < 'A') || (ch > 'Z')) && ((ch < 'a') || (ch > 'z'))
                && ((ch < '0') || (ch > '9')) && (ch != '_') && (ch != '.')
                && (ch != ':') && (ch != '-') && (ch <= '~')) {
                this.unreadChar(ch);
                return;
            }
            result.append(ch);
        }
    }

    /**
     * This method scans an identifier from the current reader.
     *
     * @return the next character following the whitespace.
     */
    protected char scanWhitespace()
        throws IOException
    {
        for (;;) {
            char ch = this.readChar();
            switch (ch) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    break;
                default:
                    return ch;
            }
        }
    }

    /**
     * This method scans an identifier from the current reader.
     * The scanned whitespace is appended to <code>result</code>.
     *
     * @return the next character following the whitespace.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     */
    protected char scanWhitespace(StringBuffer result)
        throws IOException
    {
        for (;;) {
            char ch = this.readChar();
            switch (ch) {
                case ' ':
                case '\t':
                case '\n':
                    result.append(ch);
                case '\r':
                    break;
                default:
                    return ch;
            }
        }
    }

    /**
     * This method scans a delimited string from the current reader.
     * The scanned string without delimiters is appended to
     * <code>string</code>.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li>the next char read is the string delimiter
     * </ul></dd></dl>
     */
    protected void scanString(StringBuffer string)
        throws IOException
    {
        char delimiter = this.readChar();
        if ((delimiter != '\'') && (delimiter != '"')) {
            throw this.expectedInput("' or \"");
        }
        for (;;) {
            char ch = this.readChar();
            if (ch == delimiter) {
                return;
            } else if (ch == '&') {
                this.resolveEntity(string);
            } else {
                string.append(ch);
            }
        }
    }

    /**
     * Scans a #PCDATA element. CDATA sections and entities are resolved.
     * The next &lt; char is skipped.
     * The scanned data is appended to <code>data</code>.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>data != null</code>
     * </ul></dd></dl>
     */
    protected void scanPCData(StringBuffer data)
        throws IOException
    {
        for (;;) {
            char ch = this.readChar();
            if (ch == '<') {
                ch = this.readChar();
                if (ch == '!') {
                    this.checkCDATA(data);
                } else {
                    this.unreadChar(ch);
                    return;
                }
            } else if (ch == '&') {
                this.resolveEntity(data);
            } else {
                data.append(ch);
            }
        }
    }

    /**
     * Scans a special tag and if the tag is a CDATA section, append its
     * content to <code>buf</code>.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>buf != null</code>
     *     <li>The first &lt; has already been read.
     * </ul></dd></dl>
     */
    protected boolean checkCDATA(StringBuffer buf)
        throws IOException
    {
        char ch = this.readChar();
        if (ch != '[') {
            this.unreadChar(ch);
            this.skipSpecialTag(0);
            return false;
        } else if (! this.checkLiteral("CDATA[")) {
            this.skipSpecialTag(1); // one [ has already been read
            return false;
        } else {
            int delimiterCharsSkipped = 0;
            while (delimiterCharsSkipped < 3) {
                ch = this.readChar();
                switch (ch) {
                    case ']':
                        if (delimiterCharsSkipped < 2) {
                            delimiterCharsSkipped += 1;
                        } else {
                            buf.append(']');
                            buf.append(']');
                            delimiterCharsSkipped = 0;
                        }
                        break;
                    case '>':
                        if (delimiterCharsSkipped < 2) {
                            buf.append("]".repeat(Math.max(0, delimiterCharsSkipped)));
                            delimiterCharsSkipped = 0;
                            buf.append('>');
                        } else {
                            delimiterCharsSkipped = 3;
                        }
                        break;
                    default:
                        buf.append("]".repeat(Math.max(0, delimiterCharsSkipped)));
                        buf.append(ch);
                        delimiterCharsSkipped = 0;
                }
            }
            return true;
        }
    }

    /**
     * Skips a comment.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt;!-- has already been read.
     * </ul></dd></dl>
     */
    protected void skipComment()
        throws IOException
    {
        int dashesToRead = 2;
        while (dashesToRead > 0) {
            char ch = this.readChar();
            if (ch == '-') {
                dashesToRead -= 1;
            } else {
                dashesToRead = 2;
            }
        }
        if (this.readChar() != '>') {
            throw this.expectedInput(">");
        }
    }

    /**
     * Skips a special tag or comment.
     *
     * @param bracketLevel The number of open square brackets ([) that have
     *                     already been read.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt;! has already been read.
     *     <li><code>bracketLevel >= 0</code>
     * </ul></dd></dl>
     */
    protected void skipSpecialTag(int bracketLevel)
        throws IOException
    {
        int tagLevel = 1; // <
        char stringDelimiter = '\0';
        if (bracketLevel == 0) {
            char ch = this.readChar();
            if (ch == '[') {
                bracketLevel += 1;
            } else if (ch == '-') {
                ch = this.readChar();
                if (ch == '[') {
                    bracketLevel += 1;
                } else if (ch == ']') {
                    bracketLevel -= 1;
                } else if (ch == '-') {
                    this.skipComment();
                    return;
                }
            }
        }
        while (tagLevel > 0) {
            char ch = this.readChar();
            if (stringDelimiter == '\0') {
                if ((ch == '"') || (ch == '\'')) {
                    stringDelimiter = ch;
                } else if (bracketLevel <= 0) {
                    if (ch == '<') {
                        tagLevel += 1;
                    } else if (ch == '>') {
                        tagLevel -= 1;
                    }
                }
                if (ch == '[') {
                    bracketLevel += 1;
                } else if (ch == ']') {
                    bracketLevel -= 1;
                }
            } else {
                if (ch == stringDelimiter) {
                    stringDelimiter = '\0';
                }
            }
        }
    }

    /**
     * Scans the data for literal text.
     * Scanning stops when a character does not match or after the complete
     * text has been checked, whichever comes first.
     *
     * @param literal the literal to check.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>literal != null</code>
     * </ul></dd></dl>
     */
    protected boolean checkLiteral(String literal)
        throws IOException
    {
        int length = literal.length();
        for (int i = 0; i < length; i += 1) {
            if (this.readChar() != literal.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads a character from a reader.
     */
    protected char readChar()
        throws IOException
    {
        if (this.charReadTooMuch != '\0') {
            char ch = this.charReadTooMuch;
            this.charReadTooMuch = '\0';
            return ch;
        } else {
            int i = this.reader.read();
            if (i < 0) {
                throw this.unexpectedEndOfData();
            } else if (i == 10) {
                this.parserLineNr += 1;
                return '\n';
            } else {
                return (char) i;
            }
        }
    }

    /**
     * Scans an XML element.
     *
     * @param elt The element that will contain the result.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt; has already been read.
     *     <li><code>elt != null</code>
     * </ul></dd></dl>
     */
    protected void scanElement(XMLElement elt)
        throws IOException
    {
        StringBuffer buf = new StringBuffer();
        this.scanIdentifier(buf);
        String name = buf.toString();
        elt.setName(name);
        char ch = this.scanWhitespace();
        while ((ch != '>') && (ch != '/')) {
            buf.setLength(0);
            this.unreadChar(ch);
            this.scanIdentifier(buf);
            String key = buf.toString();
            ch = this.scanWhitespace();
            if (ch != '=') {
                throw this.expectedInput("=");
            }
            this.unreadChar(this.scanWhitespace());
            buf.setLength(0);
            this.scanString(buf);
            elt.setAttribute(key, buf);
            ch = this.scanWhitespace();
        }
        if (ch == '/') {
            ch = this.readChar();
            if (ch != '>') {
                throw this.expectedInput(">");
            }
            return;
        }
        buf.setLength(0);
        ch = this.scanWhitespace(buf);
        if (ch != '<') {
            this.unreadChar(ch);
            this.scanPCData(buf);
        } else {
            for (;;) {
                ch = this.readChar();
                if (ch == '!') {
                    if (this.checkCDATA(buf)) {
                        this.scanPCData(buf);
                        break;
                    } else {
                        ch = this.scanWhitespace(buf);
                        if (ch != '<') {
                            this.unreadChar(ch);
                            this.scanPCData(buf);
                            break;
                        }
                    }
                } else {
                    if ((ch != '/') || this.ignoreWhitespace) {
                        buf.setLength(0);
                    }
                    if (ch == '/') {
                        this.unreadChar(ch);
                    }
                    break;
                }
            }
        }
        if (buf.isEmpty()) {
            while (ch != '/') {
                if (ch == '!') {
                    ch = this.readChar();
                    if (ch != '-') {
                        throw this.expectedInput("Comment or Element");
                    }
                    ch = this.readChar();
                    if (ch != '-') {
                        throw this.expectedInput("Comment or Element");
                    }
                    this.skipComment();
                } else {
                    this.unreadChar(ch);
                    XMLElement child = this.createAnotherElement();
                    this.scanElement(child);
                    elt.addChild(child);
                }
                ch = this.scanWhitespace();
                if (ch != '<') {
                    throw this.expectedInput("<");
                }
                ch = this.readChar();
            }
            this.unreadChar(ch);
        } else {
            if (this.ignoreWhitespace) {
                elt.setContent(buf.toString().trim());
            } else {
                elt.setContent(buf.toString());
            }
        }
        ch = this.readChar();
        if (ch != '/') {
            throw this.expectedInput("/");
        }
        this.unreadChar(this.scanWhitespace());
        if (! this.checkLiteral(name)) {
            throw this.expectedInput(name);
        }
        if (this.scanWhitespace() != '>') {
            throw this.expectedInput(">");
        }
    }

    /**
     * Resolves an entity. The name of the entity is read from the reader.
     * The value of the entity is appended to <code>buf</code>.
     *
     * @param buf Where to put the entity value.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &amp; has already been read.
     *     <li><code>buf != null</code>
     * </ul></dd></dl>
     */
    protected void resolveEntity(StringBuffer buf)
        throws IOException
    {
        char ch = '\0';
        StringBuilder keyBuf = new StringBuilder();
        for (;;) {
            ch = this.readChar();
            if (ch == ';') {
                break;
            }
            keyBuf.append(ch);
        }
        String key = keyBuf.toString();
        if (key.charAt(0) == '#') {
            try {
                if (key.charAt(1) == 'x') {
                    ch = (char) Integer.parseInt(key.substring(2), 16);
                } else {
                    ch = (char) Integer.parseInt(key.substring(1), 10);
                }
            } catch (NumberFormatException e) {
                throw this.unknownEntity(key);
            }
            buf.append(ch);
        } else {
            char[] value = (char[]) this.entities.get(key);
            if (value == null) {
                throw this.unknownEntity(key);
            }
            buf.append(value);
        }
    }

    /**
     * Pushes a character back to the read-back buffer.
     *
     * @param ch The character to push back.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The read-back buffer is empty.
     *     <li><code>ch != '\0'</code>
     * </ul></dd></dl>
     */
    protected void unreadChar(char ch)
    {
        this.charReadTooMuch = ch;
    }

    /**
     * Creates a parse exception for when an invalid valueset is given to
     * a method.
     *
     * @param name The name of the entity.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     * </ul></dd></dl>
     */
    protected XMLParseException invalidValueSet(String name)
    {
        String msg = "Invalid value set (entity name = \"" + name + "\")";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when an invalid value is given to a
     * method.
     *
     * @param name  The name of the entity.
     * @param value The value of the entity.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>value != null</code>
     * </ul></dd></dl>
     */
    protected XMLParseException invalidValue(String name,
                                             String value)
    {
        String msg = "Attribute \"" + name + "\" does not contain a valid "
                   + "value (\"" + value + "\")";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when the end of the data input has been
     * reached.
     */
    protected XMLParseException unexpectedEndOfData()
    {
        String msg = "Unexpected end of data reached";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when a syntax error occured.
     *
     * @param context The context in which the error occured.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>context != null</code>
     *     <li><code>context.length() &gt; 0</code>
     * </ul></dd></dl>
     */
    protected XMLParseException syntaxError(String context)
    {
        String msg = "Syntax error while parsing " + context;
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when the next character read is not
     * the character that was expected.
     *
     * @param charSet The set of characters (in human readable form) that was
     *                expected.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>charSet != null</code>
     *     <li><code>charSet.length() &gt; 0</code>
     * </ul></dd></dl>
     */
    protected XMLParseException expectedInput(String charSet)
    {
        String msg = "Expected: " + charSet;
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when an entity could not be resolved.
     *
     * @param name The name of the entity.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name.length() &gt; 0</code>
     * </ul></dd></dl>
     */
    protected XMLParseException unknownEntity(String name)
    {
        String msg = "Unknown or invalid entity: &" + name + ";";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

}
