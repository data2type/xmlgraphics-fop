/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.accessibility.fo;

import java.util.*;

import javax.xml.XMLConstants;

import org.apache.fop.accessibility.Accessibility;
import org.apache.fop.fo.flow.table.*;
import org.apache.fop.fo.properties.ListProperty;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.tools.ant.util.StringUtils;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.fo.flow.AbstractRetrieveMarker;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.util.LanguageTags;
import org.apache.fop.util.XMLUtil;

/**
 * A bridge between {@link FOEventHandler} and {@link StructureTreeEventHandler}.
 */
class StructureTreeEventTrigger extends FOEventHandler {

    private StructureTreeEventHandler structureTreeEventHandler;

    private LayoutMasterSet layoutMasterSet;

    private Stack<Table> tables = new Stack<Table>();

    private Stack<Boolean> inTableHeader = new Stack<Boolean>();

    private Stack<Locale> locales = new Stack<Locale>();

    private final Map<AbstractRetrieveMarker, State> states = new HashMap<AbstractRetrieveMarker, State>();

    private final boolean autoPDFTag;
    private final Properties roleProperties = new Properties();


    private static final class State {

        private final Stack<Table> tables;

        private final Stack<Boolean> inTableHeader;

        private final Stack<Locale> locales;

        @SuppressWarnings("unchecked")
        State(StructureTreeEventTrigger o) {
            this.tables = (Stack<Table>) o.tables.clone();
            this.inTableHeader = (Stack<Boolean>) o.inTableHeader.clone();
            this.locales = (Stack<Locale>) o.locales.clone();
        }

    }

    public StructureTreeEventTrigger
            (StructureTreeEventHandler structureTreeEventHandler,final boolean autoPDFTag,
             final Properties roleProperties) {

        this.structureTreeEventHandler = structureTreeEventHandler;
        this.autoPDFTag = autoPDFTag;
        this.roleProperties.putAll(roleProperties);
    }

    @Override
    public void startRoot(Root root) {
        locales.push(root.getLocale());
    }

    @Override
    public void endRoot(Root root) {
        locales.pop();
    }

    @Override
    public void startPageSequence(PageSequence pageSeq) {
        if (layoutMasterSet == null) {
            layoutMasterSet = pageSeq.getRoot().getLayoutMasterSet();
        }
        Locale locale = pageSeq.getLocale();
        if (locale != null) {
            locales.push(locale);
        } else {
            locales.push(locales.peek());
        }
        String role = pageSeq.getCommonAccessibility().getRole();
        structureTreeEventHandler.startPageSequence(locale, role);
    }

    @Override
    public void endPageSequence(PageSequence pageSeq) {
        structureTreeEventHandler.endPageSequence();
        locales.pop();
    }

    @Override
    public void startPageNumber(PageNumber pagenum) {
        startElementWithID(pagenum);
    }

    @Override
    public void endPageNumber(PageNumber pagenum) {
        endElement(pagenum);
    }

    @Override
    public void startPageNumberCitation(PageNumberCitation pageCite) {
        startElementWithID(pageCite);
    }

    @Override
    public void endPageNumberCitation(PageNumberCitation pageCite) {
        endElement(pageCite);
    }

