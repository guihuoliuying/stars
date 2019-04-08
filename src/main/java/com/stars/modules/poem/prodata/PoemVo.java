package com.stars.modules.poem.prodata;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class PoemVo implements Comparable<PoemVo>{
    private int poemsId;
    private String icon;
    private int worldId;
    private String image;
    private String showItem;
    private String name;
    private String showtitle;
    private String showdesc;
    private String showdescwin;
    
    public int getPoemsId() {
        return poemsId;
    }

    public void setPoemsId(int value) {
        this.poemsId = value;
    }
    
    public String getIcon() {
        return icon;
    }

    public void setIcon(String value) {
        this.icon = value;
    }
    
    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int value) {
        this.worldId = value;
    }
    
    public String getImage() {
        return image;
    }

    public void setImage(String value) {
        this.image = value;
    }
    
    public String getShowItem() {
        return showItem;
    }

    public void setShowItem(String value) {
        this.showItem = value;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }
    
    public String getShowTitle() {
        return showtitle;
    }

    public void setShowTitle(String value) {
        this.showtitle = value;
    }
    
    public String getShowDesc() {
        return showdesc;
    }

    public void setShowDesc(String value) {
        this.showdesc = value;
    }
    
    public String getShowDescWin() {
        return showdescwin;
    }

    public void setShowDescWin(String value) {
        this.showdescwin = value;
    }
    
    /**
     * 按活动worldId从小到大排
     */
	@Override
	public int compareTo(PoemVo o) {
		if (this.getWorldId() < o.getWorldId()) {
			return -1;
		}else if (this.getWorldId() > o.getWorldId()) {
			return 1;
		}else{
			return 0;
		}
	}
}
