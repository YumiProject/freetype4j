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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Represents a FreeType library handle and the FreeType bindings.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class FreeType implements AutoCloseable {
	private final MemorySegment handle;
	private boolean canBeUsed = true;

	public FreeType() {
		try (var arena = Arena.ofConfined()) {
			var ptr = arena.allocate(ValueLayout.ADDRESS);
			int result;

			try {
				result = (int) FreeTypeNative.get().ft$InitFreeType.invokeExact(ptr);
			} catch (Throwable e) {
				throw new AssertionError("Should not reach here.", e);
			}

			if (result != FreeTypeError.OK.id()) {
				throw new FreeTypeInitException(result, getErrorString(result));
			}

			this.handle = ptr.get(ValueLayout.ADDRESS, 0);
		}
	}

	/**
	 * {@return the native handle of this FreeType library instance}
	 */
	@Contract(pure = true)
	public @NotNull MemorySegment handle() {
		return this.handle;
	}

	/**
	 * Gets the version of FreeType that has been dynamically linked to.
	 *
	 * @return the version of FreeType
	 */
	public FreeTypeVersion getVersion() {
		this.checkCanBeUsed();

		try (var arena = Arena.ofConfined()) {
			var majorPtr = arena.allocate(ValueLayout.ADDRESS);
			var minorPtr = arena.allocate(ValueLayout.ADDRESS);
			var patchPtr = arena.allocate(ValueLayout.ADDRESS);

			FreeTypeNative.get().ft$LibraryVersion.invokeExact(this.handle, majorPtr, minorPtr, patchPtr);

			return new FreeTypeVersion(
					majorPtr.get(ValueLayout.JAVA_INT, 0),
					minorPtr.get(ValueLayout.JAVA_INT, 0),
					patchPtr.get(ValueLayout.JAVA_INT, 0)
			);
		} catch (Throwable e) {
			throw new AssertionError("Should not reach here.", e);
		}
	}

	public FTFace newFace(String fontPath, long faceIndex) {
		this.checkCanBeUsed();

		try (var arena = Arena.ofConfined()) {
			var ptr = arena.allocate(ValueLayout.ADDRESS);
			int result;

			try {
				result = (int) FreeTypeNative.get().ft$NewFace.invokeExact(
						this.handle, arena.allocateFrom(fontPath), faceIndex, ptr
				);
			} catch (Throwable e) {
				throw new AssertionError("Should not reach here.", e);
			}

			if (result != FreeTypeError.OK.id()) {
				throw new FreeTypeException(result, getErrorString(result));
			}

			return new FTFace(ptr.get(ValueLayout.ADDRESS, 0));
		}
	}

	public FTFace newFace(Path fontPath, long faceIndex) {
		if (fontPath.getFileSystem() != FileSystems.getDefault()) {
			throw new IllegalArgumentException("Path is not from the default filesystem.");
		}

		return this.newFace(fontPath.toAbsolutePath().toString(), faceIndex);
	}

	public FTFace newMemoryFace(byte[] fontData, long faceIndex) {
		this.checkCanBeUsed();

		try (var arena = Arena.ofConfined()) {
			var ptr = arena.allocate(ValueLayout.ADDRESS);
			int result;

			try {
				result = (int) FreeTypeNative.get().ft$NewMemoryFace.invokeExact(
						this.handle,
						arena.allocateFrom(ValueLayout.JAVA_BYTE, fontData), (long) fontData.length,
						faceIndex, ptr
				);
			} catch (Throwable e) {
				throw new AssertionError("Should not reach here.", e);
			}

			if (result != FreeTypeError.OK.id()) {
				throw new FreeTypeException(result, getErrorString(result));
			}

			return new FTFace(ptr.get(ValueLayout.ADDRESS, 0));
		}
	}

	@Override
	public void close() {
		this.checkCanBeUsed();
		this.canBeUsed = false;

		int result;

		try {
			result = (int) FreeTypeNative.get().ft$DoneFreeType.invokeExact(this.handle);
		} catch (Throwable e) {
			throw new AssertionError("Should not reach here.", e);
		}

		if (result != 0) {
			throw new FreeTypeException(result, getErrorString(result));
		}
	}

	private void checkCanBeUsed() {
		if (!this.canBeUsed) {
			throw new IllegalStateException("Cannot call FreeType library if it's already closed.");
		}
	}

	/**
	 * Retrieve the description of a valid FreeType error code.
	 *
	 * @param errorCode a valid FreeType error code
	 * @return the description of the error code, or {@code null} if any error occurred
	 */
	public static String getErrorString(int errorCode) {
		var error = FreeTypeError.byId(errorCode);

		if (error != null) {
			return error.message();
		} else if (FreeTypeNative.get().ft$ErrorString != null) {
			// If the bound FreeType library supports FT_Error_String,
			// then call it if the error code is unknown.
			try {
				var errorPtr = (MemorySegment) FreeTypeNative.get().ft$ErrorString.invokeExact(errorCode);

				if (errorPtr.equals(MemorySegment.NULL)) {
					return null;
				}

				return errorPtr.getString(0);
			} catch (Throwable e) {
				throw new AssertionError("Should not reach here.", e);
			}
		} else {
			return null;
		}
	}
}
