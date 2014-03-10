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
import java.net.URL;

/**
 * This class contains the result of a license validation.
 * 
 * @author Rick-Rainer Ludwig
 */
public class ValidationResult implements Serializable {

	private static final long serialVersionUID = 4714611770558756080L;

	private final ArtifactInformation artifactInformation;
	private final KnownLicense license;
	private final String originalLicenseName;
	private final URL originalLicenseURL;
	private final String comment;
	private final boolean valid;

	public ValidationResult(ArtifactInformation artifactInformation,
			KnownLicense license, String originalLicenseName,
			URL originalLicenseURL, String comment, boolean valid) {
		super();
		this.artifactInformation = artifactInformation;
		this.license = license;
		this.originalLicenseName = originalLicenseName;
		this.originalLicenseURL = originalLicenseURL;
		this.comment = comment;
		this.valid = valid;
	}

	public ArtifactInformation getArtifactInformation() {
		return artifactInformation;
	}

	public KnownLicense getLicense() {
		return license;
	}

	public String getOriginalLicenseName() {
		return originalLicenseName;
	}

	public URL getOriginalLicenseURL() {
		return originalLicenseURL;
	}

	public String getComment() {
		return comment;
	}

	public boolean isValid() {
		return valid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((artifactInformation == null) ? 0 : artifactInformation
						.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((license == null) ? 0 : license.hashCode());
		result = prime
				* result
				+ ((originalLicenseName == null) ? 0 : originalLicenseName
						.hashCode());
		result = prime
				* result
				+ ((originalLicenseURL == null) ? 0 : originalLicenseURL
						.toString().hashCode());
		result = prime * result + (valid ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidationResult other = (ValidationResult) obj;
		if (artifactInformation == null) {
			if (other.artifactInformation != null)
				return false;
		} else if (!artifactInformation.equals(other.artifactInformation))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (license == null) {
			if (other.license != null)
				return false;
		} else if (!license.equals(other.license))
			return false;
		if (originalLicenseName == null) {
			if (other.originalLicenseName != null)
				return false;
		} else if (!originalLicenseName.equals(other.originalLicenseName))
			return false;
		if (originalLicenseURL == null) {
			if (other.originalLicenseURL != null)
				return false;
		} else if (!originalLicenseURL.toString().equals(
				other.originalLicenseURL.toString()))
			return false;
		if (valid != other.valid)
			return false;
		return true;
	}

}
