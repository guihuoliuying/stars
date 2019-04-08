package com.stars.services.family;

/**
 * 家族授权信息
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyAuth {

    private long familyId; // 家族id
    private String familyName; // 家族名字
    private int familyLevel; // 家族等级
    private long roleId; // 玩家id
    private String roleName; // 玩家名字
    private FamilyPost post; // 家族职位

    private int hashCode;

    public FamilyAuth(long familyId, String familyName, int familyLevel, long roleId, String roleName, FamilyPost post) {
        setFamilyId(familyId);
        setFamilyName(familyName);
        setFamilyLevel(familyLevel);
        setRoleId(roleId);
        setRoleName(roleName);
        setPost(post);
    }

    public FamilyAuth(FamilyAuth auth) {
        this(auth.getFamilyId(), auth.getFamilyName(), auth.getFamilyLevel(), auth.getRoleId(), auth.getRoleName(), auth.getPost());
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
        this.hashCode = new Long(familyId).hashCode();
    }

    public long getRoleId() {
        return roleId;
    }

    private void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getFamilyName() {
        return familyName;
    }

    private void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public int getFamilyLevel() {
        return familyLevel;
    }

    private void setFamilyLevel(int familyLevel) {
        this.familyLevel = familyLevel;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public FamilyPost getPost() {
        return post;
    }

    private void setPost(FamilyPost post) {
        this.post = post;
    }

    public boolean hasFamily() {
        return familyId > 0;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "FamilyAuth{" +
                "familyId=" + familyId +
                ", roleId=" + roleId +
                ", familyLevel=" + familyLevel +
                ", post=" + post.getClass().getSimpleName() +
                '}';
    }
}
