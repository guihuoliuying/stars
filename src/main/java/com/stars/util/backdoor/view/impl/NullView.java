package com.stars.util.backdoor.view.impl;

import com.stars.util.backdoor.view.AbstractView;
import com.stars.util.backdoor.view.IView;
import com.stars.util.backdoor.view.ViewLayout;

import java.util.ArrayList;
import java.util.List;

public class NullView extends AbstractView implements IView {

	public NullView() {
		super();
	}

	@Override
	protected List<ViewLayout> initLayouts() {
		return new ArrayList<>();
	}
	
	@Override
	public String getDisplayedView() {
		return "";
	}
	
	@Override
	public String getDisplayedTitle() {
		return "";
	}
	
	@Override 
	public String getDisplayedSeparator() {
		return "";
	}
	
	@Override
	public String getDisplayedValue() {
		return "";
	}

}
