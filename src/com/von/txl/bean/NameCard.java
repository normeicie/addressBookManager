package com.von.txl.bean;

import com.von.txl.util.StringLengthUtil;

import java.util.Objects;

public class NameCard {
    // 用户名
    @LabelName(value = "用户名", isNull = false, orderIndex = 1)
    public String userName;
    // 手机号码
    @LabelName(value = "手机号码", isNull = false, orderIndex = 2, checkRule = "\\d{11}", checkRuleDesc = "要求为11位数字")
    public String mobile;
    // 电话
    @LabelName(value = "电话", orderIndex = 3, checkRule = "\\d{7,8}", checkRuleDesc = "要求为7~8位数字")
    public String phone;
    // 住址
    @LabelName(value = "住址", orderIndex = 4)
    public String address;
    // 即时消息
    @LabelName(value = "即时消息", orderIndex = 5)
    public String imessage;
    // 公司
    @LabelName(value = "公司", orderIndex = 6)
    public String company;
    // 职位
    @LabelName(value = "邮政编码", orderIndex = 7, checkRule = "\\d{6}", checkRuleDesc = "要求为6位数字")
    public String posts;
    // 昵称
    @LabelName(value = "昵称", orderIndex = 8)
    public String nickName;
    // 性别
    @LabelName(value = "性别", orderIndex = 9, checkRule = "男|女", checkRuleDesc = "要求为[男]或者[女]")
    public String gender;
    // 邮件地址
    @LabelName(value = "邮件地址", orderIndex = 10, checkRule = "[a-zA-Z0-9_]+@[a-zA-Z0-9]+\\.[a-zA-Z]+", checkRuleDesc = "要求为正确的邮箱格式。例如：abc@123.com")
    public String email;
    // 生日
    @LabelName(value = "生日", orderIndex = 11, checkRule = "([1][9]|[2][0])[0-9]{2}\\-([0][1-9]|[1][012])\\-([0][1-9]|[12][0-9]|[3][01])", checkRuleDesc = "要求为正确的日期格式，单数日期请补0。例如：2023-09-09")
    public String birthday;
    // 网站
    @LabelName(value = "网站", orderIndex = 12)
    public String site;
    // 关系
    @LabelName(value = "关系", orderIndex = 13)
    public String relationship;
    // 备注
    @LabelName(value = "备注", orderIndex = 14)
    public String remark;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setImessage(String imessage) {
        this.imessage = imessage;
    }

    public String getImessage() {
        return imessage;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public void setPosts(String posts) {
        this.posts = posts;
    }

    public String getPosts() {
        return posts;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getSite() {
        return site;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameCard nameCard = (NameCard) o;
        return userName.equals(nameCard.userName) && mobile.equals(nameCard.mobile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, mobile);
    }

    @Override
    public String toString() {
        return StringLengthUtil.processNull(userName) + "\u0001" +
                StringLengthUtil.processNull(mobile) + "\u0001" +
                StringLengthUtil.processNull(phone) + "\u0001" +
                StringLengthUtil.processNull(address) + "\u0001" +
                StringLengthUtil.processNull(imessage) + "\u0001" +
                StringLengthUtil.processNull(company) + "\u0001" +
                StringLengthUtil.processNull(posts) + "\u0001" +
                StringLengthUtil.processNull(nickName) + "\u0001" +
                StringLengthUtil.processNull(gender) + "\u0001" +
                StringLengthUtil.processNull(email) + "\u0001" +
                StringLengthUtil.processNull(birthday) + "\u0001" +
                StringLengthUtil.processNull(site) + "\u0001" +
                StringLengthUtil.processNull(relationship) + "\u0001" +
                StringLengthUtil.processNull(remark);
    }
}