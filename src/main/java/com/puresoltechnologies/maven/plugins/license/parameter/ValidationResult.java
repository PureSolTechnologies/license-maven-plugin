/*
 * Copyright 2013 PureSol Technologies
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
 */
package com.puresoltechnologies.maven.plugins.license.parameter;

import java.io.Serializable;

/**
 * This class contains the result of a license validation.
 * 
 * @author Rick-Rainer Ludwig
 */
public class ValidationResult implements Serializable {

	private static final long serialVersionUID = 4714611770558756080L;

	private final ArtifactInformation artifactInformation;
	private final KnownLicense license;
	private final ValidLicense originalLicense;
	private final String comment;
	private final boolean valid;

	public ValidationResult(ArtifactInformation artifactInformation,
			KnownLicense license, ValidLicense originalLicense, String comment,
			boolean valid) {
		super();
		this.artifactInformation = artifactInformation;
		this.license = license;
		this.originalLicense = originalLicense;
		this.comment = comment;
		this.valid = valid;
	}

	public ArtifactInformation getArtifactInformation() {
		return artifactInformation;
	}

	public KnownLicense getLicense() {
		return license;
	}

	public ValidLicense getOriginalLicense() {
		return originalLicense;
	}

	public String getComment() {
		return comment;
	}

	public boolean isValid() {
		return valid;
	}

}
