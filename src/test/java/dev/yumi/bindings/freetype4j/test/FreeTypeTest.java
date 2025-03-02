/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j.test;

import dev.yumi.bindings.freetype4j.FreeType;
import org.junit.jupiter.api.Test;

public class FreeTypeTest {
	@Test
	public void testInit() {
		var freetype = new FreeType();
		System.out.println("FreeType version: " + freetype.getVersion());
		freetype.close();
	}
}
