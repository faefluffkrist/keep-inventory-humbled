package com.faefluffkrist.humbled;

import java.util.List;

/** Origin belongs to each effect layer, including temporarily hidden weaker effects. */
public interface EffectOrigin {
 boolean humbled$fromSplash();
 void humbled$fromSplash(boolean value);
 List<Boolean> humbled$origins();
 void humbled$origins(List<Boolean> origins);
}
