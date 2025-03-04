/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class FTBBox {
	static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
			ValueLayout.JAVA_LONG.withName("xMin"),
			ValueLayout.JAVA_LONG.withName("yMin"),
			ValueLayout.JAVA_LONG.withName("xMax"),
			ValueLayout.JAVA_LONG.withName("yMax")
	).withName("FT_BBox");

	private final MemorySegment handle;

	public FTBBox(MemorySegment handle) {
		this.handle = handle;
	}

	/**
	 * {@return the native handle of this FreeType BBox object}
	 */
	@Contract(pure = true)
	public @NotNull MemorySegment handle() {
		return this.handle;
	}

	public long xMin() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("xMin"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}

	public long yMin() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("yMin"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}

	public long xMax() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("xMax"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}

	public long yMax() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("yMax"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}
}
