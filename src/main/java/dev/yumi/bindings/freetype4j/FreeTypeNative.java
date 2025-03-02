/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

class FreeTypeNative {
	private static FreeTypeNative instance;

	static FreeTypeNative get() {
		if (instance != null) {
			return instance;
		}

		// This should be safe since most JDKs bundle or depend on FreeType themselves:
		// - https://github.com/openjdk/jdk/blob/157e5ad4a3abc7aea9ec2ec3d2381e42101990b8/src/java.desktop/share/native/libfreetype/java_freetype.c
		// - https://github.com/openjdk/jdk/blob/157e5ad4a3abc7aea9ec2ec3d2381e42101990b8/src/java.desktop/share/classes/sun/font/FontManagerNativeLibrary.java#L53
		System.loadLibrary("freetype");

		var lookup = Linker.nativeLinker().defaultLookup().or(SymbolLookup.loaderLookup());
		return instance = new FreeTypeNative(lookup);
	}

	final MethodHandle ft$InitFreeType;
	final MethodHandle ft$DoneFreeType;
	final MethodHandle ft$LibraryVersion;
	final MethodHandle ft$ErrorString;

	final MethodHandle ft$NewFace;
	final MethodHandle ft$NewMemoryFace;
	final MethodHandle ft$DoneFace;
	final MethodHandle ft$SetCharSize;
	final MethodHandle ft$setPixelSizes;
	final MethodHandle ft$SelectCharmap;

	FreeTypeNative(SymbolLookup lookup) {
		var loader = new Loader(lookup);

		this.ft$InitFreeType = loader.lookup("FT_Init_FreeType",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
		);
		this.ft$DoneFreeType = loader.lookup("FT_Done_FreeType",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
		);
		MethodHandle ft$ErrorString = null;
		try {
			ft$ErrorString = loader.lookup("FT_Error_String",
					FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
			);
		} catch (Throwable _) {
			// Ignored.
		} finally {
			this.ft$ErrorString = ft$ErrorString;
		}
		this.ft$LibraryVersion = loader.lookup("FT_Library_Version",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
		);

		this.ft$NewFace = loader.lookup("FT_New_Face",
				FunctionDescriptor.of(ValueLayout.JAVA_INT,
						ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS
				)
		);
		this.ft$NewMemoryFace = loader.lookup("FT_New_Memory_Face",
				FunctionDescriptor.of(ValueLayout.JAVA_INT,
						ValueLayout.ADDRESS, // FreeType handle
						ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, // Font bytes
						ValueLayout.JAVA_LONG, ValueLayout.ADDRESS // face_index and output pointer
				)
		);
		this.ft$DoneFace = loader.lookup("FT_Done_Face",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
		);
		this.ft$SetCharSize = loader.lookup("FT_Set_Char_Size",
				FunctionDescriptor.of(ValueLayout.JAVA_INT,
						ValueLayout.ADDRESS, // FT_Face*
						ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT
				)
		);
		this.ft$setPixelSizes = loader.lookup("FT_Set_Pixel_Sizes",
				FunctionDescriptor.of(ValueLayout.JAVA_INT,
						ValueLayout.ADDRESS, // FT_Face*
						ValueLayout.JAVA_INT, ValueLayout.JAVA_INT
				)
		);
		this.ft$SelectCharmap = loader.lookup("FT_Select_Charmap",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
		);
	}

	private static class Loader {
		final SymbolLookup lookup;
		final Linker linker;

		private Loader(SymbolLookup lookup) {
			this.lookup = lookup;
			this.linker = Linker.nativeLinker();
		}

		public MethodHandle lookup(String name, FunctionDescriptor descriptor) {
			return this.linker.downcallHandle(
					this.lookup.findOrThrow(name),
					descriptor
			);
		}
	}
}
