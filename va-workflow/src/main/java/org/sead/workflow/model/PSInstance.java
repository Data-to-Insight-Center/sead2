/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sead.workflow.model;


public class PSInstance {
    private int id;
	public void setId(int id) {
		this.id = id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setRemoteAPI(String remoteAPI) {
		this.remoteAPI = remoteAPI;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String url;
	private String remoteAPI;
	private String title;
	private String type;
    private String user;
    private String password;

	public PSInstance() {}

	public int getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getRemoteAPI() {
		return remoteAPI;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