    @Override
    public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
        startElementWithID(pageLast);
    }

    @Override
    public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
        endElement(pageLast);
    }

    @Override
    public void startStatic(StaticContent staticContent) {
        AttributesImpl flowName = createFlowNameAttribute(staticContent.getFlowName());
        startElement(staticContent, flowName);
    }

    private AttributesImpl createFlowNameAttribute(String flowName) {
        String regionName = layoutMasterSet.getDefaultRegionNameFor(flowName);
        AttributesImpl attribute = new AttributesImpl();
        addNoNamespaceAttribute(attribute, Flow.FLOW_NAME, regionName);
        return attribute;
    }

    @Override
    public void endStatic(StaticContent staticContent) {
        endElement(staticContent);
    }

    @Override
    public void startFlow(Flow fl) {
        AttributesImpl flowName = createFlowNameAttribute(fl.getFlowName());
        startElement(fl, flowName);
    }

    @Override
    public void endFlow(Flow fl) {
        endElement(fl);
    }

    @Override
    public void startBlock(Block bl) {

        CommonHyphenation hyphProperties = bl.getCommonHyphenation();
        AttributesImpl attributes = createLangAttribute(hyphProperties);
        startElement(bl, attributes);
    }

    private AttributesImpl createLangAttribute(CommonHyphenation hyphProperties) {
        Locale locale = hyphProperties.getLocale();
        AttributesImpl attributes = new AttributesImpl();
        if (locale == null || locale.equals(locales.peek())) {
            locales.push(locales.peek());
        } else {
            locales.push(locale);
            addAttribute(attributes, XMLConstants.XML_NS_URI, "lang", "xml",
                    LanguageTags.toLanguageTag(locale));
        }
        return attributes;
    }

    @Override
    public void endBlock(Block bl) {
        endElement(bl);
        locales.pop();
    }

    @Override
    public void startBlockContainer(BlockContainer blc) {
        startElement(blc);
    }

    @Override
    public void endBlockContainer(BlockContainer blc) {
        endElement(blc);
    }

    @Override
    public void startInline(Inline inl) {
        startElement(inl);
    }

    @Override
    public void endInline(Inline inl) {
        endElement(inl);
    }

    @Override
    public void startTable(Table tbl) {
        tables.push(tbl);
        startElement(tbl);
    }

    @Override
    public void endTable(Table tbl) {
        endElement(tbl);
        tables.pop();
    }

    @Override
    public void startHeader(TableHeader header) {
        inTableHeader.push(Boolean.TRUE);
        startElement(header);
    }

    @Override
    public void endHeader(TableHeader header) {
        endElement(header);
        inTableHeader.pop();
    }

    @Override
    public void startFooter(TableFooter footer) {
        // TODO Shouldn't it be true?
        inTableHeader.push(Boolean.FALSE);
        startElement(footer);
    }

    @Override
    public void endFooter(TableFooter footer) {
        endElement(footer);
        inTableHeader.pop();
    }

    @Override
    public void startBody(TableBody body) {
        inTableHeader.push(Boolean.FALSE);
        startElement(body);
    }

    @Override
    public void endBody(TableBody body) {
        endElement(body);
        inTableHeader.pop();
    }

    @Override
    public void startRow(TableRow tr) {
        startElement(tr);
    }

    @Override
    public void endRow(TableRow tr) {
        endElement(tr);
    }

    @Override
    public void startCell(TableCell tc) {
        AttributesImpl attributes = new AttributesImpl();
        addSpanAttribute(attributes, "number-columns-spanned", tc.getNumberColumnsSpanned());
        addSpanAttribute(attributes, "number-rows-spanned", tc.getNumberRowsSpanned());
        boolean rowHeader = inTableHeader.peek();
        boolean columnHeader = tables.peek().getColumn(tc.getColumnNumber() - 1).isHeader();
        if (rowHeader || columnHeader) {
            final String th = "TH";
            String role = tc.getCommonAccessibility().getRole();
            /* Do not override a custom role */
            if (role == null) {
                role = th;
                addNoNamespaceAttribute(attributes, "role", th);
            }
            if (role.equals(th)) {
                if (columnHeader) {
                    String scope = rowHeader ? "Both" : "Row";
                    addAttribute(attributes, InternalElementMapping.URI, InternalElementMapping.SCOPE,
                            InternalElementMapping.STANDARD_PREFIX, scope);
                }
            }
        }
        startElement(tc, attributes);
    }

    private void addSpanAttribute(AttributesImpl attributes, String attributeName, int span) {
        if (span > 1) {
            addNoNamespaceAttribute(attributes, attributeName, Integer.toString(span));
        }
    }

    @Override
    public void endCell(TableCell tc) {
        endElement(tc);
    }

    @Override
    public void startList(ListBlock lb) {
        startElement(lb);
    }

    @Override
    public void endList(ListBlock lb) {
        endElement(lb);
    }

    @Override
    public void startListItem(ListItem li) {
        startElement(li);
    }

    @Override
    public void endListItem(ListItem li) {
        endElement(li);
    }

    @Override
    public void startListLabel(ListItemLabel listItemLabel) {
        startElement(listItemLabel);
    }

    @Override
    public void endListLabel(ListItemLabel listItemLabel) {
        endElement(listItemLabel);
    }

    @Override
    public void startListBody(ListItemBody listItemBody) {
        startElement(listItemBody);
    }

    @Override
    public void endListBody(ListItemBody listItemBody) {
        endElement(listItemBody);
    }

    @Override
    public void startLink(BasicLink basicLink) {
        startElementWithIDAndAltText(basicLink, basicLink.getAltText());
    }

    @Override
    public void endLink(BasicLink basicLink) {
        endElement(basicLink);
    }

    @Override
    public void image(ExternalGraphic eg) {
        startElementWithIDAndAltText(eg, eg.getAltText());
        endElement(eg);
    }

    @Override
    public void startInstreamForeignObject(InstreamForeignObject ifo) {
        startElementWithIDAndAltText(ifo, ifo.getAltText());
    }

    @Override
    public void endInstreamForeignObject(InstreamForeignObject ifo) {
        endElement(ifo);
    }

    @Override
    public void startFootnote(Footnote footnote) {
        startElement(footnote);
    }

    @Override
    public void endFootnote(Footnote footnote) {
        endElement(footnote);
    }

    @Override
    public void startFootnoteBody(FootnoteBody body) {
        StructureTreeElement structElem = startElement(body);
        if(structElem instanceof PDFStructElem && body.getId() != null){
            PDFStructElem pdfStructElem = (PDFStructElem) structElem;
            pdfStructElem.put("/ID", body.getId());
        }
    }

    @Override
    public void endFootnoteBody(FootnoteBody body) {
        endElement(body);
    }

    @Override
    public void startWrapper(Wrapper wrapper) {
        startElement(wrapper);
    }

    @Override
    public void endWrapper(Wrapper wrapper) {
        endElement(wrapper);
    }

    @Override
    public void startRetrieveMarker(RetrieveMarker retrieveMarker) {
        startElementWithID(retrieveMarker);
        saveState(retrieveMarker);
    }

    void saveState(AbstractRetrieveMarker retrieveMarker) {
        states.put(retrieveMarker, new State(this));
    }

    @Override
    public void endRetrieveMarker(RetrieveMarker retrieveMarker) {
        endElement(retrieveMarker);
    }

    @Override
    public void restoreState(RetrieveMarker retrieveMarker) {
        restoreRetrieveMarkerState(retrieveMarker);
    }

    @SuppressWarnings("unchecked")
    private void restoreRetrieveMarkerState(AbstractRetrieveMarker retrieveMarker) {
        State state = states.get(retrieveMarker);
        tables = (Stack<Table>) state.tables.clone();
        inTableHeader = (Stack<Boolean>) state.inTableHeader.clone();
        locales = (Stack<Locale>) state.locales.clone();
    }

    @Override
    public void startRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
        startElementWithID(retrieveTableMarker);
        saveState(retrieveTableMarker);
    }

    @Override
    public void endRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
        endElement(retrieveTableMarker);
    }

    @Override
    public void restoreState(RetrieveTableMarker retrieveTableMarker) {
        restoreRetrieveMarkerState(retrieveTableMarker);
    }

    @Override
    public void character(Character c) {
        AttributesImpl attributes = createLangAttribute(c.getCommonHyphenation());
        startElementWithID(c, attributes);
        endElement(c);
        locales.pop();
    }

    @Override
    public void characters(FOText foText) {
        startElementWithID(foText);
        endElement(foText);
    }

    private LinkedList<Boolean> tagged = new LinkedList<Boolean>();
    private FONode lastParagraph = null;
    private String lastParagraphsLocalName = null;
    private AttributesImpl lastParagraphsAttributes = null;

    private StructureTreeElement startElement(FONode node) {

        AttributesImpl attributes = new AttributesImpl();
        if (node instanceof Inline) {
            Inline in = (Inline)node;
            if (!in.getAbbreviation().equals("")) {
                addAttribute(attributes, ExtensionElementMapping.URI, "abbreviation",
                        ExtensionElementMapping.STANDARD_PREFIX, in.getAbbreviation());
            }
        }

        return startElement(node, attributes);
    }

    private void startElementWithID(FONode node) {
        startElementWithID(node, new AttributesImpl());
    }

    private void startElementWithID(FONode node, AttributesImpl attributes) {

        if ( !autoPDFTag ) {
            if ( lastParagraph != null ) {
                StructureTreeElement element = structureTreeEventHandler.startNode(lastParagraphsLocalName, lastParagraphsAttributes,
                        node.getParent().getStructureTreeElement());
                addTaggingAttributes(element, lastParagraphsAttributes);
                tagged.set(0,Boolean.TRUE);

            }
        }

        String localName = node.getLocalName();
        if (node instanceof CommonAccessibilityHolder) {
            addRole((CommonAccessibilityHolder) node, attributes);
        }

        StructureTreeElement structureTreeElement = structureTreeEventHandler.startReferencedNode(localName, attributes,
                node.getParent().getStructureTreeElement());

        addTaggingAttributes(structureTreeElement, attributes);

        node.setStructureTreeElement(structureTreeElement);

        tagged.addFirst(Boolean.TRUE);
    }

    private void startElementWithIDAndAltText(FObj node, String altText) {
        AttributesImpl attributes = new AttributesImpl();
        String localName = node.getLocalName();
        addRole((CommonAccessibilityHolder)node, attributes);
        addAttribute(attributes, ExtensionElementMapping.URI, "alt-text",
                ExtensionElementMapping.STANDARD_PREFIX, altText);
        convertRoles(attributes);
        node.setStructureTreeElement(
                structureTreeEventHandler.startImageNode(localName, attributes,
                        node.getParent().getStructureTreeElement()));

        addTaggingAttributes(node.getStructureTreeElement(), attributes);

        tagged.addFirst(Boolean.TRUE);
    }

    private StructureTreeElement startElement(FONode node, AttributesImpl attributes) {

        final String localName = node.getLocalName();
        final String roleValue = roleProperties.getProperty(localName);

        if (node instanceof CommonAccessibilityHolder) {
            addRole((CommonAccessibilityHolder) node, attributes);
        }

        if ( localName != null && roleValue != null) {
            String role = attributes.getValue("role");
            role = role != null ? role.trim() : null;
            if("".equals(role)){
                removeNoNamespaceAttribute(attributes, "role");
            } else if (role == null) {
                addNoNamespaceAttribute(attributes, "role", roleValue);
            }
        }

        convertRoles(attributes);

        lastParagraph = null;
        lastParagraphsLocalName = null;
        lastParagraphsAttributes = null;

        if ( !autoPDFTag  ) {
            String role = attributes.getValue("role");
            if (role == null) {
                lastParagraph = node;
                lastParagraphsLocalName = localName;
                lastParagraphsAttributes = attributes;
                tagged.addFirst(Boolean.FALSE);
                return null;
            }
        }

        StructureTreeElement element = structureTreeEventHandler.startNode(localName, attributes,
                node.getParent().getStructureTreeElement());
        addTaggingAttributes(element, attributes);
        tagged.addFirst(Boolean.TRUE);

        return element;
    }

    private void addNoNamespaceAttribute(AttributesImpl attributes, String name, String value) {

        attributes.addAttribute("", name, name, XMLUtil.CDATA, value);
    }

    private void removeNoNamespaceAttribute(AttributesImpl attributes, String name) {
        attributes.removeAttribute(attributes.getIndex("", name));
    }

    private void convertRoles(AttributesImpl attributes){
        String role = attributes.getValue("role");
        if(role == null)
            return;
        if(!role.contains("?"))
            return;
        String eRole = role.substring(0, role.indexOf("?"));
        String roleAttr = role.substring(role.indexOf("?") + 1);
        removeNoNamespaceAttribute(attributes, "role");
        addNoNamespaceAttribute(attributes, "role", eRole);

        String[] roleAttrs = parseRoleData(roleAttr);

        for (String rAttr:
             roleAttrs) {
            if(rAttr.contains("=")){
                String name = rAttr.substring(0, rAttr.indexOf('='));
                String value = rAttr.substring(rAttr.indexOf('=') + 1);
                addAttribute(attributes, Accessibility.ACCESSIBILITY_NSURI, name, ExtensionElementMapping.STANDARD_PREFIX, value);
            }
        }

    }

    private static String[] parseRoleData(String data){
        ArrayList<String> result = new ArrayList<>();
        StringBuffer buffer = new StringBuffer();
        char[] chars = data.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]){
                case '\\':
                    if(chars.length == i+1)
                        throw new IllegalArgumentException("Bad escaping in role data " + data);
                    buffer.append(chars[i+1]);
                    i++;
                    break;
                case ';':
                    result.add(buffer.toString());
                    buffer = new StringBuffer();
                    break;
                default:
                    buffer.append(chars[i]);
            }
        }
        if(buffer.length() > 0){
            result.add(buffer.toString());
        }
        return result.toArray(new String[result.size()]);
    }

    private void addTaggingAttributes(StructureTreeElement element, AttributesImpl attributes){
        if(element instanceof PDFStructElem){
            PDFStructElem pdfStructElem = (PDFStructElem) element;
            for (int i = 0; i < attributes.getLength(); i++) {
                if(Accessibility.ACCESSIBILITY_NSURI.equals(attributes.getURI(i))){
                    pdfStructElem.put(attributes.getLocalName(i), attributes.getValue(i));
                }
            }
        }
    }

    private void addAttribute(AttributesImpl attributes,
            String namespace, String localName, String prefix, String value) {
        assert namespace.length() > 0 && prefix.length() > 0;
        String qualifiedName = prefix + ":" + localName;
        attributes.addAttribute(namespace, localName, qualifiedName, XMLUtil.CDATA, value);
    }

    private void addRole(CommonAccessibilityHolder node, AttributesImpl attributes) {

        String role = node.getCommonAccessibility().getRole();
        if (role != null) {
            addNoNamespaceAttribute(attributes, "role", role);
        }
    }

    private void endElement(FONode node) {

        if (tagged.getFirst().booleanValue()) {
            String localName = node.getLocalName();
            structureTreeEventHandler.endNode(localName);
        }

        lastParagraph = null;
        lastParagraphsLocalName = null;
        lastParagraphsAttributes = null;

        tagged.removeFirst();
    }

}
