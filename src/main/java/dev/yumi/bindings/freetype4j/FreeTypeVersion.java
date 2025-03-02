/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j;

/**
 * Represents a version of FreeType.
 *
 * @param major the major version number
 * @param minor the minor version number
 * @param patch the patch version number
 *
 * @see FreeType#getVersion()
 */
public record FreeTypeVersion(int major, int minor, int patch) {
	@Override
	public String toString() {
		return this.major + "." + this.minor + "." + this.patch;
	}
}
