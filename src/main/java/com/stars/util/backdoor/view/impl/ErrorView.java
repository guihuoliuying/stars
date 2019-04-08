package com.stars.util.backdoor.view.impl;

import com.stars.util.backdoor.view.AbstractView;
import com.stars.util.backdoor.view.IView;
import com.stars.util.backdoor.view.ViewLayout;

import java.util.ArrayList;
import java.util.List;

public class ErrorView extends AbstractView implements IView {

	public ErrorView() {
		super();
	}

	@Override
	protected List<com.stars.util.backdoor.view.ViewLayout> initLayouts() {
		List<com.stars.util.backdoor.view.ViewLayout> list = new ArrayList<com.stars.util.backdoor.view.ViewLayout>();
        list.add(new ViewLayout("ERROR INFO", 160));
		return list;
	}

}
