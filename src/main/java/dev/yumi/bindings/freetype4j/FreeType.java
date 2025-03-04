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

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Files;
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

	/**
	 * Creates a face object from a given resource path.
	 *
	 * @param fontPath the path to the font file
	 * @param faceIndex the face index, which holds two different values.
	 * From bits 0-15 are the index of the face in the font file (starting with value {@code 0}).
	 * Set it to {@code 0} if there is only one face in the font file.
	 * <p>
	 * Since FreeType 2.6.1, bits 16-30 are relevant to GX and OpenType variation fonts only,
	 * specifying the named instance index for the current face index (starting with value {@code 1},
	 * value {@code 0} makes FreeType ignore named instances).
	 * For non-variation fonts, bits 16-30 are ignored. Assuming that you want to access the third named instance
	 * in face {@code 4}, {@code faceIndex} should be set to {@code 0x00030004}. If you want to access face {@code 4}
	 * without variation handling, simply set {@code faceIndex} to value {@code 4}.
	 *
	 * @return the new face object
	 * @see #newFace(Path, long)
	 * @see #newMemoryFace(byte[], long)
	 * @see #newMemoryFace(InputStream, long)
	 */
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

	public FTFace newFace(Path path, long faceIndex) throws IOException {
		var bytes = Files.readAllBytes(path);
		return this.newMemoryFace(bytes, faceIndex);
	}

	public FTFace newMemoryFace(byte[] fontData, long faceIndex) {
		this.checkCanBeUsed();

		var faceArena = Arena.ofShared();

		try (var localArena = Arena.ofConfined()) {
			var ptr = localArena.allocate(ValueLayout.ADDRESS);
			int result;

			try {
				result = (int) FreeTypeNative.get().ft$NewMemoryFace.invokeExact(
						this.handle,
						faceArena.allocateFrom(ValueLayout.JAVA_BYTE, fontData), (long) fontData.length,
						faceIndex, ptr
				);
			} catch (Throwable e) {
				throw new AssertionError("Should not reach here.", e);
			}

			if (result != FreeTypeError.OK.id()) {
				throw new FreeTypeException(result, getErrorString(result));
			}

			return new FTFace.FromMemory(faceArena, ptr.get(ValueLayout.ADDRESS, 0));
		}
	}

	public FTFace newMemoryFace(InputStream inputStream, long faceIndex) throws IOException {
		return this.newMemoryFace(inputStream.readAllBytes(), faceIndex);
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
