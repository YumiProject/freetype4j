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

import java.lang.foreign.MemorySegment;

public class FTFace implements AutoCloseable {
	private final MemorySegment handle;

	FTFace(MemorySegment handle) {
		this.handle = handle;
	}

	/**
	 * {@return the native handle of this FreeType Face object}
	 */
	@Contract(pure = true)
	public @NotNull MemorySegment handle() {
		return this.handle;
	}

	/**
	 * Sets the character size of this face.
	 *
	 * @param charWidth the nominal width, in 26.6 fractional points
	 * @param charHeight the nominal height, in 26.6 fractional points
	 * @param horizontalResolution the horizontal resolution in DPI
	 * @param verticalResolution the vertical resolution in DPI
	 */
	public void setCharSize(long charWidth, long charHeight, int horizontalResolution, int verticalResolution) {
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

	@Override
	public void close() {
		try {
			FreeTypeNative.get().ft$DoneFace.invokeExact(this.handle);
		} catch (Throwable e) {
			throw new AssertionError("Should not reach here.", e);
		}
	}
}
