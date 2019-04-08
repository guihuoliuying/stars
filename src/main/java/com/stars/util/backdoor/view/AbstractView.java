package com.stars.util.backdoor.view;

import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractView implements IView {

    protected List<com.stars.util.backdoor.view.ViewLayout> layouts;
    protected com.stars.util.backdoor.result.BackdoorResult result;

    public AbstractView() {
        this.layouts = initLayouts();
    }

    protected abstract List<com.stars.util.backdoor.view.ViewLayout> initLayouts();

    public String getDisplayedView() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayedTitle()).append("\n");
        sb.append(getDisplayedSeparator()).append("\n");
        sb.append(getDisplayedValue()).append("\n");
        return sb.toString();
    }

    public String getDisplayedTitle() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.layouts.size(); i++) {
            com.stars.util.backdoor.view.ViewLayout layout = this.layouts.get(i);
            sb.append(layout.getTitleName());
            if (i != this.layouts.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public String getDisplayedSeparator() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.layouts.size(); i++) {
            com.stars.util.backdoor.view.ViewLayout layout = this.layouts.get(i);
            sb.append(layout.getSeparator());
            if (i != this.layouts.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public String getDisplayedValue() {
        StringBuilder sb = new StringBuilder();
        Iterator<com.stars.util.backdoor.result.BackdoorRow> itor = this.result.rowIterator();
        while (itor.hasNext()) {
            BackdoorRow row = itor.next();
            for (int i = 0; i < this.layouts.size(); i++) {
                ViewLayout layout = this.layouts.get(i);
                BackdoorCell cell = row.getCell(i);
                sb.append(layout.getDisplayedValue(cell));
                if (i != this.layouts.size() - 1) {
                    sb.append(" ");
                }
            }
            if (itor.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public int size() {
        return layouts.size();
    }

    @Override
    public void setResult(BackdoorResult result) {
        this.result = result;
    }
}
