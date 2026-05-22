package io.github.wasabithumb.dryeye.app;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@MagicConstant(valuesFromClass = ApplicationString.class)
public @interface ApplicationString {

    String WINDOW_TITLE = "DryEye UI";
    String EYE_SCHEME_LABEL = "Eye Style";
    String EYE_SCHEME_AUTO_DETECT_LABEL = "Auto-Detect";
    String BLINK_DELAY_LABEL = "Blink Delay";
    String BLINK_DURATION_LABEL = "Blink Duration";
    String FETCH_SKIN_BUTTON = "Fetch Skin";

}
