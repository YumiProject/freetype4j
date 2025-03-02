/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j;

import java.util.List;

/**
 * Represents the FreeType error code values.
 * <p>
 * Some FreeType errors may be missing from this file if a newer version of FreeType is used.
 * <p>
 * Based on the {@code fterrdef.h} file.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public enum FreeTypeError {
	//region Generic errors
	OK(0x00, "No error"),
	CANNOT_OPEN_RESOURCE(0x01, "Cannot open resource"),
	UNKNOWN_FILE_FORMAT(0x02, "Unknown file format"),
	INVALID_FILE_FORMAT(0x03, "Invalid file format"),
	INVALID_VERSION(0x04, "Invalid FreeType version"),
	LOWER_MODULE_VERSION(0x05, "Module version is too low"),
	INVALID_ARGUMENT(0x06, "Invalid argument"),
	UNIMPLEMENTED_FEATURE(0x07, "Unimplemented feature"),
	INVALID_TABLE(0x08, "Invalid table"),
	INVALID_OFFSET(0x09, "Invalid offset within table"),
	ARRAY_TOO_LARGE(0x0a, "Array allocation size too large"),
	MISSING_MODULE(0x0b, "Missing module"),
	MISSING_PROPERTY(0x0c, "Missing property"),
	//endregion
	//region Glyph/Character errors
	INVALID_GLYPH_INDEX(0x10, "Invalid glyph index"),
	INVALID_CHARACTER_CODE(0x11, "Invalid character code"),
	INVALID_GLYPH_FORMAT(0x12, "Unsupported glyph image format"),
	CANNOT_RENDER_GLYPH(0x13, "Cannot render this glyph format"),
	INVALID_OUTLINE(0x14, "Invalid outline"),
	INVALID_COMPOSITE(0x15, "Invalid composite glyph"),
	TOO_MANY_HINTS(0x16, "Too many hints"),
	INVALID_PIXEL_SIZE(0x17, "Invalid pixel size"),
	INVALID_SVG_DOCUMENT(0x18, "Invalid SVG document"),
	//endregion
	//region Handle errors
	INVALID_HANDLE(0x20, "Invalid object handle"),
	INVALID_LIBRARY_HANDLE(0x21, "Invalid library handle"),
	INVALID_DRIVER_HANDLE(0x22, "Invalid module handle"),
	INVALID_FACE_HANDLE(0x23, "Invalid face handle"),
	INVALID_SIZE_HANDLE(0x24, "Invalid size handle"),
	INVALID_SLOT_HANDLE(0x25, "Invalid glyph slot handle"),
	INVALID_CHARMAP_HANDLE(0x26, "Invalid charmap handle"),
	INVALID_CACHE_HANDLE(0x27, "Invalid cache handle"),
	INVALID_STREAM_HANDLE(0x28, "Invalid stream handle"),
	//endregion
	//region Driver errors
	TOO_MANY_DRIVERS(0x30, "Too many modules"),
	TOO_MANY_EXTENSIONS(0x31, "Too many extensions"),
	//endregion
	//region Memory errors
	OUT_OF_MEMORY(0x40, "Out of memory"),
	UNLISTED_OBJECT(0x41, "Unlisted object"),
	//endregion
	//region Stream errors
	CANNOT_OPEN_STREAM(0x51, "Cannot open stream"),
	INVALID_STREAM_SEEK(0x52, "Invalid stream seek"),
	INVALID_STREAM_SKIP(0x53, "Invalid stream skip"),
	INVALID_STREAM_READ(0x54, "Invalid stream read"),
	INVALID_STREAM_OPERATION(0x55, "Invalid stream operation"),
	INVALID_FRAME_OPERATION(0x56, "Invalid frame operation"),
	NESTED_FRAME_ACCESS(0x57, "Nested frame access"),
	INVALID_FRAME_READ(0x58, "Invalid frame read"),
	//endregion
	//region Raster errors
	//endregion
	//region Cache errors
	//endregion
	//region TrueType and SFNT errors
	//endregion
	//region CFF, CID, and Type 1 errors
	//endregion
	//region BDF errors
	CORRUPTED_FONT_GLYPHS(0xba, "Font glyphs corrupted or missing fields"),
	//endregion
	;

	public static final List<FreeTypeError> VALUES = List.of(values());
	private final int id;
	private final String message;

	FreeTypeError(int id, String message) {
		this.id = id;
		this.message = message;
	}

	/**
	 * {@return the error code identifier associated with this error}
	 */
	public int id() {
		return this.id;
	}

	/**
	 * {@return the message associated with this error}
	 */
	public String message() {
		return this.message;
	}

	/**
	 * {@return the FreeType error associated with the given {@code errorCode}}
	 *
	 * @param errorCode the error code
	 */
	public static FreeTypeError byId(int errorCode) {
		return VALUES.stream().filter(v -> v.id == errorCode).findFirst().orElse(null);
	}
}
