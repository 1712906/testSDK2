package com.production.mylibrary.Adapter;

class CustomItem {
    int imgUrl;
    String subject;
    String detail;

    public CustomItem(int imgUrl, String subject, String detail) {
        this.imgUrl = imgUrl;
        this.subject = subject;
        this.detail = detail;
    }

    public int getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(int imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
