package com.bupt.app.multivrWAP.model;

public class StatisticsWAP {

    private Integer id;


    private Byte pagetype;


    private String vrid;


    private Byte linkid;


    private Float vrposav;

 
    private Integer pvnum;

  
    private Integer clicknum;


    private Integer endclicknum;


    private Integer hour;


    private String jhid;


    private String date;


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public Byte getPagetype() {
        return pagetype;
    }


    public void setPagetype(Byte pagetype) {
        this.pagetype = pagetype;
    }

 
    public String getVrid() {
        return vrid;
    }


    public void setVrid(String vrid) {
        this.vrid = vrid == null ? null : vrid.trim();
    }


    public Byte getLinkid() {
        return linkid;
    }


    public void setLinkid(Byte linkid) {
        this.linkid = linkid;
    }


    public Float getVrposav() {
        return vrposav;
    }


    public void setVrposav(Float vrposav) {
        this.vrposav = vrposav;
    }


    public Integer getPvnum() {
        return pvnum;
    }

 
    public void setPvnum(Integer pvnum) {
        this.pvnum = pvnum;
    }


    public Integer getClicknum() {
        return clicknum;
    }


    public void setClicknum(Integer clicknum) {
        this.clicknum = clicknum;
    }


    public Integer getEndclicknum() {
        return endclicknum;
    }

    public void setEndclicknum(Integer endclicknum) {
        this.endclicknum = endclicknum;
    }


    public Integer getHour() {
        return hour;
    }


    public void setHour(Integer hour) {
        this.hour = hour;
    }


    public String getJhid() {
        return jhid;
    }


    public void setJhid(String jhid) {
        this.jhid = jhid;
    }


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date == null ? null : date.trim();
    }
}