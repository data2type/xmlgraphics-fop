package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class WritingMode extends Property  {
    public static final int dataTypes = ENUM | INHERIT;
    public static final int traitMapping = NEW_TRAIT;
    public static final int initialValueType = ENUM_IT;
    public static final int LR_TB = 1;
    public static final int RL_TB = 2;
    public static final int TB_RL = 3;
    public static final int LR = 4;
    public static final int RL = 5;
    public static final int TB = 6;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.WRITING_MODE, LR_TB);
    }
    public static final int inherited = COMPUTED;

    private static final String[] rwEnums = {
        null
        ,"lr-tb"
        ,"rl-tb"
        ,"tb-rl"
        ,"lr"
        ,"rl"
        ,"tb"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(rwEnums.length);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }
    public int getEnumIndex(String enum) {
        return ((Integer)(rwEnumHash.get(enum))).intValue();
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

