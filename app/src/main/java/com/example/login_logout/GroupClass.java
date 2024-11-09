package com.example.login_logout;

import java.util.Map;

public class GroupClass {
    private String groupId;
    private String name;
    private Map<String, Boolean> members; // Use Map to store member IDs

    public void Group() {}

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }
}
