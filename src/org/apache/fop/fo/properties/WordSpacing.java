package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.WordSpacingCommon;

public class WordSpacing extends WordSpacingCommon  {
    public static final int dataTypes =
                            COMPOUND | LENGTH | MAPPED_LENGTH | INHERIT;
    public static final int traitMapping = DISAPPEARS;
    public static final int initialValueType = LENGTH_IT;
    public /*static*/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return getMappedLength(NORMAL); //normal
    }
    public static final int inherited = NO;

    public /*static*/ Numeric getMappedLength(int enum)
        throws PropertyException
    {
        if (enum != NORMAL)
            throw new PropertyException("Invalid MAPPED_LENGTH enum: "
                                        + enum);
        return Length.makeLength(PropNames.WORD_SPACING, 0d, Length.PT);
    }
}

