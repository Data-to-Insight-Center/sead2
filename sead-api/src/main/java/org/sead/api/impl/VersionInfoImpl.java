/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */

package org.sead.api.impl;

import org.bson.Document;
import org.sead.api.VersionInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * See abstract base class for documentation of the rest api. Note - path
 * annotations must match base class for documentation to be correct.
 */

@Path("/version")
public class VersionInfoImpl extends VersionInfo {
	private CacheControl control = new CacheControl();

	public VersionInfoImpl() {
		control.setNoCache(true);
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVersionInfo() {
		Document verDocument = new Document("version", "0.9.3beta1");
		return Response.ok(verDocument.toJson()).cacheControl(control).build();

	}
}
