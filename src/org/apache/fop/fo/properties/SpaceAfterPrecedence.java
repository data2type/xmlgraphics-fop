package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.PrecedenceCommon;

public class SpaceAfterPrecedence extends PrecedenceCommon {
    public static final int dataTypes = INTEGER | ENUM;
    public static final int traitMapping = FORMATTING;
    public static final int initialValueType = INTEGER_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new IntegerType(PropNames.SPACE_AFTER_PRECEDENCE, 0);
    }

    public static final int inherited = NO;

}

