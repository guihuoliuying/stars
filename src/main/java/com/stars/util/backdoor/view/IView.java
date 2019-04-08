package com.stars.util.backdoor.view;

import com.stars.util.backdoor.result.BackdoorResult;

public interface IView {

    String getDisplayedView();
	String getDisplayedTitle();
	String getDisplayedSeparator();
	String getDisplayedValue();

    void setResult(BackdoorResult result);
    int size();
	
}
