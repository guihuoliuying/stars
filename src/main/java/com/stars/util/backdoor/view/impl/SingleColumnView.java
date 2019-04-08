package com.stars.util.backdoor.view.impl;

import com.stars.util.backdoor.view.AbstractView;
import com.stars.util.backdoor.view.IView;
import com.stars.util.backdoor.view.ViewLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/2/4.
 */
public class SingleColumnView extends AbstractView implements IView {

    @Override
    protected List<com.stars.util.backdoor.view.ViewLayout> initLayouts() {
        List<com.stars.util.backdoor.view.ViewLayout> list = new ArrayList<>();
        list.add(new ViewLayout("ITEM", 160));
        return list;
    }
}
