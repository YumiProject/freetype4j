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
import org.jetbrains.annotations.Unmodifiable;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FTFace implements AutoCloseable {
	private final MemorySegment handle;
	private final FTBBox bbox;

	FTFace(MemorySegment handle) {
		this.handle = handle.reinterpret(FreeTypeNative.FT_FACE_LAYOUT.byteSize());

		long bboxOffset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("bbox"));
		this.bbox = new FTBBox(this.handle.asSlice(bboxOffset, FTBBox.LAYOUT));
	}

	/**
	 * {@return the native handle of this FreeType Face object}
	 */
	@Contract(pure = true)
	public @NotNull MemorySegment handle() {
		return this.handle;
	}

	/**
	 * {@return the number of faces in the font of this face}
	 * Some font formats can have multiple faces in a single font file.
	 */
	public long faceCount() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("num_faces"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}

	/**
	 * {@return the face index of this face}
	 */
	public long faceIndex() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("face_index"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}

	/**
	 * {@return the number of glyphs in this face}
	 */
	public long glyphCount() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("num_glyphs"));
		return this.handle.get(ValueLayout.JAVA_LONG, offset);
	}

	/**
	 * {@return the family name of this face}
	 */
	public @NotNull String familyName() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("family_name"));
		var ptr = this.handle.get(FreeTypeNative.C_POINTER, offset);
		return ptr.getString(0);
	}

	/**
	 * {@return the style name of this face if present, or {@linkplain Optional#empty() nothing} otherwise}
	 * <p>
	 * This is an ASCII string, usually in English, that describes the typeface's style
	 * (like ‘Italic’, ‘Bold’, ‘Condensed’, etc).
	 * Not all font formats provide a style name, so this field is optional,
	 * and can return {@linkplain Optional#empty() nothing}.
	 */
	public @NotNull Optional<String> styleName() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("style_name"));
		var ptr = this.handle.get(FreeTypeNative.C_POINTER, offset);

		if (ptr.equals(MemorySegment.NULL)) {
			return Optional.empty();
		} else {
			return Optional.of(ptr.getString(0));
		}
	}

	public int fixedSizesCount() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("num_fixed_sizes"));
		return this.handle.get(ValueLayout.JAVA_INT, offset);
	}

	public int charMapCount() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("num_charmaps"));
		return this.handle.get(ValueLayout.JAVA_INT, offset);
	}

	public @Unmodifiable List<FTCharMap> charMaps() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("charmaps"));
		var array = this.handle.get(FreeTypeNative.C_POINTER, offset);
		var list = new ArrayList<FTCharMap>();

		for (int i = 0; i < this.charMapCount(); i++) {
			var item = array.getAtIndex(FreeTypeNative.C_POINTER, i).reinterpret(FTCharMap.LAYOUT.byteSize());
			list.add(new FTCharMap(this, item));
		}

		return List.copyOf(list);
	}

	@Contract(pure = true)
	public @NotNull FTBBox bbox() {
		return this.bbox;
	}

	public short unitsPerEm() {
		long offset = FreeTypeNative.FT_FACE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("units_per_EM"));
		return this.handle.get(ValueLayout.JAVA_SHORT, offset);
	}

	/**
	 * Sets the character size of this face.
	 *
	 * @param charWidth the nominal width, in 26.6 fractional points
	 * @param charHeight the nominal height, in 26.6 fractional points
	 * @param horizontalResolution the horizontal resolution in DPI
	 * @param verticalResolution the vertical resolution in DPI
	 */
	public void setCharSize(
			long charWidth, long charHeight, int horizontalResolution, int verticalResolution
	) {
		int result;

		try {
			result = (int) FreeTypeNative.get().ft$SetCharSize.invokeExact(
					this.handle, charWidth, charHeight, horizontalResolution, verticalResolution
			);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}

		if (result != 0) {
			throw new FreeTypeException(result, FreeType.getErrorString(result));
		}
	}

	/**
	 * Sets the character size of this face in pixels.
	 *
	 * @param width the nominal width, in pixels
	 * @param height the nominal height, in pixels
	 */
	public void setPixelSizes(int width, int height) {
		int result;

		try {
			result = (int) FreeTypeNative.get().ft$setPixelSizes.invokeExact(
					this.handle, width, height
			);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}

		if (result != 0) {
			throw new FreeTypeException(result, FreeType.getErrorString(result));
		}
	}

	public void selectCharMap(FTEncoding encoding) {
		int result;

		try {
			result = (int) FreeTypeNative.get().ft$SelectCharmap.invokeExact(
					this.handle, encoding.id()
			);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}

		if (result != 0) {
			throw new FreeTypeException(result, FreeType.getErrorString(result));
		}
	}

	public void setCharMap(FTCharMap charMap) {
		int result;

		try {
			result = (int) FreeTypeNative.get().ft$SetCharmap.invokeExact(
					this.handle, charMap.handle()
			);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}

		if (result != 0) {
			throw new FreeTypeException(result, FreeType.getErrorString(result));
		}
	}

	@Override
	public void close() {
		int result;

		try {
			result = (int) FreeTypeNative.get().ft$DoneFace.invokeExact(this.handle);
		} catch (Throwable e) {
			throw new AssertionError("Should not reach here.", e);
		}

		if (result != 0) {
			throw new FreeTypeException(result, FreeType.getErrorString(result));
		}
	}
}
