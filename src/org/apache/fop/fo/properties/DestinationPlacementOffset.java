package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class DestinationPlacementOffset extends Property  {
    public static final int dataTypes = LENGTH;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = LENGTH_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return Length.makeLength
		    (PropNames.DESTINATION_PLACEMENT_OFFSET, 0.0d, Length.PT);
    }
    public static final int inherited = NO;
}

