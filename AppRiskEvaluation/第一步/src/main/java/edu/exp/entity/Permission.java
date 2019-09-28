package edu.exp.entity;

/**
 * 权限
 */
public class Permission {
	/**
	 * 权限的id
	 */
	private String id;
	/**
	 * App 的名称
	 */
	private String docId;
	/**
	 * App 的权限
	 */
	private String permission;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
}
