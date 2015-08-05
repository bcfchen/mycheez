package com.mycheez.util;

import com.mycheez.enums.CheeseCountChangeType;

public class CheeseCountChangeTypeHelper {
    public static CheeseCountChangeType getChangeType(Integer oldCheeseCount, Integer newCheeseCount){
        if (oldCheeseCount == null){
            return null;
        } else if (newCheeseCount >= oldCheeseCount){
            return CheeseCountChangeType.STEAL;
        } else if (newCheeseCount < oldCheeseCount){
            return CheeseCountChangeType.STOLEN;
        } else {
            return CheeseCountChangeType.NO_CHANGE;
        }
    }
}
