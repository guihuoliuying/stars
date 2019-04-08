package com.stars.server.proxy.weaknetwork;

import com.stars.util.backdoor.view.AbstractView;
import com.stars.util.backdoor.view.IView;
import com.stars.util.backdoor.view.ViewLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Garwah on 2015/10/27.
 */
public class SessionView extends AbstractView implements IView {

    public SessionView() {
        super();
    }

    @Override
    protected List<com.stars.util.backdoor.view.ViewLayout> initLayouts() {
        List<com.stars.util.backdoor.view.ViewLayout> list = new ArrayList<>();
        list.add(new com.stars.util.backdoor.view.ViewLayout("ID", 32));
        list.add(new com.stars.util.backdoor.view.ViewLayout("LOCAL ADDRESS", 21));
        list.add(new com.stars.util.backdoor.view.ViewLayout("REMOTE ADDRESS", 21));
        list.add(new com.stars.util.backdoor.view.ViewLayout("AUTO READ", 9));
        list.add(new com.stars.util.backdoor.view.ViewLayout("DELAY", 16));
        list.add(new ViewLayout("READ COUNT", 56));
        return list;
    }
}
