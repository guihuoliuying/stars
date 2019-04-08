package com.stars.util.backdoor.view.impl;

import com.stars.util.backdoor.view.AbstractView;
import com.stars.util.backdoor.view.IView;
import com.stars.util.backdoor.view.ViewLayout;

import java.util.ArrayList;
import java.util.List;

public class KeyValueView extends AbstractView implements IView {

	public KeyValueView() {
		super();
	}

	@Override
	protected List<com.stars.util.backdoor.view.ViewLayout> initLayouts() {
		List<com.stars.util.backdoor.view.ViewLayout> list = new ArrayList<>();
        list.add(new com.stars.util.backdoor.view.ViewLayout("KEY", 32));
        list.add(new ViewLayout("VALUE", 127));
		return list;
	}

}
