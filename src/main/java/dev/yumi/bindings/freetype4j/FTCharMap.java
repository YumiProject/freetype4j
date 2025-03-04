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

/**
 * Represents a character map (usually abbreviated to "charmap").
 * <p>
 * A charmap is used to translate character codes in a given encoding into glyph indexes for its parent's face.
 * Some font formats may provide several charmaps per font.
 * <p>
 * Each face object owns zero or more charmaps, but only one of them can be "active".
 *
 * @see FTFace#charMaps()
 * @see FTFace#setCharMap(FTCharMap)
 * @see FTFace#selectCharMap(FTEncoding)
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class FTCharMap {
	static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
			FreeTypeNative.C_POINTER.withName("face"),
			ValueLayout.JAVA_INT.withName("encoding"),
			ValueLayout.JAVA_SHORT.withName("platform_id"),
			ValueLayout.JAVA_SHORT.withName("encoding_id")
	);

	private final FTFace parent;
	private final MemorySegment handle;

	public FTCharMap(MemorySegment handle) {
		this(new FTFace(getFace(handle)), handle);
	}

	public FTCharMap(FTFace parent, MemorySegment handle) {
		this.parent = parent;
		this.handle = handle;

		if (!parent.handle().equals(getFace(handle))) {
			throw new IllegalArgumentException("Given parent is not matching the given FT_CharMap handle's parent field.");
		}
	}

	/**
	 * {@return the native handle of this FreeType CharMap object}
	 */
	@Contract(pure = true)
	public @NotNull MemorySegment handle() {
		return this.handle;
	}

	/**
	 * {@return the parent of this character map}
	 */
	public FTFace parent() {
		return this.parent;
	}

	/**
	 * {@return an {@link FTEncoding} tag identifying this character map}
	 *
	 * @see FTFace#selectCharMap(FTEncoding)
	 */
	public FTEncoding encoding() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("encoding"));
		int id = this.handle.get(ValueLayout.JAVA_INT, offset);
		return FTEncoding.byId(id);
	}

	/**
	 * {@return an identifier number describing the platform for the following encoding identifier}
	 * This comes directly from the TrueType specification and gets emulated for other formats.
	 */
	public short platformId() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("platform_id"));
		return this.handle.get(ValueLayout.JAVA_SHORT, offset);
	}

	/**
	 * {@return a platform-specific encoding number}
	 * This also comes from the TrueType specification and gets emulated similarly.
	 */
	public short encodingId() {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("encoding_id"));
		return this.handle.get(ValueLayout.JAVA_SHORT, offset);
	}

	private static MemorySegment getFace(MemorySegment handle) {
		long offset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("face"));
		return handle.get(FreeTypeNative.C_POINTER, offset);
	}
}
