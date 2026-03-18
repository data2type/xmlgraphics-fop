package org.apache.fop.pdf;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PDFStructElemTestCase {
    @Test
    public void testScopeNotOverwritten() {
        PDFDocument doc = new PDFDocument("FOP-3167");
        PDFStructElem th = new PDFStructElem(doc.getRoot(), StandardStructureTypes.Table.TH);
        th.setDocument(doc);

        StandardStructureAttributes.Table.Scope.addScopeAttribute(th, StandardStructureAttributes.Table.Scope.COLUMN);

        th.setTableAttributeColSpan(2);

        th.attachAttributes();

        PDFDictionary dict = (PDFDictionary) th.get("A");

        assertEquals("/Column", dict.get("Scope").toString());
        assertEquals("2", dict.get("ColSpan").toString());
        assertEquals("/Table", dict.get("O").toString());
    }
}