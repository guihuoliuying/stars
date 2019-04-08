package com.stars.util.backdoor.view.impl;


import com.stars.util.backdoor.view.AbstractView;
import com.stars.util.backdoor.view.IView;
import com.stars.util.backdoor.view.ViewLayout;

import java.util.ArrayList;
import java.util.List;

public class NoTitleView extends AbstractView implements IView {
	
	public NoTitleView() {
		super();
	}

	@Override
	protected List<com.stars.util.backdoor.view.ViewLayout> initLayouts() {
		List<com.stars.util.backdoor.view.ViewLayout> list = new ArrayList<com.stars.util.backdoor.view.ViewLayout>();
		com.stars.util.backdoor.view.ViewLayout layout = new ViewLayout("", 160);
		list.add(layout);
		return list;
	}

}
