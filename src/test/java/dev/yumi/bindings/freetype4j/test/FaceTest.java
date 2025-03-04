/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j.test;

import dev.yumi.bindings.freetype4j.FreeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FaceTest {
	private static final long GLYPH_COUNT = 2030;
	private static final String FAMILY_NAME = "Fira Code";
	private static final String STYLE_NAME = "Regular";

	@Test
	public void testClassicLoad() {
		try (
				var freetype = new FreeType();
				var face = freetype.newFace("build/test/ttf/FiraCode-Regular.ttf", 0)
		) {
			assertEquals(1, face.faceCount());
			assertEquals(0, face.faceIndex());
			assertEquals(GLYPH_COUNT, face.glyphCount());
			assertEquals(FAMILY_NAME, face.familyName());
			assertEquals(Optional.of(STYLE_NAME), face.styleName());
			assertEquals(0, face.fixedSizesCount());
			assertEquals(4, face.charMapCount());
			assertEquals(1950, face.unitsPerEm());
		}
	}

	@Test
	public void testNioLoad() throws IOException {
		try (
				var freetype = new FreeType();
				var face = freetype.newFace(Path.of("build/test/ttf/FiraCode-Regular.ttf"), 0)
		) {
			assertEquals(1, face.faceCount());
			assertEquals(0, face.faceIndex());
			assertEquals(GLYPH_COUNT, face.glyphCount());
			assertEquals(FAMILY_NAME, face.familyName());
			assertEquals(Optional.of(STYLE_NAME), face.styleName());
			assertEquals(0, face.fixedSizesCount());
			assertEquals(4, face.charMapCount());
			assertEquals(1950, face.unitsPerEm());
		}
	}
}
